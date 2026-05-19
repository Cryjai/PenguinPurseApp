# 🐧 Penguin Purse

**Penguin Purse** is a cute, offline-first Android personal finance app for people who hate giving their bank data to random clouds but still want a clean, fun budgeting experience.

- 📴 100% offline – no login, no cloud, no tracking  
- 🧠 Local-first with Room as single source of truth  
- 🐧 Penguin-themed UI designed for low-friction daily use  

---

## ✨ Features at a glance

- 💸 **Expense & income tracking**  
  Date, amount, type, category, note, currency, `createdAt`, `updatedAt`.

- ➕ **Fast record workflow**  
  Add / edit / delete with confirmation, category & type filters, and custom dates.

- 🏷️ **Categories with emoji**  
  Preset + custom categories so the wallet actually looks like your life.

- 🧮 **Inline calculator**  
  Simple four-operation calculator in the record form – no context switching.

- 📊 **Budgets & savings**  
  - Monthly budget  
  - Category-level budgets  
  - Savings amount, savings target, progress display  
  - Monthly rollover logic

- 🕒 **Month-change detection**  
  Mandatory `lastProcessedMonth` using device local date, with **monthly snapshots** instead of fake auto-generated transactions.

- 📈 **Stats & charts**  
  Daily / weekly / monthly / yearly stats with local chart views.

- 📤 **Backups & export**  
  - JSON export/import for backup & restore  
  - CSV export for records and reports

- 🐧 **Saving Coach & AI prompt generator**  
  - Rule-based Saving Coach  
  - Advanced Personalized AI Coach **prompt generator only** (no API calls, no chat built in).

---

## 🧱 Tech stack

- **Language**: Java (UI built programmatically for clarity and rebuildability)
- **Architecture**: Offline-first, Room as local source of truth
- **Storage**: Room database + JSON/CSV export
- **Platform**: Android, built with Gradle

Design principles:

- Local-first > cloud-first  
- Predictable behavior > hidden magic  
- Explicit user actions > silent background jobs  

---

## 📲 Build & run

### Android Studio

1. Open this folder in **Android Studio**.  
2. Let Gradle sync.  
3. Select the `debug` build variant for `app`.  
4. Build the APK. It will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Command line

If Android SDK is installed locally:

```bash
./gradlew assembleDebug
```

The debug APK will be generated in the same `app/build/outputs/apk/debug/` directory.

---

## 🔒 Privacy & scope

Penguin Purse is intentionally **one-wallet / one-base-currency** only.

- No bank sync  
- No live FX rates  
- No multi-wallet exchange logic  
- No analytics, ads, or tracking

The app is designed as a **local finance notebook** you fully own, not a SaaS product.

---

## 🐧 v1.1 UI / asset pass

- Replaced rough pose-sheet crops with individually cut penguin assets.
- Kept light-blue image backgrounds to preserve the penguin’s white belly.
- Updated Home screen to match UI draft:
  - Large Balance card with mascot
  - Microcopy, Daily Check card, Add Record card
  - Budget/saving progress + chart
- Added penguin visual category buttons in Add/Edit Record.
- Updated Budget & Savings screen:
  - Overall budget and category budgets
  - Saving Coach card
  - Export/Import card
  - Advanced AI Coach copy-prompt card

---

## 👤 About

Penguin Purse is built by **Acry and her teammates** as an offline-first, penguin-powered finance app.

- GitHub: [@Cryjai](https://github.com/Cryjai)
