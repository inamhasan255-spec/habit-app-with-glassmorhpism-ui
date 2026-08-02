# FocusCube 🧊

> **Lumina — Focus, refined.** A premium aesthetic productivity suite featuring glassmorphism, floating ambient soundscapes, fluid focus ring timers, interactive task flows, and server-side Gemini intelligence.

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-latest-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

---

## 📖 Table of Contents

- [Overview](#overview)
- [✨ Key Features](#-key-features)
- [🎨 Design Philosophy](#-design-philosophy)
- [🛠️ Tech Stack](#️-tech-stack)
- [📁 Project Structure](#-project-structure)
- [🚀 Getting Started](#-getting-started)
- [⚙️ Configuration](#️-configuration)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

---

## Overview

**FocusCube** is a comprehensive productivity and academic tracking Android application designed to help you stay focused, build positive habits, track your academic progress, and connect with a community for accountability. Built with modern Android technologies, it combines powerful functionality with stunning visual design.

---

## ✨ Key Features

### 🎯 Focus & Productivity
- **Focus Timer**: Deep work and Pomodoro timer modes with customizable sessions
- **Fluid Focus Ring**: Beautiful animated ring timer for visual progress tracking
- **Focus Shield**: Advanced app blocker to eliminate distractions during focus sessions
- **Hardened Blocker Service**: Robust distraction prevention with overlay protection

### 📈 Habit Building
- **Habit Tracker**: Build and maintain positive habits with streak tracking
- **Visual Progress**: Glassmorphic cards showing habit completion and statistics
- **Gamification**: Achievement system and leaderboards for motivation

### 📚 Academic Suite
- **Timetable Manager**: Organize your class schedule with intelligent time cushioning
- **Subject Tracking**: Monitor progress across multiple subjects
- **Assignment Management**: Track deadlines and task completion
- **Flashcards**: Study tool with spaced repetition support

### 🌟 Premium UI/UX
- **Glassmorphism Design**: Stunning glass-effect cards, buttons, and containers
- **Galaxy Background**: Immersive animated space-themed backgrounds
- **Liquid Notifications**: Unique notification style for better engagement
- **Confetti Celebrations**: Reward animations for completed tasks and milestones
- **Dark/Light Themes**: Seamless theme switching with persistent preferences

### 🔮 AI-Powered Features
- **Server-Side Gemini API**: Intelligent academic and productivity assistance
- **AI Chat**: Interactive chatbot for study help and motivation
- **Voice Notes**: Quick capture thoughts with voice-to-text
- **Image Analysis**: Extract text and insights from images

### 📊 Analytics & Insights
- **Detailed Statistics**: Comprehensive focus time and productivity metrics
- **Progress Tracking**: Visual charts and graphs showing improvement over time
- **Widget Support**: Home screen widgets for quick glance at productivity

### 🔔 Additional Features
- **Journal & Reading Log**: Track mood, gratitude, and reading progress
- **Calendar Sync**: Integrate with Google Calendar for seamless scheduling
- **Ambient Soundscapes**: Floating background sounds for enhanced focus
- **Stereo Binaural Engine**: 3D audio experiences for deep concentration
- **Community Groups**: Join accountability groups and set shared goals

---

## 🎨 Design Philosophy

FocusCube embraces the **Lumina** design language, prioritizing:

- **Aesthetic Excellence**: Every pixel is crafted with care
- **Smooth Animations**: Fluid transitions and micro-interactions
- **Depth & Dimension**: Layered glassmorphic elements creating visual hierarchy
- **Accessibility**: High contrast modes and readable typography
- **Performance**: Optimized rendering even with complex visual effects

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Dependency Injection** | Hilt / Manual DI |
| **Local Database** | Room Persistence Library |
| **Data Store** | Jetpack DataStore |
| **Async Programming** | Kotlin Coroutines & Flow |
| **Navigation** | Navigation Compose |
| **AI Integration** | Google Gemini API |
| **Cloud Services** | Firebase |
| **Build System** | Gradle Kotlin DSL |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 (Android 16) |

### Key Libraries
- **Room**: Local database persistence
- **KSP**: Kotlin Symbol Processing for annotations
- **Roborazzi**: Screenshot testing
- **Firebase**: Authentication and cloud services
- **Google Services**: Push notifications and analytics

---

## 📁 Project Structure

```
FocusCube/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/focuscube/
│   │   │   │   ├── data/              # Data layer
│   │   │   │   │   ├── local/         # Room DAOs & Database
│   │   │   │   │   ├── model/         # Data models & entities
│   │   │   │   │   └── repository/    # Repository implementations
│   │   │   │   ├── domain/            # Business logic layer
│   │   │   │   │   └── usecase/       # Use cases
│   │   │   │   ├── ui/                # Presentation layer
│   │   │   │   │   ├── screens/       # App screens
│   │   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   │   ├── dashboard/     # Dashboard-specific UI
│   │   │   │   │   └── theme/         # App theming
│   │   │   │   ├── audio/             # Audio playback & soundscapes
│   │   │   │   └── service/           # Background services
│   │   │   └── res/                   # Android resources
│   │   ├── test/                      # Unit tests
│   │   └── androidTest/               # Instrumentation tests
│   └── build.gradle.kts
├── assets/                            # Static assets
├── gradle/                            # Gradle wrapper
├── build.gradle.kts                   # Root build configuration
├── settings.gradle.kts                # Project settings
└── README.md                          # This file
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Latest stable version recommended
- **JDK**: Version 17 or higher
- **Android SDK**: API level 24+
- **Git**: For cloning the repository

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/FocusCube.git
   cd FocusCube
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync the project
   - Wait for all dependencies to download

4. **Configure API Keys** (if needed)
   - Copy `.env.example` to `.env`
   - Add your Gemini API key and other credentials

5. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift + F10`

### Building Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated in `app/build/outputs/apk/release/`

---

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the root directory with the following:

```env
GEMINI_API_KEY=your_api_key_here
FIREBASE_CONFIG=your_firebase_config
KEYSTORE_PATH=/path/to/keystore.jks
STORE_PASSWORD=your_store_password
KEY_PASSWORD=your_key_password
```

### Google Services Setup

For Firebase features:

1. Download `google-services.json` from Firebase Console
2. Place it in the `app/` directory
3. Enable required Firebase services (Auth, Firestore, etc.)

Or run the setup script:

```bash
# Linux/Mac
./setup-google-services.sh

# Windows
setup-google-services.bat
```

---

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### How to Contribute

1. **Fork the Project**
   ```bash
   git fork https://github.com/yourusername/FocusCube.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```

3. **Make Your Changes**
   - Follow the existing code style
   - Write tests for new features
   - Update documentation as needed

4. **Commit Your Changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```

5. **Push to the Branch**
   ```bash
   git push origin feature/AmazingFeature
   ```

6. **Open a Pull Request**
   - Navigate to the original repository
   - Create a new pull request
   - Describe your changes thoroughly

### Code Style Guidelines

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused
- Write unit tests for business logic

### Reporting Issues

- Use the GitHub issue tracker
- Provide detailed steps to reproduce bugs
- Include screenshots when applicable
- Suggest potential solutions if possible

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
Copyright (c) 2024 FocusCube

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## (●'◡'●) Acknowledgments

- [JetBrains](https://www.jetbrains.com/) for Kotlin
- [Google](https://developer.android.com/) for Android and Jetpack Compose
- [Google DeepMind](https://deepmind.google/) for Gemini AI
- All contributors and supporters of this project

---

<div align="center">

**Made with ❤️ using Kotlin & Jetpack Compose**

[Report Bug](https://github.com/yourusername/FocusCube/issues) · [Request Feature](https://github.com/yourusername/FocusCube/issues)

</div>
