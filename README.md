# Penguin Purse

Penguin Purse is a cute, clean, penguin-themed offline-first Android personal finance tracker made for local use. It stores all core finance data locally with Room and does not require login, cloud sync, banking APIs, AI APIs, or internet access for tracking features.

## What is included

- Expense and income records with date, amount, type, category, note, currency, `createdAt`, and `updatedAt`
- Add, edit, delete with confirmation, category filtering, type filtering, and custom dates
- Preset and custom categories with emoji
- Simple four-operation calculator inside the record form
- Monthly budget and category-level budgets
- Savings amount, savings target, progress display, and monthly rollover
- Mandatory `lastProcessedMonth` month-change detection using device local date
- Monthly snapshots instead of fake auto-generated transactions
- Daily, weekly, monthly, and yearly stats with local chart views
- JSON export/import for backup and restore
- CSV export for records and reports
- Rule-based Saving Coach
- Advanced Personalized AI Coach prompt generator only, with no built-in AI chat and no API calls
- Penguin mascot assets reused from the provided mascot sheet
- Launcher icon generated from the provided AppIcon reference
- Settings footer: `Made by Acry and her teammates` with `https://github.com/Cryjai`

## Build in Android Studio

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Build `app` using the `debug` variant.
4. The APK will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Command-line build

If Android SDK is installed locally:

```bash
./gradlew assembleDebug
```

## Notes

- This is a first working version focused on reliability and offline-first behavior.
- The UI is built programmatically in Java to keep the project simple and easy to rebuild.
- Room is the local source of truth.
- Import warns before merge/overwrite through the Android file picker flow.
- Currency is intentionally one-wallet / one-base-currency only. No conversion, live rates, bank sync, or multi-wallet exchange logic.

## v1.1 UI / asset pass

- Replaced the rough pose-sheet crops with the user's individually cut penguin image files.
- Kept the light-blue image backgrounds instead of forcing transparency, to avoid damaging the penguin's white belly.
- Updated the Home screen to better follow the UI draft: large Balance card with mascot, microcopy, Daily Check card, Add Record card, budget/saving progress, and chart.
- Added penguin visual category buttons inside Add/Edit Record.
- Updated Budget & Savings to more closely match the UI draft: overall budget, category budgets, Saving Coach card, Export/Import card, and Advanced AI Coach copy prompt card.
