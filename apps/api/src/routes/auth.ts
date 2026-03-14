import { Router } from 'express';
import { db, organizations, users } from '@inspectavision/database';
import { hashPassword, comparePassword, generateToken } from '../utils/auth.js';
import { z } from 'zod';
import { eq } from 'drizzle-orm';
import crypto from 'crypto';

const router = Router();

const RegisterSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
  organizationName: z.string().min(1),
});

const LoginSchema = z.object({
  email: z.string().email(),
  password: z.string(),
});

router.post('/register', async (req, res) => {
  try {
    const { email, password, organizationName } = RegisterSchema.parse(req.body);

    const existingUser = await db.query.users.findFirst({
      where: eq(users.email, email),
    });

    if (existingUser) {
      return res.status(400).json({ error: 'User already exists' });
    }

    const orgId = crypto.randomUUID();
    const userId = crypto.randomUUID();
    const passwordHash = await hashPassword(password);

    await db.transaction(async (tx) => {
      await tx.insert(organizations).values({
        id: orgId,
        name: organizationName,
      });

      await tx.insert(users).values({
        id: userId,
        orgId,
        email,
        passwordHash,
        role: 'admin',
      });
    });

    const token = generateToken({ id: userId, orgId, role: 'admin' });
    res.status(201).json({ token, user: { id: userId, email, orgId, role: 'admin' } });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

router.post('/login', async (req, res) => {
  try {
    const { email, password } = LoginSchema.parse(req.body);

    const user = await db.query.users.findFirst({
      where: eq(users.email, email),
    });

    if (!user || !(await comparePassword(password, user.passwordHash))) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const token = generateToken({ id: user.id, orgId: user.orgId!, role: user.role });
    res.json({ token, user: { id: user.id, email: user.email, orgId: user.orgId, role: user.role } });
  } catch (error) {
    res.status(400).json({ error: (error as any).message });
  }
});

export default router;
