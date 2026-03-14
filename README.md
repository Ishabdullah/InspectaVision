# Inspectavision - AI-Powered Home Inspection Platform

[![Live Demo](https://img.shields.io/badge/Live-Demo-blue?style=for-the-badge)](https://inspectavisionai.github.io/InspectaVision/)

Inspectavision is a production-grade, AI-first home inspection platform designed for licensed home inspectors. It replaces traditional template-based tools with a workflow powered by Gemini Vision and InterNACHI Standards of Practice RAG.

## Architecture
- **Monorepo**: Turborepo
- **API**: Node.js + Express + TypeScript
- **Database**: SQLite (Drizzle ORM) + ChromaDB (Vector Store)
- **Frontend**: React (Web CRM & Editor) + React Native/Expo (Mobile Field App)
- **AI**: Google Gemini (Pro, Vision, Embeddings)

## Core Features
1. **AI Inspection Engine**: Automated defect detection in photos via Gemini Vision.
2. **InterNACHI RAG**: Automated standard mapping using a semantic search of the Residential SOP.
3. **Professional Reporting**: Pixel-perfect PDF generation and interactive web reports.
4. **CRM Suite**: Full scheduler, client management, and inspection tracking.
5. **E-Sign Contracts**: Legally binding pre-inspection agreements.

## Getting Started

### Prerequisites
- Node.js 18+
- Google Gemini API Key
- ChromaDB (running via Docker)

### Installation
1. Clone the repository.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Set up environment variables in `apps/api/.env`:
   ```env
   PORT=4000
   DATABASE_URL=inspectavision.db
   JWT_SECRET=your_secret
   GEMINI_API_KEY=your_key
   CHROMA_URL=http://localhost:8000
   ```
4. Scrape and Ingest InterNACHI standards:
   ```bash
   npm run scrape -w packages/ai
   npm run ingest -w packages/ai
   ```
5. Start development:
   ```bash
   npm run dev
   ```

## Development Workflow
- **Web**: `http://localhost:3000`
- **API**: `http://localhost:4000`
- **Mobile**: Use Expo Go to run `apps/mobile`

## Deployment
Use the included `Dockerfile` and `docker-compose.yml` to deploy the API and ChromaDB to any cloud provider.
