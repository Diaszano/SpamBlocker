# Temporary Blocking Pause and JVM Pure Unit Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a temporary blocking pause feature (15 min, 1 hour, or custom duration) that temporarily suspends call/SMS filtering, and mirror pure arithmetic/logic tests to JVM `src/test/` so they run instantly in CI without an emulator.

**Architecture:** Extend `spf.Global` with a `pausedUntilTimestamp: Long` preference. In `Checker.kt` and `GlobalEnable.kt`, if `currentTime < pausedUntil`, consider global blocking temporarily suspended. In `HistoryHeader` and `GloballyEnabled`, show a countdown chip/banner with a "Resume" button. Mirror `PopupSizeTest` and `HistoryEmptyTest` to `app/src/test/java/`.

**Tech Stack:** Kotlin, Jetpack Compose, Android SharedPreferences (`SharedPref.kt`), JUnit 4.

## Global Constraints

- No background alarms needed: evaluate `System.currentTimeMillis() < pausedUntil` synchronously during incoming call/SMS check.
- When paused, Quick Tile reflects the paused state and allows unpausing with a single tap.
- Pure JVM unit tests must execute cleanly with `./gradlew testFdroidDebugUnitTest`.

---

### Task 1: Mirror Pure Logic Tests to JVM `src/test/`

**Files:**
- Create: `app/src/test/java/spam/blocker/ui/widgets/PopupSizeUnitTest.kt`
- Create: `app/src/test/java/spam/blocker/ui/history/HistoryEmptyUnitTest.kt`

- [ ] **Step 1: Create `PopupSizeUnitTest.kt` testing clamping on 320dp and 600dp viewports**
- [ ] **Step 2: Create `HistoryEmptyUnitTest.kt` testing fallback hierarchy of empty messages**
- [ ] **Step 3: Run `./gradlew testFdroidDebugUnitTest` and verify all tests PASS without an emulator**
- [ ] **Step 4: Commit: `test: mirror popup size and history empty tests to JVM test suite`**

---

### Task 2: Temporary Pause Logic and State Handling

**Files:**
- Modify: `app/src/main/java/spam/blocker/util/SharedPref.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/GlobalEnable.kt`
- Modify: `app/src/main/java/spam/blocker/service/checker/Checker.kt`
- Test: `app/src/test/java/spam/blocker/util/PauseLogicTest.kt`

**Interfaces:**
- Produces: `fun GlobalEnable.pauseBlocking(ctx: Context, durationMillis: Long)`
- Produces: `fun GlobalEnable.resumeBlocking(ctx: Context)`
- Produces: `fun GlobalEnable.isPaused(ctx: Context): Boolean`

- [ ] **Step 1: Write unit test for `isPaused` evaluation with simulated timestamps**
- [ ] **Step 2: Add `pausedUntil` property to `spf.Global`**
- [ ] **Step 3: Update `Checker.kt` to check `if (GlobalEnable.isPaused(ctx)) return RESULT_ALLOWED`**
- [ ] **Step 4: Verify test passes**

---

### Task 3: UI Pause Affordance in History and Settings

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/GloballyEnabled.kt`
- Modify: `app/src/main/java/spam/blocker/service/CustomTile.kt`

- [ ] **Step 1: Add "Pause for 15m / 1h" options dialog triggered from long-press on Master Switch**
- [ ] **Step 2: Render discreet "Paused (XXm remaining) [Resume]" indicator when active**
- [ ] **Step 3: Update `CustomTile.kt` to reflect paused state on the Android Quick Settings tile**
- [ ] **Step 4: Add dark 320dp/1.3x preview for paused state**
- [ ] **Step 5: Run `./gradlew assembleDebug lint` and commit: `feat: implement temporary blocking pause`**
