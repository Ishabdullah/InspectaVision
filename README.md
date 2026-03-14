# InspectaVision - AI-Powered Home Inspection Platform

[![Android Build](https://github.com/Ishabdullah/InspectaVision/actions/workflows/android-build.yml/badge.svg)](https://github.com/Ishabdullah/InspectaVision/actions/workflows/android-build.yml)
[![Code Audit](CODE_AUDIT_2026-03-14.md)](CODE_AUDIT_2026-03-14.md)

**📱 NEW: Standalone Android App - 100% Offline, No Server Required!**

InspectaVision is an AI-powered home inspection platform. The **Android app runs entirely on your device** using llama.cpp for local LLM inference - no internet, no API keys, no servers needed.

## 🚀 Quick Start - Android App

### 1. Download & Install

```bash
# Clone and build
git clone https://github.com/Ishabdullah/InspectaVision.git
cd InspectaVision/apps/android
./gradlew assembleDebug
```

Or download APK from [Releases](https://github.com/Ishabdullah/InspectaVision/releases)

### 2. Get a GGUF Model

Download from Hugging Face:
- [Llama-3.2-1B-Instruct-GGUF](https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF) (~1.3 GB)
- [Phi-3-mini-4k-instruct-GGUF](https://huggingface.co/bartowski/Phi-3-mini-4k-instruct-GGUF) (~2.4 GB)

### 3. Place Model on Phone

Copy `.gguf` file to:
- `/sdcard/Download/` or
- `/sdcard/models/`

### 4. Load & Analyze

1. Open app → Tap folder icon
2. Select your GGUF model
3. Tap "Load Model"
4. Capture photo → Run AI analysis

**That's it! Everything works offline.**

## 📦 Project Structure

```
InspectaVision/
├── apps/
│   ├── android/          # 📱 STANDALONE ANDROID APP (llama.cpp)
│   ├── web/              # React CRM (optional)
│   └── api/              # Node.js API (optional)
├── packages/
│   ├── ai/               # AI utilities
│   ├── database/         # Database schema
│   └── shared/           # Shared types
└── .github/workflows/
    └── android-build.yml # CI/CD for Android
```

## 📱 Android App Features

| Feature | Description |
|---------|-------------|
| **100% Offline** | No internet required, all processing on-device |
| **GGUF Support** | Load any GGUF model (Llama, Phi, Qwen, Mistral) |
| **Camera** | Capture inspection photos in-app |
| **AI Analysis** | Streaming LLM responses via llama.cpp |
| **Findings** | Save and organize inspection results |
| **Dark Mode** | Material 3 theme |

## 🔧 Build Android App

### Prerequisites

- Android Studio Hedgehog+ OR command-line build tools
- JDK 17
- Android NDK 26.1+
- Android SDK API 35

### Build Commands

```bash
# Setup (first time only)
cd apps/android
./setup-android.sh

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug
```

### APK Output

- Debug: `apps/android/app/build/outputs/apk/debug/app-debug.apk`
- Release: `apps/android/app/build/outputs/apk/release/app-release.apk`

## 🤖 CI/CD - GitHub Actions

The Android app automatically builds on every push to `main`:

```yaml
# .github/workflows/android-build.yml
- Builds native llama.cpp library
- Compiles Android app with Jetpack Compose
- Uploads APK as artifact (30 days)
- Creates GitHub Release on tag push
```

### Manual Build Trigger

1. Go to **Actions** → **Android Build**
2. Click **Run workflow**
3. Select debug/release
4. Download APK from artifacts

## 📊 Model Performance

| Model | Size | RAM | Speed (SD 8 Gen 2) |
|-------|------|-----|-------------------|
| Llama-3.2-1B Q4 | 1.3 GB | 2+ GB | ~30 tok/s |
| Phi-3-mini Q4 | 2.4 GB | 4+ GB | ~15 tok/s |
| Llama-3.2-3B Q4 | 2.8 GB | 6+ GB | ~15 tok/s |
| Mistral-7B Q4 | 4.4 GB | 8+ GB | ~6 tok/s |

## 📄 Documentation

- [Android README](apps/android/README.md) - Full Android app docs
- [Code Audit](CODE_AUDIT_2026-03-14.md) - Comprehensive code review

## 🙏 Acknowledgments

- [llama.cpp](https://github.com/ggerganov/llama.cpp) - On-device LLM inference

---

**Built with ❤️ for offline-first AI inspection**
