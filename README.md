<div align="center">
  <!-- You can add a logo image here -->
  <!-- <img src="docs/images/logo.png" width="128" height="128" alt="SpendWise Logo"/> -->

  #  SpendWise

  **Your Intelligent, Privacy-First Personal Finance Companion**

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Modern_UI-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
  [![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android)](https://www.android.com/)

</div>

---

## 📖 Overview

SpendWise is a modern, native Android application built entirely with Kotlin and Jetpack Compose. It's designed to help users take control of their finances with ease, offering intuitive expense logging, visual budget tracking, AI-powered insights, and robust biometric security—all while keeping your data strictly local and private. The app features a beautiful, fluid interface based on **Material 3 Expressive** design guidelines.

---

## 📱 Screenshots 

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Home Dashboard</b></td>
      <td align="center"><b>Expense Insights</b></td>
      <td align="center"><b>Add Transaction</b></td>
    </tr>
    <tr>
      <td><img src="https://via.placeholder.com/300x600.png?text=Home+Screen" width="250" alt="Home Screen"/></td>
      <td><img src="https://via.placeholder.com/300x600.png?text=Insights+Screen" width="250" alt="Insights Screen"/></td>
      <td><img src="https://via.placeholder.com/300x600.png?text=Add+Transaction" width="250" alt="Add Transaction Sheet"/></td>
    </tr>
    <tr>
      <td align="center"><b>Budget & Goals</b></td>
      <td align="center"><b>Transaction History</b></td>
      <td align="center"><b>Biometric Lock</b></td>
    </tr>
    <tr>
      <td><img src="https://via.placeholder.com/300x600.png?text=Budget+Screen" width="250" alt="Budget Screen"/></td>
      <td><img src="https://via.placeholder.com/300x600.png?text=History+Screen" width="250" alt="History Screen"/></td>
      <td><img src="https://via.placeholder.com/300x600.png?text=Biometric+Lock" width="250" alt="Biometric Screen"/></td>
    </tr>
  </table>
</div>

---

## ✨ Key Features

*   **🎨 Material 3 Expressive Design:** Experience a vibrant, dynamic UI with fluid animations, custom shapes, and an adaptable color palette.
*   **📊 Interactive Dashboards:** Visualize your spending habits with dynamic donut charts, line graphs, and spending calendars powered by the Vico charting library.
*   **💸 Effortless Tracking:** Log daily incomes and expenses quickly using a beautifully designed bottom sheet.
*   **🎯 Smart Budgeting:** Set periodic budgets and track your remaining balance in real-time.
*   **🤖 AI Spending Assistant:** Leverage Firebase AI to gain deeper, contextual insights into your financial behavior and receive personalized advice.
*   **🔒 Biometric Security:** Keep your sensitive financial data locked behind device-level biometric authentication (Fingerprint/Face Unlock).
*   **🔥 Streaks & Gamification:** Stay motivated to track your spending daily with the built-in streak counter and interactive confetti celebrations.
*   **🌙 Dark Mode Ready:** Fully supports Material 3 dynamic theming with a gorgeous dark mode for late-night budgeting.
*   **📴 Offline-First Architecture:** 100% of your data is stored locally via Room database, ensuring maximum privacy, zero latency, and true offline capability.

---

## 🛠 Tech Stack & Libraries

SpendWise showcases modern Android development practices using the latest Jetpack libraries.

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
*   **Architecture:** Clean Architecture + MVVM + MVI-style state management
*   **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
*   **Local Database:** [Room Database](https://developer.android.com/training/data-storage/room)
*   **Key-Value Storage:** [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore)
*   **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
*   **Asynchronous Programming:** [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)
*   **Charts:** [Vico Compose](https://patrykandpatrick.com/vico)
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
*   **Security:** [AndroidX Biometric](https://developer.android.com/training/sign-in/biometric-auth)
*   **AI Integration:** Firebase AI (Gemini)

---

## 🏗 App Architecture

The project adheres to **Clean Architecture** principles to separate concerns, making the codebase highly testable, scalable, and maintainable.

### Layer Breakdown

1.  **Presentation (UI) Layer (`ui/`, `home/`, `insights/`, etc.)**
    *   Contains all Jetpack Compose UI elements styled with Material 3 Expressive.
    *   ViewModels manage state holding (`StateFlow`) and handle UI events.
2.  **Domain Layer (`domain/`)**
    *   Contains the core business logic (`UseCases` like `AddTransactionUseCase`, `GetFinancialSummaryUseCase`).
    *   Defines pure Kotlin data models (`Transaction`, `FinancialSummary`) independent of any framework.
3.  **Data Layer (`data/`)**
    *   Implements repository interfaces defined in the domain layer.
    *   Manages data from local sources (`Room`, `DataStore`).
4.  **DI Layer (`di/`)**
    *   Contains Dagger Hilt modules that provide dependencies across all layers.

---

## 🚀 Getting Started

Follow these steps to build and run the app on your local machine.

### Prerequisites

*   [Android Studio Ladybug](https://developer.android.com/studio) (or newer)
*   JDK 17
*   Android SDK Platform 36

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/SpendWise.git
    cd SpendWise
    ```

2.  **Open the project:**
    *   Open Android Studio and select `Open an existing project`.
    *   Navigate to the cloned `SpendWise` directory.

3.  **Firebase Configuration (Required for AI Features):**
    *   Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    *   Register an Android app with the package name `com.biprangshu.spendwise`.
    *   Download the `google-services.json` file.
    *   Place the `google-services.json` file inside the `app/` directory of the project.

4.  **Sync & Build:**
    *   Let Gradle sync the project dependencies.
    *   Click the **Run** button (`Shift + F10`) to build and deploy the app to your emulator or connected device.

---

## 📂 Project Structure

A quick glance at the main package structure (`com.biprangshu.spendwise`):

```text
├── data/              # Database (Room), DataStore, and Repository Implementations
├── di/                # Hilt Dependency Injection Modules
├── domain/            # Business Logic (Use Cases) and Core Models
├── navigation/        # App Navigation Graph and Routes
├── onboarding/        # First-time User Experience Screens
├── theme/             # Compose Material 3 Theme, Colors, Typography
├── ui/                # Core Feature Modules (Home, Insights, History, Budget)
│   ├── biometric/     # Biometric Lock Screen
│   ├── budgetend/     # Budget Summary & Analytics
│   ├── components/    # Reusable UI Components (DatePickers, Charts, Cards)
│   ├── history/       # Transaction History List
│   ├── home/          # Main Dashboard
│   └── insights/      # Spending Trends & AI Assistant
└── util/              # Helper functions and extensions
```

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---
<div align="center">
  <i>Built with ❤️ by Biprangshu</i>
</div>
