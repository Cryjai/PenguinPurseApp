# 🐧 Penguin Purse

**Penguin Purse** is a cute, offline-first Android personal finance app for people who want a clean budgeting experience without giving their financial data to random clouds.

- 📴 100% offline — no login, no cloud sync, no tracking
- 🧠 Local-first with Room as the single source of truth
- 🐧 Penguin-themed UI designed for low-friction daily use

## Download

### Latest APK
- [Download Penguin Purse v1.2 APK](./PenguinPurse-v1.2-debug.apk)

> Current stable version: **v1.2**  
> This is the current final version for now.

## Why Penguin Purse

Most finance apps try to become your bank, your therapist, your subscription trap, and your data vacuum cleaner at the same time.  
**Penguin Purse** does not.

It is built as a private, local finance notebook:
- no account creation
- no internet dependency for tracking
- no analytics, ads, or hidden syncing
- no fake “AI assistant” talking to your wallet behind the scenes

## Features

- 💸 **Expense and income tracking**  
  Record date, amount, type, category, note, currency, `createdAt`, and `updatedAt`.

- ➕ **Fast record workflow**  
  Add, edit, and delete records with confirmation, type/category filters, and custom date input.

- 🏷️ **Preset + custom categories**  
  Includes emoji-friendly categories so the app feels personal instead of sterile.

- 🧮 **Built-in calculator**  
  A simple four-operation calculator is included inside the record form.

- 📊 **Budgeting and savings tools**
  - Monthly budget
  - Category-level budgets
  - Savings amount
  - Savings target
  - Progress display
  - Monthly rollover logic

- 🕒 **Reliable month change handling**  
  Uses mandatory `lastProcessedMonth` logic based on device local date, with monthly snapshots instead of fake auto-generated transactions.

- 📈 **Local stats and charts**  
  Daily, weekly, monthly, and yearly statistics with on-device chart views.

- 📤 **Backup and export**
  - JSON export/import for backup and restore
  - CSV export for records and reports

- 🐧 **Saving Coach + AI prompt helper**
  - Rule-based Saving Coach
  - Advanced Personalized AI Coach prompt generator only
  - No built-in AI chat
  - No API calls

## Privacy and scope

Penguin Purse is intentionally limited in scope.

- One wallet only
- One base currency only
- No bank sync
- No live exchange rates
- No multi-wallet exchange logic
- No analytics
- No ads
- No cloud dependency

This app is designed as a **local-first personal finance notebook**, not a SaaS platform.

## Tech stack

- **Language:** Java
- **UI approach:** Programmatic Android UI
- **Architecture:** Offline-first
- **Local database:** Room
- **Export formats:** JSON and CSV
- **Build system:** Gradle

Design principles:
- Local-first > cloud-first
- Predictable behavior > hidden magic
- Explicit user actions > silent background jobs

## UI update notes

### v1.1 UI / asset pass
- Replaced rough pose-sheet crops with individually cut penguin image assets
- Kept light-blue image backgrounds to preserve the penguin’s white belly
- Updated Home screen to better match the UI draft
- Added penguin visual category buttons in Add/Edit Record
- Updated Budget & Savings layout with better card structure

### v1.2
- Current final version for now
- APK added directly to this repository for easier download and testing

## Build from source

### Android Studio
1. Open this folder in **Android Studio**
2. Let Gradle sync
3. Select the `debug` build variant for `app`
4. Build the APK at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Command line

If Android SDK is installed locally:

```bash
./gradlew assembleDebug
```

The debug APK will be generated in the same `app/build/outputs/apk/debug/` directory.

## About

Penguin Purse is built by **Acry and her teammates**.

- GitHub: [@Cryjai](https://github.com/Cryjai)
