# EchoPal Application Requirements

This document outlines how EchoPal meets the core requirements for your final project.

## 1. Screens/Pages
EchoPal will feature at least three main screens:

- **Home Screen**
  - Quick start, onboarding, and navigation to practice or live coaching.
- **Live Coaching Screen**
  - Real-time speech-to-text transcription.
  - AI-powered feedback and suggestions (on-screen tips, vibration cues).
- **Results/Breakdown Screen**
  - Post-conversation analytics (metrics, positivity, interruptions, etc.).
  - Personalized improvement suggestions and scenario practice options.
- *(Optional for extra credit)* **Profile/Login/Settings Screen**
  - User authentication, API key management, privacy settings, and cloud sync.

## 2. Data Persistence
- **Local Storage:**
  - Conversation history and analytics are stored locally using Room or DataStore.
  - User preferences (e.g., scenario selection, privacy options) are persisted.
- **Optional Cloud Sync:**
  - Users can opt-in to sync progress for long-term tracking.

## 3. Mobile-First UX
- Designed with Jetpack Compose for responsive layouts and intuitive navigation.
- Large tap targets, accessible colors, and smooth transitions.
- Real-time feedback is delivered with minimal distraction (vibration/on-screen cues).

## 4. Authentication (Optional for Extra Credit)
- Basic login (email/password or Google Sign-In) to enable cloud sync and personalized scenarios.
- Secure handling of user data and API keys.

---

**Summary:**
EchoPal is structured to deliver a mobile-first, AI-powered conversation coaching experience, with persistent progress tracking and a clear, accessible interface. The app’s modular design allows for future expansion (more practice scenarios, advanced analytics, or B2B features).
