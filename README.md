#  Real-Time BPM Detector

**A cross-platform mobile app (iOS + Android)** that performs **real-time tempo (BPM) detection** from live audio input and visualizes tempo similar to *LiveBPM*.

---

## Overview

The app continuously listens to microphone input, analyzes beats and rhythm, and displays the current tempo visually in real time.

Built natively for:
- **iOS (Swift + AVAudioEngine)**
- **Android (Kotlin + AudioRecord)**
- with a **shared C++ core** (`/core`) for accurate, consistent BPM detection using [aubio](https://aubio.org/).

---



https://github.com/user-attachments/assets/43482cec-ac55-4def-ae46-67d227951f82



## 🧩 Architecture

Mic Input
↓
Audio Processing (C++ aubio-based DSP core)
↓
Tempo / Beat Detection
↓
Native Layer (Swift / Kotlin)
↓
UI Visualization (SwiftUI / Jetpack Compose)


Each platform project uses the shared C++ code for real-time audio processing while rendering native visualizations for BPM and beat pulses.

---

## 🛠️ Setup

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/realtime-bpm.git
cd realtime-bpm

git submodule update --init --recursive

cd ios
open BPMDetectorApp.xcodeproj
# build and run from Xcode

cd android
./gradlew assembleDebug
# or open in Android Studio
