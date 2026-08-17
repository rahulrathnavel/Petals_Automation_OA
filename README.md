<div align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/ONNX-005CED?style=for-the-badge&logo=onnx&logoColor=white" />
  <img src="https://img.shields.io/badge/YOLO-00FFFF?style=for-the-badge&logo=yolo&logoColor=black" />
  <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" />
</div>

<h1 align="center">Offline Edge Car Detector (Android)</h1>

<p align="center">
  A minimal, ultra-fast, and <strong>100% offline</strong> Android application for detecting cars using an edge-optimized YOLO26 nano model via ONNX Runtime.
</p>

---

##  Features

- **Completely Offline**: No network calls, no cloud API, no `INTERNET` permissions. Privacy-first by design.
- **YOLO26 Nano Model**: Leverages the official `yolo26n` end-to-end NMS-free object detection model.
- **ONNX Runtime**: Hardware-agnostic edge inference using `ONNXRuntime-Android` for rapid execution on standard devices.
- **Smart Preprocessing**: Preserves original image aspect ratios through intelligent padding/letterboxing (640x640), preventing distortion.
- **Ultra-Minimal UI**: No fluff. No login, no database, no complex settings. Just upload and detect.

##  Tech Stack

- **Framework:** Android SDK (API 24 - 34)
- **Language:** Kotlin
- **Inference Engine:** ONNX Runtime (`com.microsoft.onnxruntime:onnxruntime-android`)
- **Machine Learning:** Ultralytics YOLO26, Python, OpenCV (for model export and analysis)
- **Build System:** Gradle 8.7

##  Download APK

The compiled Android application is available directly in this repository:
 **[Download OfflineCarDetector.apk](./OfflineCarDetector.apk)**

## 🚀 Quick Start Guide

### Prerequisites
- An Android device running Android 7.0 (API 24) or higher.
- (For building from source) JDK 17 and Android SDK.

### Installation & Testing
1. Download the `OfflineCarDetector.apk` from the root of this repository.
2. Transfer the APK to your Android device.
3. Install the APK. You may need to enable **"Install from Unknown Sources"** in your device settings.
4. Turn on **Airplane Mode** to verify the application is completely offline.
5. Launch the app and tap **Upload Image** to select a photo from your gallery.
6. The app will immediately draw bounding boxes and confidence scores over any detected cars.

### Building from Source
To compile the codebase manually:
```bash
# Clone the repository
git clone https://github.com/rahulrathnavel/Petals_Automation_OA.git
cd Petals_Automation_OA

# Build using Gradle wrapper
./gradlew assembleDebug
```
The newly compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

##  Workflow & Architecture

1. **Model Export**: 
   - We utilized the Ultralytics Python library to load `yolo26n.pt`.
   - Exported the model to an intermediate `.onnx` graph targeting an input dimension of `[1, 3, 640, 640]`.
   - Analyzed the NMS-free end-to-end YOLO26 detection head output shape `[1, 300, 6]` mapping to `[xmin, ymin, xmax, ymax, confidence, class_id]`.
2. **Android Integration**:
   - The `.onnx` model was bundled into `app/src/main/assets/`.
   - Android's `Bitmap` API handles image picking and dynamic letterbox scaling.
   - Pixels are normalized to `RGB 0.0-1.0` float arrays and piped into the ONNX execution provider.
   - Custom coordinate mapping transforms the 640x640 bounding box results accurately back to the original image coordinates via the inverse scale and padding offsets.
   - Results are rendered in real-time onto an Android Canvas.

---
*Developed for edge-device optimization and seamless offline car detection.*
