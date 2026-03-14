import { Router } from 'express';
import { db, contracts, contractSignatures, inspections } from '@inspectavision/database';
import { authMiddleware, AuthRequest } from '../middleware/auth.js';
import { z } from 'zod';
import { eq, and } from 'drizzle-orm';
import crypto from 'crypto';

const router = Router();
router.use(authMiddleware as any);

const CreateContractSchema = z.object({
  inspectionId: z.string(),
  content: z.string(),
});

const SignatureSchema = z.object({
  signatureImage: z.string(), // Base64 image
});

router.post('/', async (req: AuthRequest, res) => {
  try {
    const { inspectionId, content } = CreateContractSchema.parse(req.body);
    const id = crypto.randomUUID();
    
    await db.insert(contracts).values({
      id,
      inspectionId,
      content,
      status: 'draft',
    });

    res.status(201).json({ id });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

router.get('/:id', async (req: AuthRequest, res) => {
  const contract = await db.query.contracts.findFirst({
    where: eq(contracts.id, req.params.id),
    with: {
      signatures: true,
      inspection: {
        with: {
          client: true,
          property: true,
        }
      }
    }
  });

  if (!contract) return res.status(404).json({ error: 'Contract not found' });
  res.json(contract);
});

router.post('/:id/sign', async (req: AuthRequest, res) => {
  try {
    const { id } = req.params;
    const { signatureImage } = SignatureSchema.parse(req.body);
    
    const sigId = crypto.randomUUID();
    await db.insert(contractSignatures).values({
      id: sigId,
      contractId: id,
      signerType: req.user!.role === 'client' ? 'client' : 'inspector',
      signatureImageUrl: signatureImage,
      ipAddress: req.ip,
    });

    // Check if both have signed (simplified logic)
    // In production, we'd check the signatures table
    await db.update(contracts)
      .set({ status: 'signed' })
      .where(eq(contracts.id, id));

    res.status(201).json({ id: sigId });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

export default router;
