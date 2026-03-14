import { z } from 'zod';

export const UserRoleSchema = z.enum(['admin', 'inspector', 'client']);

export const UserSchema = z.object({
  id: z.string(),
  orgId: z.string().optional(),
  email: z.string().email(),
  role: UserRoleSchema,
  createdAt: z.date(),
});

export const OrganizationSchema = z.object({
  id: z.string(),
  name: z.string().min(1),
  logoUrl: z.string().url().optional(),
  createdAt: z.date(),
});

export const FindingSeveritySchema = z.enum(['safety', 'major', 'minor', 'maintenance', 'info']);

export const FindingSchema = z.object({
  id: z.string(),
  categoryId: z.string(),
  location: z.string().optional(),
  severity: FindingSeveritySchema,
  description: z.string(),
  recommendation: z.string().optional(),
  internachiReference: z.string().optional(),
  inspectorNotes: z.string().optional(),
  createdAt: z.date(),
});
