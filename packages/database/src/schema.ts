import { sqliteTable, text, integer, primaryKey } from 'drizzle-orm/sqlite-core';
import { sql, relations } from 'drizzle-orm';

// --- Multi-tenant Core ---
...
// --- Relations ---
export const organizationsRelations = relations(organizations, ({ many }) => ({
  users: many(users),
  clients: many(clients),
  properties: many(properties),
}));

export const usersRelations = relations(users, ({ one, many }) => ({
  organization: one(organizations, { fields: [users.orgId], references: [organizations.id] }),
  inspections: many(inspections),
  profile: one(inspectorProfiles, { fields: [users.id], references: [inspectorProfiles.userId] }),
}));

export const inspectionsRelations = relations(inspections, ({ one, many }) => ({
  organization: one(organizations, { fields: [inspections.orgId], references: [organizations.id] }),
  inspector: one(users, { fields: [inspections.inspectorId], references: [users.id] }),
  client: one(clients, { fields: [inspections.clientId], references: [clients.id] }),
  property: one(properties, { fields: [inspections.propertyId], references: [properties.id] }),
  categories: many(inspectionCategories),
  contracts: many(contracts),
  reports: many(reports),
}));

export const inspectionCategoriesRelations = relations(inspectionCategories, ({ one, many }) => ({
  inspection: one(inspections, { fields: [inspectionCategories.inspectionId], references: [inspections.id] }),
  findings: many(findings),
}));

export const findingsRelations = relations(findings, ({ one, many }) => ({
  category: one(inspectionCategories, { fields: [findings.categoryId], references: [inspectionCategories.id] }),
  images: many(findingImages),
}));

export const contractsRelations = relations(contracts, ({ one, many }) => ({
  inspection: one(inspections, { fields: [contracts.inspectionId], references: [inspections.id] }),
  signatures: many(contractSignatures),
}));
export const organizations = sqliteTable('organizations', {
  id: text('id').primaryKey(),
  name: text('name').notNull(),
  logoUrl: text('logo_url'),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

export const users = sqliteTable('users', {
  id: text('id').primaryKey(),
  orgId: text('org_id').references(() => organizations.id),
  email: text('email').notNull().unique(),
  passwordHash: text('password_hash').notNull(),
  role: text('role', { enum: ['admin', 'inspector', 'client'] }).notNull().default('inspector'),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

export const inspectorProfiles = sqliteTable('inspector_profiles', {
  userId: text('user_id').primaryKey().references(() => users.id),
  licenseNumber: text('license_number'),
  eoInsuranceInfo: text('eo_insurance_info'),
  signatureUrl: text('signature_url'),
});

// --- CRM Tables ---
export const clients = sqliteTable('clients', {
  id: text('id').primaryKey(),
  orgId: text('org_id').references(() => organizations.id),
  name: text('name').notNull(),
  email: text('email').notNull(),
  phone: text('phone'),
  address: text('address'),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

export const properties = sqliteTable('properties', {
  id: text('id').primaryKey(),
  orgId: text('org_id').references(() => organizations.id),
  address: text('address').notNull(),
  propertyType: text('property_type'),
  squareFootage: integer('square_footage'),
  yearBuilt: integer('year_built'),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

// --- Inspection Core ---
export const inspections = sqliteTable('inspections', {
  id: text('id').primaryKey(),
  orgId: text('org_id').references(() => organizations.id),
  inspectorId: text('inspector_id').references(() => users.id),
  clientId: text('client_id').references(() => clients.id),
  propertyId: text('property_id').references(() => properties.id),
  status: text('status', { enum: ['scheduled', 'in_progress', 'review', 'published'] }).default('scheduled'),
  scheduledAt: integer('scheduled_at', { mode: 'timestamp' }),
  completedAt: integer('completed_at', { mode: 'timestamp' }),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

export const inspectionCategories = sqliteTable('inspection_categories', {
  id: text('id').primaryKey(),
  inspectionId: text('inspection_id').references(() => inspections.id),
  name: text('name').notNull(), // Roof, Electrical, etc.
  status: text('status', { enum: ['pending', 'completed'] }).default('pending'),
});

export const findings = sqliteTable('findings', {
  id: text('id').primaryKey(),
  categoryId: text('category_id').references(() => inspectionCategories.id),
  location: text('location'),
  severity: text('severity', { enum: ['safety', 'major', 'minor', 'maintenance', 'info'] }).notNull(),
  description: text('description').notNull(),
  recommendation: text('recommendation'),
  internachiReference: text('internachi_reference'),
  inspectorNotes: text('inspector_notes'),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

export const findingImages = sqliteTable('finding_images', {
  id: text('id').primaryKey(),
  findingId: text('finding_id').references(() => findings.id),
  url: text('url').notNull(),
  isPrimary: integer('is_primary', { mode: 'boolean' }).default(false),
});

// --- Reports ---
export const reports = sqliteTable('reports', {
  id: text('id').primaryKey(),
  inspectionId: text('inspection_id').references(() => inspections.id),
  summary: text('summary'),
  pdfUrl: text('pdf_url'),
  publishedAt: integer('published_at', { mode: 'timestamp' }),
});

// --- Contracts ---
export const contracts = sqliteTable('contracts', {
  id: text('id').primaryKey(),
  inspectionId: text('inspection_id').references(() => inspections.id),
  content: text('content').notNull(),
  status: text('status', { enum: ['draft', 'sent', 'signed'] }).default('draft'),
  createdAt: integer('created_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
});

export const contractSignatures = sqliteTable('contract_signatures', {
  id: text('id').primaryKey(),
  contractId: text('contract_id').references(() => contracts.id),
  signerType: text('signer_type', { enum: ['inspector', 'client'] }).notNull(),
  signatureImageUrl: text('signature_image_url').notNull(),
  signedAt: integer('signed_at', { mode: 'timestamp' }).default(sql`CURRENT_TIMESTAMP`),
  ipAddress: text('ip_address'),
});

// --- RAG / Knowledge Base ---
export const internachiChunks = sqliteTable('internachi_chunks', {
  id: text('id').primaryKey(),
  sectionId: text('section_id'),
  category: text('category'),
  standardText: text('standard_text').notNull(),
  sourceUrl: text('source_url'),
});
