# InspectaVision - AI-Powered Home Inspection Platform

[![Live Demo](https://img.shields.io/badge/Live-Demo-blue?style=for-the-badge)](https://inspectavisionai.github.io/InspectaVision/)
[![Android Build](https://github.com/InspectaVisionAI/InspectaVision/actions/workflows/android-build.yml/badge.svg)](https://github.com/InspectaVisionAI/InspectaVision/actions/workflows/android-build.yml)
[![Code Audit](CODE_AUDIT_2026-03-14.md)](CODE_AUDIT_2026-03-14.md)

InspectaVision is a production-grade, AI-first home inspection platform designed for licensed home inspectors. It replaces traditional template-based tools with a workflow powered by local LLM inference (llama.cpp) and optional cloud-based Gemini Vision with InterNACHI Standards of Practice RAG.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        InspectaVision                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   Web App   │  │  Android    │  │      API Server         │  │
│  │  (React)    │  │  (Kotlin)   │  │   (Node.js/Express)     │  │
│  │  Port 3000  │  │  (Native)   │  │       Port 4000         │  │
│  └──────┬──────┘  └──────┬──────┘  └───────────┬─────────────┘  │
│         │                │                      │                │
│         └────────────────┼──────────────────────┘                │
│                          │                                       │
│         ┌────────────────┴──────────────────────┐                │
│         │                                       │                │
│  ┌──────▼──────┐                       ┌───────▼────────┐       │
│  │   SQLite    │                       │   ChromaDB     │       │
│  │  (Drizzle)  │                       │  (Vector DB)   │       │
│  └─────────────┘                       └────────────────┘       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              AI Inference Layer                           │   │
│  │  ┌─────────────────┐     ┌────────────────────────────┐   │   │
│  │  │  llama.cpp      │     │  Google Gemini (Optional)  │   │   │
│  │  │  (GGUF Models)  │     │  (Vision + Embeddings)     │   │   │
│  │  │  OFFLINE-FIRST  │     │  CLOUD-BACKUP              │   │   │
│  │  └─────────────────┘     └────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Tech Stack

| Component | Technology | Details |
|-----------|------------|---------|
| **Monorepo** | Turborepo | Shared packages, parallel builds |
| **Web Frontend** | React + Vite | Jetpack Compose-like UI with Material 3 |
| **Android App** | Kotlin + Jetpack Compose | Native llama.cpp integration via JNI |
| **API** | Node.js + Express | TypeScript, JWT auth, Zod validation |
| **Database** | SQLite + Drizzle ORM | Multi-tenant schema |
| **Vector Store** | ChromaDB | InterNACHI SOP RAG |
| **Local AI** | llama.cpp | GGUF models, offline inference |
| **Cloud AI** | Google Gemini | Vision analysis, embeddings |
| **PDF** | Puppeteer | Professional report generation |
| **Mobile (Legacy)** | React Native + Expo | Camera-first field app |

## ✨ Core Features

### 1. **Local LLM Inference (NEW)** 📱
- Run AI analysis completely offline using GGUF models
- Supports any llama.cpp-compatible model (Llama, Phi, Qwen, etc.)
- Streaming token generation for real-time feedback
- Configurable context size, threads, and sampling parameters

### 2. **AI Inspection Engine**
- Automated defect detection in photos via Gemini Vision or local LLM
- Category-based analysis (Roof, Electrical, Plumbing, etc.)
- Severity classification (Safety, Major, Minor, Maintenance)

### 3. **InterNACHI RAG**
- Automated standard mapping using semantic search of Residential SOP
- Vector embeddings via Gemini or local models
- Reference citations in generated findings

### 4. **Professional Reporting**
- Pixel-perfect PDF generation via Puppeteer
- Interactive web reports
- Findings organized by category with severity indicators

### 5. **CRM Suite**
- Client management with contact details
- Property database with address and characteristics
- Inspection scheduling and status tracking

### 6. **E-Sign Contracts**
- Pre-inspection agreement generation
- Digital signature capture (inspector + client)
- Legally binding document storage

## 📦 Project Structure

```
InspectaVision/
├── apps/
│   ├── api/                    # Express.js REST API
│   │   ├── src/
│   │   │   ├── routes/         # Auth, CRM, Inspections, Contracts
│   │   │   ├── middleware/     # JWT authentication
│   │   │   └── utils/          # PDF generation, auth helpers
│   │   └── package.json
│   │
│   ├── web/                    # React CRM Dashboard
│   │   ├── src/
│   │   │   ├── pages/          # Login, Dashboard, InspectionDetail
│   │   │   └── components/     # Shared UI components
│   │   └── package.json
│   │
│   ├── android/                # Native Android App (NEW)
│   │   ├── app/
│   │   │   ├── src/main/
│   │   │   │   ├── java/       # Kotlin source + JNI bridge
│   │   │   │   ├── cpp/        # llama.cpp + native code
│   │   │   │   └── res/        # Android resources
│   │   │   └── build.gradle.kts
│   │   └── README.md
│   │
│   └── mobile/                 # React Native (Legacy)
│       ├── src/
│       └── package.json
│
├── packages/
│   ├── ai/                     # AI Engine
│   │   ├── src/
│   │   │   ├── vision.ts       # Gemini Vision integration
│   │   │   ├── embeddings.ts   # Text embeddings
│   │   │   ├── vector_store.ts # ChromaDB wrapper
│   │   │   └── scraper.ts      # InterNACHI SOP scraper
│   │   └── package.json
│   │
│   ├── database/               # Database Layer
│   │   ├── src/
│   │   │   ├── schema.ts       # Drizzle schema
│   │   │   └── db.ts           # Connection management
│   │   └── package.json
│   │
│   └── shared/                 # Shared Types & Schemas
│       ├── src/
│       └── package.json
│
├── .github/
│   └── workflows/
│       ├── deploy.yml          # Web deployment (GitHub Pages)
│       └── android-build.yml   # Android CI/CD (NEW)
│
├── CODE_AUDIT_2026-03-14.md    # Comprehensive code audit
├── package.json                # Root package (workspaces)
├── turbo.json                  # Turborepo config
└── tsconfig.json               # TypeScript config
```

## 🚀 Getting Started

### Prerequisites

| Requirement | Version | Required For |
|-------------|---------|--------------|
| Node.js | 18+ | Web, API, AI packages |
| npm | 11.9+ | Package management |
| Android Studio | Hedgehog+ | Android app development |
| Android NDK | 26.1+ | Native llama.cpp build |
| Google Gemini API Key | - | Cloud AI features (optional) |
| ChromaDB | Latest | Vector search (optional) |

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/InspectaVisionAI/InspectaVision.git
cd InspectaVision
git submodule update --init --recursive  # For llama.cpp
```

#### 2. Install Dependencies

```bash
npm install
```

#### 3. Environment Setup

Create `apps/api/.env`:

```env
PORT=4000
DATABASE_URL=inspectavision.db
JWT_SECRET=your_super_secret_key_change_this
GEMINI_API_KEY=your_gemini_api_key  # Optional for cloud AI
CHROMA_URL=http://localhost:8000
```

#### 4. Set Up InterNACHI RAG (Optional)

```bash
# Scrape InterNACHI Standards of Practice
npm run scrape -w packages/ai

# Ingest into ChromaDB
npm run ingest -w packages/ai
```

#### 5. Download GGUF Models (For Android)

Place GGUF models in one of these locations:
```
/sdcard/models/
/sdcard/Download/models/
```

Recommended models:
- `Llama-3.2-1B-Instruct-Q4_K_M.gguf` (~1GB)
- `Phi-3-mini-4k-instruct-Q4_K_M.gguf` (~2.5GB)

### Development

```bash
# Start all services (Web + API)
npm run dev

# Start specific services
npm run dev -w apps/api      # API only
npm run dev -w apps/web      # Web only

# Android development
npm run android:dev          # Build debug APK
npm run android:install      # Install on device

# Build for production
npm run build                # Build all
npm run build -w apps/android  # Android release
```

### URLs

| Service | URL | Description |
|---------|-----|-------------|
| Web App | http://localhost:3000 | CRM Dashboard |
| API | http://localhost:4000 | REST API |
| API Health | http://localhost:4000/health | Health check |

## 📱 Android App

The Android app provides native, offline-first AI inspection analysis.

### Quick Start

1. **Build the APK:**
   ```bash
   cd apps/android
   ./gradlew assembleDebug
   ```

2. **Install on device:**
   ```bash
   ./gradlew installDebug
   ```

3. **Load a model:**
   - Open app → Tap folder icon
   - Navigate to your GGUF model
   - Tap "Load Model"

4. **Analyze photos:**
   - Tap "New Inspection"
   - Capture or select photo
   - Choose category
   - Tap "Run AI Analysis"

### CI/CD

The Android app automatically builds on push to `main`:

- **Debug APK**: Uploaded as artifact (30-day retention)
- **Release APK**: Created as GitHub Release (on tag push)
- **SSH Deployment**: Optional deployment to remote server

See [apps/android/README.md](apps/android/README.md) for detailed documentation.

## 🔒 Security

### Authentication
- JWT-based authentication with configurable expiry
- bcrypt password hashing (10 salt rounds)
- Role-based access control (admin, inspector, client)

### Data Protection
- Multi-tenant database isolation
- Environment variable configuration
- CORS and Helmet security headers

### Android Permissions
- Camera: Photo capture for inspections
- Storage: Model file access and photo saving
- Internet: Optional cloud sync

## 🧪 Testing

```bash
# Run all tests
npm run test

# Run specific package tests
npm run test -w packages/ai
npm run test -w apps/api
```

## 📊 Deployment

### Web App (GitHub Pages)

Automatically deploys on push to `main`:

```bash
git push origin main
```

### API Server

```bash
# Docker (recommended)
docker build -f Dockerfile.api -t inspectavision-api .
docker run -p 4000:4000 --env-file .env inspectavision-api

# Direct
cd apps/api
npm run build
npm run start
```

### Android App

```bash
# Manual
cd apps/android
./gradlew assembleRelease

# CI/CD (automatic on tag)
git tag v1.0.0
git push origin v1.0.0
```

## 📄 Documentation

- [Code Audit Report](CODE_AUDIT_2026-03-14.md) - Comprehensive code review
- [Android README](apps/android/README.md) - Android app documentation
- [SSH Deployment Guide](apps/android/SSH_DEPLOYMENT.md) - SSH setup instructions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is proprietary software. All rights reserved.

## 🙏 Acknowledgments

- [llama.cpp](https://github.com/ggerganov/llama.cpp) - High-performance LLM inference
- [InterNACHI](https://www.nachi.org/) - Standards of Practice
- [Google Gemini](https://ai.google.dev/) - Cloud AI capabilities
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI

---

**Built with ❤️ for professional home inspectors**
