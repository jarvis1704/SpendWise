# 📊 SpendWise Performance Audit Report

**Date:** Monday, April 6, 2026  
**Status:** ⚠️ Moderate optimizations needed.  
**Focus:** APK Size, Startup Latency, and Compose Recomposition efficiency.

---

## 📦 APK / AAB Size Analysis

Size is a critical conversion factor. Current dependencies and configurations suggest significant bloat that can be trimmed.

### 1. Optimize Material Icons Extended
*   **Finding:** `androidx.compose.material:material-icons-extended` is included in `build.gradle.kts`.
*   **Impact:** High (~5MB to 10MB+ increase in APK size). This library contains thousands of icons, most of which are unused.
*   **Recommendation:** 
    *   **Option A:** Explicitly import only the icons you need (e.g., `androidx.compose.material:material-icons-core` + specific icons).
    *   **Option B:** Ensure R8 is stripping them effectively. In 2025/2026, it is recommended to use the `material-icons-core` and add only the specific icon sets needed.
*   **Effort:** Low

### 2. Missing Locale Stripping (`resConfigs`)
*   **Finding:** The `defaultConfig` in `app/build.gradle.kts` does not specify `resConfigs`.
*   **Impact:** Medium (~500KB - 1MB). All localized strings from every library (Google Play Services, Firebase, AppCompat) are bundled even if the app only supports English.
*   **Fix:**
    ```kotlin
    defaultConfig {
        resConfigs("en") // Add other supported languages here
    }
    ```
*   **Ref:** [developer.android.com/studio/build/shrink-code#unused-alt-resources](https://developer.android.com/studio/build/shrink-code#unused-alt-resources)

### 3. Enable R8 "Full Mode"
*   **Finding:** `isMinifyEnabled` is true, but "Full Mode" (more aggressive optimization) isn't explicitly mentioned in `gradle.properties`.
*   **Impact:** Medium (Better code shrinking and optimization).
*   **Fix:** Add `android.enableR8.fullMode=true` to `gradle.properties`.
*   **Ref:** [developer.android.com/studio/releases#r8-full-mode](https://developer.android.com/studio/releases#r8-full-mode)

### 4. Large Charting Library (`vico`)
*   **Finding:** `com.patrykandpatrick.vico:compose` and `compose-m3` are used.
*   **Impact:** Medium (Adds significant dependency weight).
*   **Recommendation:** If APK size remains a concern after other optimizations, consider a lighter charting solution or custom Canvas-based implementation for simple charts.
*   **Effort:** High (Rewriting charts)

---

## 🚀 App Startup Performance

The transition from the system splash to the app's first contentful paint can be improved.

### 1. Missing Baseline Profiles
*   **Finding:** No `baselineprofile` module or `baseline-prof.txt` found.
*   **Impact:** **High** (Up to 30% faster startup). Baseline profiles allow the system to pre-compile critical code paths before the app is even opened.
*   **Recommendation:** Create a Baseline Profile generator module using the `androidx.baselineprofile` plugin.
*   **Ref:** [developer.android.com/topic/performance/baselineprofiles](https://developer.android.com/topic/performance/baselineprofiles)

### 2. Optimized Splash Screen Exit
*   **Finding:** `MainActivity` shows an empty `Box` while `startState == AppStartState.LOADING`.
*   **Impact:** Low-Medium (Visual "flicker" or delay). The system splash screen disappears, and then the user sees a blank screen while the DataStore/Room loads.
*   **Fix:** Use the `installSplashScreen().setKeepOnScreenCondition { viewModel.startState.value == AppStartState.LOADING }` in `MainActivity`.
*   **Effort:** Low

---

## 🖼️ Rendering & UI Performance

The app uses modern Jetpack Compose, but some reactive patterns may cause unnecessary work.

### 1. HomeViewModel UI State "Fatigue"
*   **Finding:** The `uiState` in `HomeViewModel` is a massive `combine` chain that includes 5+ flows.
*   **Problem:** Every single transaction update (add/delete/edit) triggers a full re-calculation of the `HomeUiState` and a full recomposition of `HomeScreen`.
*   **Recommendation:**
    *   Break down the `uiState` into smaller, focused flows (e.g., `dailyBudgetFlow`, `balanceFlow`, `streakFlow`).
    *   Use `remember` and `derivedStateOf` in the Composable to only react to the specific fields that changed.
*   **Impact:** Medium (Reduces CPU usage and frame drops on dashboard).

### 2. Heavy Initialization in `HomeViewModel`
*   **Finding:** `updateStreakUseCase()` is called directly in the `init` block of `HomeViewModel`.
*   **Impact:** Medium (Can delay the first frame of the Home screen).
*   **Fix:** Move heavy calculations or DB updates to a `LaunchedEffect` in the Screen or trigger it lazily.

---

## 🧠 Memory & Data Management

### 1. Room Flow Multi-Emissions
*   **Finding:** `GetFinancialSummaryUseCase` combines 3 separate Room flows.
*   **Problem:** When a transaction is added, Room might emit on all three flows separately, causing the `combine` block to run multiple times in rapid succession.
*   **Fix:** Use a single `@Query` that returns all 3 values (Total Income, Total Expense, Today Expense) in one POJO to ensure atomic updates.

### 2. Unnecessary Object Allocations in `formatAmount`
*   **Finding:** `formatAmount` creates a new `NumberFormat` instance on every call.
*   **Impact:** Low (GC Pressure). If called frequently (e.g., in a list or rapid UI updates), this adds up.
*   **Fix:** Cache the `NumberFormat` instance in a `remember` block or a companion object.

---

## 🏆 Priority Action List

| Priority | Task | Impact | Effort |
| :--- | :--- | :--- | :--- |
| **1 - Critical** | **Add Baseline Profiles** | High | Medium |
| **2 - High** | **Trim Material Icons Extended** | High | Low |
| **3 - High** | **Keep Splash Screen during Loading** | Medium | Low |
| **4 - Medium** | **Add `resConfigs` to Gradle** | Medium | Low |
| **5 - Medium** | **Optimize Room flows in UseCase** | Medium | Medium |

---

## Final Verdict
❌ **Significant performance issues found.** (Mainly due to lack of Baseline Profiles and massive Icon dependency). Addressing the Top 3 items will result in a significantly leaner and faster application.
