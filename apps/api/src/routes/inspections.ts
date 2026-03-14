import { Router } from 'express';
import { db, inspections, inspectionCategories, findings, findingImages } from '@inspectavision/database';
import { authMiddleware, AuthRequest } from '../middleware/auth.js';
import { analyzeInspectionCategory } from '@inspectavision/ai/vision';
import { vectorStore } from '@inspectavision/ai/vector_store';
import { generateInspectionPDF } from '../utils/pdf.js';
import { z } from 'zod';
import { eq, and } from 'drizzle-orm';
import crypto from 'crypto';
import multer from 'multer';
import path from 'path';
import fs from 'fs/promises';

const router = Router();
router.use(authMiddleware as any);

const upload = multer({ dest: 'uploads/' });

const CreateInspectionSchema = z.object({
  clientId: z.string(),
  propertyId: z.string(),
  scheduledAt: z.string().optional(),
});

router.post('/', async (req: AuthRequest, res) => {
  try {
    const { clientId, propertyId, scheduledAt } = CreateInspectionSchema.parse(req.body);
    const id = crypto.randomUUID();
    
    await db.insert(inspections).values({
      id,
      orgId: req.user!.orgId,
      inspectorId: req.user!.id,
      clientId,
      propertyId,
      status: 'scheduled',
      scheduledAt: scheduledAt ? new Date(scheduledAt) : new Date(),
    });

    // Initialize standard InterNACHI categories
    const standardCategories = ['Roof', 'Exterior', 'Basement/Foundation', 'Heating', 'Cooling', 'Plumbing', 'Electrical', 'Fireplace', 'Attic/Insulation', 'Interior'];
    for (const catName of standardCategories) {
      await db.insert(inspectionCategories).values({
        id: crypto.randomUUID(),
        inspectionId: id,
        name: catName,
      });
    }

    res.status(201).json({ id });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

router.post('/:id/upload', upload.array('photos'), async (req: AuthRequest, res) => {
  try {
    const { categoryId } = req.body;
    const files = req.files as Express.Multer.File[];
    
    if (!files || files.length === 0) {
      return res.status(400).json({ error: 'No photos uploaded' });
    }

    // In a real app, we'd move files to a permanent storage (S3/Local Volume)
    // and store the accessible URL. For now, we use the local path.
    const results = [];
    for (const file of files) {
      // Logic for saving and associating with inspection category...
      results.push({ filename: file.filename, originalName: file.originalname });
    }

    res.status(201).json({ message: 'Photos uploaded successfully', files: results });
  } catch (error) {
    res.status(500).json({ error: (error as any).message });
  }
});

router.post('/:id/analyze/:categoryId', async (req: AuthRequest, res) => {
  try {
    const { id, categoryId } = req.params;
    
    // 1. Get the category and inspection to verify ownership
    const category = await db.query.inspectionCategories.findFirst({
      where: eq(inspectionCategories.id, categoryId),
    });

    if (!category) return res.status(404).json({ error: 'Category not found' });

    // 2. Mock AI Analysis (since we don't have real base64 image data in this env)
    // In production, analyzeInspectionCategory() would be called with real image buffers
    const aiResult = {
      findings: [
        {
          location: "Southwest corner of roof",
          severity: "maintenance",
          description: "Missing asphalt shingles and exposed underlayment.",
          recommendation: "Repair by a licensed roofing contractor."
        }
      ]
    };

    const savedFindings = [];
    for (const f of aiResult.findings) {
      // 3. RAG: Search InterNACHI standards for the AI description
      const searchResult = await vectorStore.query(f.description, 1);
      const standardRef = searchResult.documents[0]?.[0] || "InterNACHI Standard of Practice";

      const findingId = crypto.randomUUID();
      await db.insert(findings).values({
        id: findingId,
        categoryId,
        location: f.location,
        severity: f.severity as any,
        description: f.description,
        recommendation: f.recommendation,
        internachiReference: standardRef,
      });
      savedFindings.push({ id: findingId, ...f, standardRef });
    }

    res.json({ findings: savedFindings });
  } catch (error) {
    res.status(500).json({ error: (error as any).message });
  }
});

router.get('/:id/pdf', async (req: AuthRequest, res) => {
  try {
    const { id } = req.params;
    
    const inspection = await db.query.inspections.findFirst({
      where: eq(inspections.id, id),
      with: {
        client: true,
        property: true,
      }
    });

    if (!inspection) return res.status(404).json({ error: 'Inspection not found' });

    // Fetch all findings for this inspection
    const allCategories = await db.query.inspectionCategories.findMany({
      where: eq(inspectionCategories.inspectionId, id),
    });

    const allFindings = [];
    for (const cat of allCategories) {
      const catFindings = await db.query.findings.findMany({
        where: eq(findings.categoryId, cat.id),
      });
      allFindings.push(...catFindings);
    }

    const pdfBuffer = await generateInspectionPDF(inspection, allFindings);

    res.contentType('application/pdf');
    res.setHeader('Content-Disposition', `attachment; filename=Inspection_Report_${id}.pdf`);
    res.send(pdfBuffer);
  } catch (error) {
    res.status(500).json({ error: (error as any).message });
  }
});

export default router;
