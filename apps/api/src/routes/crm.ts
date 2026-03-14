import { Router } from 'express';
import { db, clients, properties, inspections } from '@inspectavision/database';
import { authMiddleware, AuthRequest } from '../middleware/auth.js';
import { z } from 'zod';
import { eq, and } from 'drizzle-orm';
import crypto from 'crypto';

const router = Router();
router.use(authMiddleware as any);

const ClientSchema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
  phone: z.string().optional(),
  address: z.string().optional(),
});

const PropertySchema = z.object({
  address: z.string().min(1),
  propertyType: z.string().optional(),
  squareFootage: z.number().optional(),
  yearBuilt: z.number().optional(),
});

router.get('/clients', async (req: AuthRequest, res) => {
  const data = await db.query.clients.findMany({
    where: eq(clients.orgId, req.user!.orgId),
  });
  res.json(data);
});

router.post('/clients', async (req: AuthRequest, res) => {
  try {
    const body = ClientSchema.parse(req.body);
    const id = crypto.randomUUID();
    await db.insert(clients).values({
      id,
      orgId: req.user!.orgId,
      ...body,
    });
    res.status(201).json({ id, ...body });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

router.get('/properties', async (req: AuthRequest, res) => {
  const data = await db.query.properties.findMany({
    where: eq(properties.orgId, req.user!.orgId),
  });
  res.json(data);
});

router.post('/properties', async (req: AuthRequest, res) => {
  try {
    const body = PropertySchema.parse(req.body);
    const id = crypto.randomUUID();
    await db.insert(properties).values({
      id,
      orgId: req.user!.orgId,
      ...body,
    });
    res.status(201).json({ id, ...body });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

router.get('/inspections', async (req: AuthRequest, res) => {
  const data = await db.query.inspections.findMany({
    where: eq(inspections.orgId, req.user!.orgId),
    with: {
      client: true,
      property: true,
    }
  });
  res.json(data);
});

export default router;
