# WattWise — Smart Home Energy Monitor

> FIT5046 Assessment 4 · Lab03, Group 05 · Monash University S1 2026

WattWise is an Android application built with Kotlin and Jetpack Compose that helps households monitor, analyse, and reduce their electricity consumption in real time. It streams live sensor data from a Kaggle CSV dataset, applies an 8-rule context engine to classify the household's energy situation, visualises 30 days of historical usage, and sends proactive budget alerts via WorkManager notifications.

---

## Team

| Name | Student ID | GitLab | Role |
|---|---|---|---|
| Sehjwant Singh | 35728949 | `sehj0001` | Architect & Build Lead |
| Vedika Shivhare | 35445483 | `vshi0005` | Auth & Household System Lead |
| Ria Joshi | 35577584 | `rjos0028` | Screens & TFLite Lead |
| Jai Negi | 35576375 | `jneg0001` | Data Visualisation & API Lead |

**Repository:** https://git.infotech.monash.edu/FIT5046/26S1/A24-Lab03-G05

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Configuration](#setup--configuration)
- [Running the App](#running-the-app)
- [Screens](#screens)
- [Android Components Used](#android-components-used)
- [Context-Aware Computing](#context-aware-computing)
- [Advanced Features](#advanced-features)
- [Known Issues](#known-issues)
- [Permissions](#permissions)

---

## Features

- **Real-time energy monitoring** — streams 1 CSV row every 3 seconds simulating a live smart meter feed
- **Context engine** — 8 rules classify household state as Normal, Warning, or Critical
- **Live weather integration** — fetches outdoor temperature from OpenWeatherMap API via Retrofit
- **TFLite energy forecasting** — on-device next-hour energy prediction using a pre-trained `.tflite` model
- **Energy history charts** — 30-day seeded data visualised across Daily Usage, Breakdown, Carbon, and Trends tabs
- **Appliance management** — full CRUD with real-time Firestore sync across Owner and Member devices
- **Household messaging** — Firestore-backed real-time chat scoped to each household
- **Member approval system** — Owner approves/rejects join requests; Members see pending/approved/removed status in real time
- **WorkManager budget alerts** — fires at 80% and 100% of daily budget goal; visible within 15 seconds for demo
- **Email verification** — Firebase sends a verification link before a new user can sign in
- **Google Sign-In** — one-tap authentication with automatic household creation
- **Carbon footprint tracking** — CO₂ equivalent calculated from daily kWh totals

---

## Architecture

WattWise uses **MVVM** (Model–View–ViewModel) with a single `WattWiseViewModel` shared across all screens via `AndroidViewModel`.

```
UI Layer (Jetpack Compose screens)
        ↕  State / Events
ViewModel Layer (WattWiseViewModel)
        ↕                    ↕
Local Data               Remote Data
Room DB                  Firestore (users, messages, appliances)
  └─ appliances          Firebase Auth
  └─ energy_readings     OpenWeatherMap (Retrofit)
SmartMeterSimulator      WorkManager (EnergyBudgetWorker)
  └─ CSV Flow            TFLite (EnergyForecaster)
ContextEngine
```

Data flows one way: the ViewModel exposes `mutableStateOf` / `mutableStateListOf` properties; Compose screens observe them and re-compose on change. All Firestore listeners are stored as `ListenerRegistration` references and removed on logout to prevent cross-session data leakage.

---

## Tech Stack

| Category | Library / Version |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.09.00), Material3 |
| Navigation | Navigation Compose 2.9.8 |
| Local DB | Room 2.7.0 + KSP 2.0.21-1.0.28 |
| Remote DB | Firebase Firestore 26.0.x |
| Auth | Firebase Auth + Google Sign-In |
| HTTP | Retrofit 2 + Gson (OpenWeatherMap) |
| Background | WorkManager 2.9.0 |
| ML | TensorFlow Lite / LiteRT (energy_forecast.tflite) |
| Build | AGP 9.0.1, Google Services 4.4.3 |
| Min SDK | 24 |
| Target SDK | 35 |

---

## Project Structure

```
app/src/main/
├── java/com/fit5046/wattwise/
│   ├── MainActivity.kt             — app shell, bottom nav, WorkManager scheduling
│   ├── WattWiseViewModel.kt        — all state, Firebase, Room, simulator, ContextEngine
│   │
│   ├── data/
│   │   ├── Appliance.kt            — Room entity
│   │   ├── ApplianceDao.kt         — Room DAO (getAll, insert, update, deleteById, getCount)
│   │   ├── EnergyReading.kt        — Room entity for historical data
│   │   ├── EnergyReadingDao.kt     — aggregate queries (daily totals, category breakdown, hourly averages)
│   │   ├── EnergyReadingRepository.kt
│   │   └── WattWiseDatabase.kt     — Room singleton (version 2)
│   │
│   ├── engine/
│   │   ├── ContextEngine.kt        — 8-rule situation state engine
│   │   └── SmartMeterSimulator.kt  — Kaggle CSV Flow (1 row / 3 s)
│   │
│   ├── forecast/
│   │   └── EnergyForecaster.kt     — TFLite inference + linear regression fallback
│   │
│   ├── weather/
│   │   ├── WeatherApi.kt           — Retrofit interface
│   │   ├── WeatherModels.kt        — OpenWeatherMap response data classes
│   │   └── WeatherRepository.kt
│   │
│   ├── work/
│   │   ├── EnergyBudgetWorker.kt   — WorkManager worker (80%/100% threshold notifications)
│   │   └── WorkManagerScheduler.kt — enqueues periodic + one-time demo trigger
│   │
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── LiveMonitorScreen.kt
│   │   │   ├── HistoryScreen.kt
│   │   │   ├── ApplianceManagerScreen.kt
│   │   │   ├── AddEditApplianceScreen.kt
│   │   │   ├── SearchScreen.kt
│   │   │   ├── MessagingScreen.kt
│   │   │   ├── ProfileScreen.kt
│   │   │   ├── LoginScreen.kt
│   │   │   └── RegisterScreen.kt
│   │   └── theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   │
│   └── NavigationDestination.kt
│
└── assets/
    ├── household_electricity_usage.csv   — Kaggle dataset (10,000 rows, 200 loaded)
    └── energy_forecast.tflite            — pre-trained TFLite model (6-feature input)
```

---

## Prerequisites

- Android Studio Hedgehog or later
- JDK 17+
- Android emulator or physical device running API 24+
- A Firebase project with **Authentication** and **Firestore** enabled
- An **OpenWeatherMap** API key (free tier)
- A **Google Sign-In** OAuth 2.0 Web Client ID from Firebase console

---

## Setup & Configuration

### 1. Clone the repository

```bash
git clone https://git.infotech.monash.edu/FIT5046/26S1/A24-Lab03-G05.git
cd A24-Lab03-G05
```

### 2. Add `google-services.json`

Download `google-services.json` from your Firebase project console and place it at:

```
app/google-services.json
```

This file is excluded from version control (`.gitignore`). Without it the build will fail.

### 3. Configure API keys in `local.properties`

Add the following to your `local.properties` file (never commit this file):

```properties
WEATHER_API_KEY=your_openweathermap_api_key_here
GOOGLE_WEB_CLIENT_ID=your_firebase_google_oauth_web_client_id_here
```

These are injected into `BuildConfig` at compile time via the app-level `build.gradle.kts`.

### 4. Firebase console setup

In your Firebase project:

- **Authentication** → Sign-in methods → Enable **Email/Password** and **Google**
- **Firestore Database** → Create database in production mode
- **Firestore rules** — for development use:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

- **Authentication → Templates** — optionally customise the verification email sender name

### 5. Emulator recommendation

Use an AVD with the **Google Play** system image (API 35 recommended for arm64 stability). The `google_apis` image without Play Store will cause Google Sign-In to fail. After first boot, add a Google account in the emulator's Settings → Accounts.

---

## Running the App

1. Open the project in Android Studio
2. Sync Gradle (`File → Sync Project with Gradle Files`)
3. Select your emulator or connected device
4. Click **Run** (`Shift+F10`)

On first launch the app shows the Login screen. Register as **Owner** to create a new household, or register as **Member** and enter the Owner's household ID to join.

---

## Screens

### Login
Firebase email/password and Google Sign-In. Blocks login if email is unverified (email/password accounts only).

### Register
Create an Owner or Member account. A Firebase verification email is sent immediately after registration — the user must click the link before signing in. Google accounts are auto-verified.

### Home
Dashboard showing: live energy reading, tariff, budget progress bar, ContextEngine state card (Normal/Warning/Critical), weather card (outdoor temp via OpenWeatherMap), next-hour TFLite forecast card, occupancy, room temperature, CO₂ estimate, and a smart tip. Household ID shortcut to Messaging.

### Live Monitor
Real-time feed of CSV rows as they stream in (1 row / 3 seconds). Displays energy_kwh, room_temp_c, occupancy_count, tariff_per_kwh, appliance name, and category with colour dots. Scrollable LazyColumn with column headers.

### History
Four tabs backed by Room queries on 30 days of seeded `energy_readings`:
- **Daily Usage** — bar chart of kWh per day with date filter (DatePicker)
- **Breakdown** — category pie/breakdown chart
- **Carbon** — CO₂ equivalent line chart
- **Trends** — hourly averages line chart with peak and low indicators

### Appliance Manager
Owner: full CRUD — add, edit, delete. Member: read-only view. All changes sync to Firestore in real time so both Owner and Member devices stay in sync. Uses a Firestore snapshot listener with full reconciliation (removes seeded defaults, upserts Firestore state).

### Add / Edit Appliance
Form screen with name, category dropdown (ExposedDropdownMenu), wattage field with validation, notes, and unsaved-changes dialog on back navigation.

### Search
Full-text search across appliances by name and category. Empty state and no-results handling.

### Messaging
Firestore-backed real-time household chat. Messages scoped strictly to `households/{householdId}/messages` — users in different households cannot see each other's messages. Listener is removed on logout.

### Profile
Account info form (full name, suburb, budget goal, billing type), completion progress ring, delete account option. Owner only can edit suburb. Budget goal triggers WorkManager re-scheduling. Pending members list (Owner only) to approve or reject join requests.

---

## Android Components Used

### Core 6
| Component | Where used |
|---|---|
| Room Database | Appliance CRUD + 30-day energy readings |
| Bottom Navigation Bar | 5-tab navigation in MainActivity |
| Retrofit | OpenWeatherMap weather API |
| DatePicker | History screen date range filter |
| ExposedDropdownMenu | Category selector in AddEditApplianceScreen |
| LazyColumn | LiveMonitor feed, Messaging list, Appliance list |

### Additional 4
| Component | Where used |
|---|---|
| Switch | Notification toggle in ProfileScreen |
| LinearProgressIndicator | Budget progress bar on HomeScreen |
| TimePicker | Peak hours setting in ProfileScreen |
| RadioButton | Billing type selector in ProfileScreen |

---

## Context-Aware Computing

`ContextEngine.kt` aggregates 8 inputs and applies 8 rules to produce one of three situation states: **Normal**, **Warning**, or **Critical**.

### Sensory inputs (from Kaggle CSV stream)
| Field | Unit | Rules applied |
|---|---|---|
| `energy_kwh` | kWh | High-usage detection |
| `room_temp_c` | °C | Heat-stress detection |
| `occupancy_count` | persons | Occupancy-aware rules |
| `tariff_per_kwh_usd` | USD | Peak-tariff surge detection |

### Non-sensory inputs
| Input | Source | Rules applied |
|---|---|---|
| Outdoor temperature | OpenWeatherMap (Retrofit) | Extreme weather rules |
| Budget goal | Room DB (user setting) | Budget threshold rules |
| `is_weekend` | CSV stream / calendar | Weekend high-usage rule |
| `holiday_flag` | CSV stream | Holiday pattern rules |

### Output states
- **Normal** — no threshold exceeded, usage within budget
- **Warning** — budget ≥ 80% used, outdoor temp > 28 °C, or cold-weather high usage
- **Critical** — budget exceeded, outdoor temp > 35 °C with high energy, or peak-tariff surge

---

## Advanced Features

### WorkManager — EnergyBudgetWorker
`EnergyBudgetWorker` checks `dailyCumulativeKwh` against `budgetGoal` and fires Android notifications at:
- **80% threshold** — "You've used 80% of your daily energy budget"
- **100% threshold** — "Daily energy budget exceeded"

`WorkManagerScheduler` enqueues two work requests:
- A `PeriodicWorkRequest` with 15-minute repeat interval (Android's enforced minimum) and a 15-second `initialDelay` so the first run is visible during a demo
- A `OneTimeWorkRequest` with 15-second delay as an immediate demo trigger

WorkManager is only re-scheduled when `budgetGoal` changes (not on every simulator tick) to prevent the periodic timer being reset continuously.

### TFLite — EnergyForecaster
`EnergyForecaster` loads `energy_forecast.tflite` from the app's assets directory. The model accepts a 6-float input vector (StandardScaler-normalised: energy_kwh, room_temp_c, occupancy_count, tariff_per_kwh, is_weekend, hour_of_day) and outputs a predicted next-hour kWh value. A linear regression fallback is used if TFLite inference fails.

---

## Known Issues

| Issue | Status | Notes |
|---|---|---|
| TFLite inference error (24 vs 36 bytes) | Logged but handled | Model uses 6 features; code sends 6 after fix. Fallback linear regression runs if mismatch occurs |
| WorkManager never firing in dev | Fixed | `LaunchedEffect` key changed from `dailyCumulativeKwh` to `budgetGoal` |
| Seeded appliances appearing on Member device | Fixed | Firestore listener now uses full reconciliation instead of documentChanges only |
| Cross-household messages | Fixed | All Firestore listeners stored as `ListenerRegistration` and removed on logout |

---

## Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

`POST_NOTIFICATIONS` is requested at runtime on Android 13+ before WorkManager notifications can be shown.

---

## Firestore Data Structure

```
users/
  {uid}/
    fullName, email, role (Owner|Member), householdId, suburb, status (approved|pending|removed)

households/
  {householdId}/
    appliances/
      {applianceId}/
        id, name, category, wattage, notes
    messages/
      {messageId}/
        senderId, senderName, content, createdAt, type (CHAT|ALERT)
```

---

*WattWise — FIT5046 Assessment 4 — Lab03, Group 05 — Monash University S1 2026*
