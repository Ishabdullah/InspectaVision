# InspectaVision Android App

AI-powered home inspection Android application with on-device LLM inference using llama.cpp and GGUF models.

## Features

- **Offline AI Analysis**: Run inspection analysis completely offline using local GGUF models
- **Camera Integration**: Capture photos directly within the app for analysis
- **Streaming Responses**: Real-time token streaming during AI generation
- **Model Selection**: Load any GGUF model from your device storage
- **Findings Management**: Save and organize inspection findings
- **Dark Mode**: Full dark/light theme support

## Architecture

```
apps/android/
├── app/
│   ├── src/main/
│   │   ├── java/com/inspectavision/
│   │   │   ├── llm/
│   │   │   │   └── LlamaCppBridge.kt      # JNI bridge to llama.cpp
│   │   │   ├── ui/
│   │   │   │   ├── navigation/            # Compose Navigation
│   │   │   │   ├── screens/               # App screens
│   │   │   │   └── theme/                 # Material 3 theme
│   │   │   ├── MainActivity.kt
│   │   │   └── InspectaVisionApplication.kt
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt             # Native build config
│   │   │   ├── jni_bridge.cpp             # JNI implementation
│   │   │   └── llama.cpp/                 # llama.cpp submodule
│   │   ├── AndroidManifest.xml
│   │   └── res/                           # Android resources
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── wrapper/
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **NDK**: 26.1.10909125
- **CMake**: 3.22.1
- **JDK**: 17
- **Android SDK**: API 35 (target), API 26 (minimum)

## Getting GGUF Models

Download GGUF format models from Hugging Face or other sources:

**Recommended models for mobile:**
- [Llama-3.2-1B-Instruct-GGUF](https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF)
- [Llama-3.2-3B-Instruct-GGUF](https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF)
- [Phi-3-mini-4k-instruct-GGUF](https://huggingface.co/bartowski/Phi-3-mini-4k-instruct-GGUF)
- [Qwen2.5-1.5B-Instruct-GGUF](https://huggingface.co/bartowski/Qwen2.5-1.5B-Instruct-GGUF)

**Model placement:**
```
/sdcard/models/          # Primary location
/sdcard/Download/models/ # Alternative location
```

## Building

### Local Build

```bash
# Navigate to Android directory
cd apps/android

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

### From Root

```bash
# Using npm scripts
npm run android:dev      # Build debug
npm run android:build    # Build release
npm run android:clean    # Clean
npm run android:install  # Install on device
```

### Build Configuration

Edit `gradle.properties` to customize:

```properties
# Memory settings
org.gradle.jvmargs=-Xmx4096m

# NDK version
android.ndk.version=26.1.10909125

# CMake version
android.cmake.version=3.22.1
```

## GitHub Actions Build

The app automatically builds on push to `main` branch.

### Manual Trigger

1. Go to Actions → Android Build
2. Click "Run workflow"
3. Select build type (debug/release)
4. Optionally enable SSH deployment

### Required Secrets for Release Build

```bash
# In GitHub Repository Settings → Secrets → Actions
RELEASE_KEYSTORE           # Base64 encoded .jks file
RELEASE_KEYSTORE_PASSWORD  # Keystore password
RELEASE_KEY_ALIAS          # Key alias
RELEASE_KEY_PASSWORD       # Key password
```

### SSH Deployment Setup

See [SSH_DEPLOYMENT.md](SSH_DEPLOYMENT.md) for detailed setup instructions.

Required secrets:
- `SSH_HOST` - Deployment server hostname
- `SSH_USER` - SSH username
- `SSH_PRIVATE_KEY` - Private key for authentication

## Native Library Build

The llama.cpp native library is built automatically during the Android build process.

### Manual Native Build

```bash
cd apps/android/app/src/main/cpp

# Clone llama.cpp if not present
git clone --depth 1 --branch b4426 https://github.com/ggerganov/llama.cpp.git

# Build with CMake
mkdir build && cd build
cmake .. -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
         -DANDROID_ABI=arm64-v8a \
         -DANDROID_PLATFORM=android-26 \
         -DGGML_METAL=OFF \
         -DGGML_OPENMP=OFF
cmake --build .
```

### Supported ABIs

- `arm64-v8a` (primary target)
- `armeabi-v7a` (legacy devices)
- `x86_64` (emulators)

## Usage

### First Launch

1. Open the app
2. Navigate to Model Selector (folder icon)
3. Select a GGUF model from your device
4. Wait for model to load (may take 10-30 seconds)

### Analyzing Photos

1. From Home, tap "New Inspection"
2. Capture or select a photo
3. Choose the inspection category (Roof, Electrical, etc.)
4. Tap "Run AI Analysis"
5. Review the streaming analysis results
6. Save findings for your report

### Settings

Access Settings to configure:
- Context size (memory vs. capability tradeoff)
- CPU threads (performance tuning)
- Temperature (creativity vs. determinism)
- Max tokens (response length)

## Performance Guidelines

### Memory Usage

| Model Size | RAM Required | Recommended Devices |
|------------|--------------|---------------------|
| 1B Q4_K_M  | ~1 GB        | All devices         |
| 3B Q4_K_M  | ~2.5 GB      | Mid-range+          |
| 7B Q4_K_M  | ~5 GB        | High-end only       |

### Inference Speed

Expected tokens/second on different hardware:

| Device | Snapdragon 8 Gen 2 | Snapdragon 865 | Emulator |
|--------|-------------------|----------------|----------|
| 1B Q4  | ~30 tok/s         | ~15 tok/s      | ~5 tok/s |
| 3B Q4  | ~15 tok/s         | ~7 tok/s       | ~2 tok/s |
| 7B Q4  | ~6 tok/s          | ~3 tok/s       | ~1 tok/s |

## Troubleshooting

### Model Fails to Load

```
Error: Failed to load model
```

**Solutions:**
1. Verify the file is valid GGUF format
2. Check available RAM (need 2x model size)
3. Try a smaller model (1B or 3B)
4. Check logcat for detailed errors

### Slow Inference

**Solutions:**
1. Reduce context size in Settings
2. Use Q4_K_M or Q4_K_S quantization
3. Close background apps
4. Use smaller model

### Build Fails with CMake Error

```
CMake Error: Could not find NDK
```

**Solutions:**
1. Install NDK via Android Studio SDK Manager
2. Set `ANDROID_NDK_HOME` environment variable
3. Verify NDK version in `gradle.properties`

### JNI Crash

```
Fatal signal 11 (SIGSEGV)
```

**Solutions:**
1. Ensure model file is not corrupted
2. Check model path has no special characters
3. Update to latest llama.cpp version

## Project Structure

### Key Files

| File | Purpose |
|------|---------|
| `LlamaCppBridge.kt` | Kotlin wrapper for JNI calls |
| `jni_bridge.cpp` | Native JNI implementation |
| `CMakeLists.txt` | Native library build config |
| `AnalysisScreen.kt` | Main AI analysis UI |
| `ModelSelectorScreen.kt` | Model file picker |

### Dependencies

- **Jetpack Compose**: Modern declarative UI
- **CameraX**: Camera integration
- **Coil**: Image loading
- **Room**: Local database (for findings)
- **Ktor**: HTTP client (for optional cloud sync)
- **DataStore**: Preferences storage

## License

This project is part of InspectaVision. See the main repository for licensing information.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and linting
5. Submit a pull request

## Acknowledgments

- [llama.cpp](https://github.com/ggerganov/llama.cpp) - High-performance LLM inference
- [Google Gemini](https://ai.google.dev/) - Optional cloud-based analysis
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI
