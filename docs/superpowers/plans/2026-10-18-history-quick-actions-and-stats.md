# History Quick Prefix Action and Compact Stats Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable users to block an entire number prefix/range directly from the history context menu with one tap, and display a discreet weekly blocked count in the history header.

**Architecture:** In `HistoryContextMenu.kt`, add a "Block Prefix / Range" action that extracts the leading digits (trunk/carrier prefix) and pre-fills `EditRegexDialog` with a wildcard pattern (e.g. `119999.*`). In `HistoryHeader.kt`, add a compact one-line summary showing calls and SMS blocked over the last 7 days queried from `CallTable` and `SmsTable`.

**Tech Stack:** Kotlin, Jetpack Compose, SQLite queries on `CallTable` / `SmsTable`.

## Global Constraints

- No schema migrations. Stats are computed via lightweight indexed queries: `SELECT COUNT(*) FROM table WHERE time >= :since AND result = BLOCKED`.
- Preserve dark compact UI. The stats line must not push down the history list or overlap with FABs.
- Touch targets must remain >= 48dp for all interactive elements.

---

### Task 1: "Block Prefix" Action in History Context Menu

**Files:**
- Create: `app/src/main/java/spam/blocker/util/PrefixUtils.kt`
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryContextMenu.kt`
- Test: `app/src/test/java/spam/blocker/util/PrefixUtilsTest.kt`

**Interfaces:**
- Produces: `fun PrefixUtils.suggestPrefixPattern(rawNumber: String): String`

- [ ] **Step 1: Write unit test for prefix extraction**

```kotlin
package spam.blocker.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PrefixUtilsTest {
    @Test
    fun testSuggestPrefixPattern() {
        assertEquals("1198765.*", PrefixUtils.suggestPrefixPattern("11987654321"))
        assertEquals("+55119876.*", PrefixUtils.suggestPrefixPattern("+551198765432"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.util.PrefixUtilsTest"`
Expected: FAIL

- [ ] **Step 3: Implement `PrefixUtils.kt` and add "Block Prefix" item to `HistoryContextMenu.kt`**
- [ ] **Step 4: Run test to verify it passes**
- [ ] **Step 5: Run `./gradlew assembleDebug lint` and commit: `feat: add block prefix action to history context menu`**

---

### Task 2: Compact Weekly Blocked Counter in History Header

**Files:**
- Modify: `app/src/main/java/spam/blocker/db/HistoryTable.kt`
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt`

**Interfaces:**
- Produces: `fun HistoryTable.countBlockedSince(ctx: Context, sinceMillis: Long): Int`
- `HistoryHeader` displays discrete subtitle text: `Str(R.string.blocked_last_7_days, count)` in `G.palette.textGrey` (12sp).

- [ ] **Step 1: Implement `countBlockedSince` in `CallTable` and `SmsTable`**
- [ ] **Step 2: Add reactive state to `HistoryViewModel` refreshed during `reload()`**
- [ ] **Step 3: Render discreet 12sp summary inside `HistoryHeader`**
- [ ] **Step 4: Add dark 320dp/1.3x preview for `HistoryHeader` with stats**
- [ ] **Step 5: Run `./gradlew assembleDebug lint` and commit: `feat: display weekly blocked count in history header`**
