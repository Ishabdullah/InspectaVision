# InspectaVision Android - Offline AI Home Inspection

**100% Offline - No Server Required**

InspectaVision Android is a completely standalone home inspection app that runs AI analysis directly on your device using llama.cpp. No internet connection, no API keys, no servers - just install and use.

## 📱 Features

- **100% Offline**: Everything runs on your device using llama.cpp
- **GGUF Model Support**: Load any GGUF format model (Llama, Phi, Qwen, etc.)
- **Camera Integration**: Capture photos for AI analysis
- **Streaming Responses**: Real-time token generation
- **Findings Management**: Save and organize inspection results
- **Dark/Light Theme**: Material 3 design

## 📥 Installation

### Download APK

Get the latest APK from [GitHub Releases](https://github.com/Ishabdullah/InspectaVision/releases) or build it yourself.

### Install

```bash
# Enable "Install from Unknown Sources" in Android settings
# Then install the APK
adb install app-release.apk
```

## 📂 Setting Up Models

### Step 1: Download a GGUF Model

Download a GGUF model from Hugging Face:

**Recommended Models (for most phones):**
| Model | Size | RAM Needed | Download |
|-------|------|------------|----------|
| Llama-3.2-1B-Instruct Q4_K_M | ~1.3 GB | 2+ GB | [Download](https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF) |
| Phi-3-mini-4k-instruct Q4_K_M | ~2.4 GB | 4+ GB | [Download](https://huggingface.co/bartowski/Phi-3-mini-4k-instruct-GGUF) |
| Qwen2.5-1.5B-Instruct Q4_K_M | ~1.8 GB | 3+ GB | [Download](https://huggingface.co/bartowski/Qwen2.5-1.5B-Instruct-GGUF) |

**For powerful phones (8+ GB RAM):**
| Model | Size | RAM Needed | Download |
|-------|------|------------|----------|
| Llama-3.2-3B-Instruct Q4_K_M | ~2.8 GB | 6+ GB | [Download](https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF) |
| Mistral-7B-Instruct Q4_K_M | ~4.4 GB | 8+ GB | [Download](https://huggingface.co/bartowski/Mistral-7B-Instruct-v0.3-GGUF) |

### Step 2: Place Model on Your Phone

**Option A: Using USB Cable**
1. Connect your phone to computer
2. Copy the `.gguf` file to:
   - `/sdcard/Download/` (easiest)
   - `/sdcard/models/` (create this folder)

**Option B: Download Directly on Phone**
1. Open browser on your phone
2. Download the GGUF file directly
3. It will save to Downloads folder

### Step 3: Load Model in App

1. Open InspectaVision app
2. Tap the folder icon (top right)
3. Your model should appear automatically
4. Tap on the model to select it
5. Tap "Load Model"
6. Wait 10-30 seconds for loading

## 📸 Using the App

### Analyze a Photo

1. **Home Screen** → Tap "New Inspection"
2. **Camera** → Take a photo of the area to inspect
3. **Select Category** → Choose: Roof, Electrical, Plumbing, etc.
4. **Run AI Analysis** → Tap the button
5. **Review Results** → AI will describe issues and recommendations
6. **Save Finding** → Tap save to add to your findings list

### View Saved Findings

1. **Home Screen** → Tap "Saved Findings"
2. See all your inspection findings organized by severity
3. Filter by: Safety, Major, Minor, Maintenance
4. Export or share findings

### Settings

Access Settings to adjust:
- **Context Size**: More = better understanding, uses more RAM
- **CPU Threads**: More = faster, but more battery
- **Temperature**: Higher = more creative, Lower = more focused
- **Max Tokens**: Limit response length

## 🔧 Building from Source

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android NDK 26.1+
- Android SDK API 35

### Build Steps

```bash
# Clone the repository
git clone https://github.com/Ishabdullah/InspectaVision.git
cd InspectaVision

# Initialize llama.cpp submodule
git submodule update --init --recursive

# Or run the setup script
cd apps/android
./setup-android.sh

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### APK Location

After building:
- **Debug**: `apps/android/app/build/outputs/apk/debug/app-debug.apk`
- **Release**: `apps/android/app/build/outputs/apk/release/app-release.apk`

## 📊 Performance

### Expected Speed (tokens/second)

| Phone | Snapdragon 8 Gen 2 | Snapdragon 865 | Snapdragon 778G |
|-------|-------------------|----------------|-----------------|
| **1B Q4** | ~30 tok/s | ~15 tok/s | ~10 tok/s |
| **3B Q4** | ~15 tok/s | ~7 tok/s | ~4 tok/s |
| **7B Q4** | ~6 tok/s | ~3 tok/s | ~2 tok/s |

### RAM Requirements

| Model Size | Minimum RAM | Recommended RAM |
|------------|-------------|-----------------|
| 1B Q4 | 2 GB | 4 GB |
| 3B Q4 | 4 GB | 6 GB |
| 7B Q4 | 8 GB | 12 GB |

## 🛠️ Troubleshooting

### "Model Fails to Load"

**Problem**: App shows error when loading model

**Solutions:**
1. Check your phone has enough RAM (need 2x model size)
2. Try a smaller model (1B instead of 7B)
3. Close other apps to free memory
4. Restart your phone and try again

### "No GGUF Models Found"

**Problem**: Model selector shows empty screen

**Solutions:**
1. Verify file ends with `.gguf` extension
2. Check file is in Downloads folder or /sdcard/models/
3. Tap "Refresh" button (top right)
4. Use "Browse Custom Location" to find your file

### "Analysis is Slow"

**Problem**: AI generates tokens slowly

**Solutions:**
1. Use a smaller model (1B or 3B)
2. Reduce context size in Settings
3. Close background apps
4. Use Q4_K_M quantization (faster than Q8)

### "App Crashes"

**Problem**: App closes unexpectedly

**Solutions:**
1. Clear app data in Android settings
2. Reinstall the app
3. Try a different GGUF model
4. Check logcat for error details

## 📁 File Locations

| Type | Location |
|------|----------|
| Models (primary) | `/sdcard/Download/` |
| Models (secondary) | `/sdcard/models/` |
| App data | `/sdcard/Android/data/com.inspectavision.app/` |
| Saved findings | Internal database (Room) |

## 🔒 Privacy

- **No internet required** - Everything runs locally
- **No data collection** - Your inspections stay on your device
- **No API keys** - No accounts, no subscriptions
- **Open source** - Code is auditable on GitHub

## 📄 License

This project is part of InspectaVision. See main repository for licensing.

## 🙏 Acknowledgments

- [llama.cpp](https://github.com/ggerganov/llama.cpp) - On-device LLM inference
- [Hugging Face](https://huggingface.co/) - GGUF model hosting

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Ishabdullah/InspectaVision/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Ishabdullah/InspectaVision/discussions)

---

**Built with ❤️ for offline-first AI inspection**
