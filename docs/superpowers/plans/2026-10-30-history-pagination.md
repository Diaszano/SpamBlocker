# Paginação do Histórico Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Load call and SMS history in bounded pages while retaining global fuzzy search and current history behavior.

**Architecture:** Add keyset pagination to `HistoryTable`, using `(time DESC, id DESC)` as a stable cursor. `HistoryViewModel` loads pages off the main thread, applies existing visibility and fuzzy-search rules, and fetches further pages when the user scrolls or a global search still needs older records.

**Tech Stack:** Kotlin, Jetpack Compose, Android SQLite, Lifecycle ViewModel coroutines, JUnit 5, AndroidX Compose UI Test.

**Spec:** `docs/superpowers/specs/2026-10-30-history-pagination-design.md`

## Global Constraints

- Use 50 rows per database page.
- Preserve fuzzy search across the full history by scanning pages in the background when search is active.
- Preserve `(time DESC, id DESC)` order, existing allowed/blocked toggles, and existing empty/error/loading behavior.
- Do not delete history, add dependencies, or migrate the database unless measurements prove an index is needed.
- Database reads and cursor conversion must not block the main thread.

## Review Focus

- Equal timestamps: the ID tie-breaker returns each row once, in stable order.
- New records arrive between page requests: restart or reconcile the cursor without losing or duplicating visible rows.
- Search is changed while an older-page scan runs: stale results must not overwrite the newer query.
- All rows are filtered out on a page: continue fetching until a visible row or database exhaustion.
- Database read fails after some pages loaded: preserve visible rows and offer retry without marking the list empty.

## File and Test Map

- Modify `app/src/main/java/spam/blocker/db/HistoryTable.kt` for keyset queries and cursor/page models.
- Modify `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt` for page state, background loading, search generation/cancellation, and visible-row accumulation.
- Modify `app/src/main/java/spam/blocker/ui/history/HistoryList.kt` or `HistoryScreen.kt` for end-of-list loading and loading/error footer.
- Preserve reload callsites in `app/src/main/java/spam/blocker/ui/main/MainActivity.kt`, `HistoryScreen.kt`, and `HistoryFabs.kt`; keep `reload(ctx)` source-compatible.
- Add `app/src/androidTest/java/spam/blocker/db/HistoryPaginationTest.kt` using QA-owned database fixtures.
- Extend `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt` for scroll loading, older search matches, and recovery.

## Interfaces

- `data class HistoryCursor(val time: Long, val id: Long)`
- `data class HistoryPage(val records: List<HistoryRecord>, val nextCursor: HistoryCursor?)`
- `HistoryTable.listPage(ctx: Context, before: HistoryCursor?, limit: Int = 50): HistoryPage`
- `HistoryViewModel.loadNextPage(ctx: Context)` appends visible records and updates `isLoading`/`hasError`.
- `nextCursor` is the `(time,id)` pair of the final returned row, not the extra look-ahead row.

---

### Task 1: Add stable database keyset pagination

**Files:**
- Modify: `app/src/main/java/spam/blocker/db/HistoryTable.kt`
- Test: `app/src/androidTest/java/spam/blocker/db/HistoryPaginationTest.kt`

**Interfaces:** Produces `HistoryCursor`, `HistoryPage`, and `HistoryTable.listPage(...)` as defined above.

- [ ] **Step 1: Add failing database tests** for first page limit/order, next-page cursor, repeated timestamps, empty table, final short page, and invalid nonpositive limit.
- [ ] **Step 2: Run the focused test** with `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.db.HistoryPaginationTest`; confirm the new API/tests fail to compile or assertions fail before implementation.
- [ ] **Step 3: Implement keyset query** with `WHERE time < ? OR (time = ? AND id < ?)` for subsequent pages, `ORDER BY time DESC, id DESC`, and `limit + 1` rows to detect continuation. Bind all values as query arguments.
- [ ] **Step 4: Run the focused test** and confirm every cursor boundary passes without duplicate/missing IDs.
- [ ] **Step 5: Commit** the repository API and tests as `feat: page call and sms history`.

### Task 2: Load and search pages in the ViewModel

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt`
- Test: `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`

**Interfaces:** Consumes `HistoryPage`; `loadNextPage(ctx)` appends rows and `reload(ctx)` resets cursor/query state while retaining its current call signature.

- [ ] **Step 1: Add failing ViewModel/UI tests** for initial 50 visible rows, accumulated next page, fuzzy match found only on an older page, filtered-out pages, query changes during loading, and a read error after a prior successful page.
- [ ] **Step 2: Run those tests** with `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryFlowsTest`; verify missing pagination behavior fails.
- [ ] **Step 3: Implement page state and background reads.** Keep a cursor, exhaustion flag, and monotonically increasing query generation (or cancellable job); build `FuzzyFilter` once per query and continue reading pages until 50 visible matches or exhaustion. Search over contact name, number, stored SMS text, and reason exactly as today.
- [ ] **Step 4: Run focused tests** and verify stale jobs cannot mutate current results and errors do not erase already loaded rows.
- [ ] **Step 5: Commit** as `feat: load history pages and search globally`.

### Task 3: Connect scrolling to incremental loading

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryList.kt`
- Modify if needed: `app/src/main/java/spam/blocker/ui/history/HistoryScreen.kt`
- Test: `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`

**Interfaces:** Calls `HistoryViewModel.loadNextPage(ctx)` when the final loaded row becomes visible; does not make direct database calls from composables.

- [ ] **Step 1: Add failing Compose tests** that scroll to the end, assert more rows appear once, and assert a terminal list does not keep requesting pages.
- [ ] **Step 2: Run the focused Compose tests** and confirm the current single-load list cannot satisfy them.
- [ ] **Step 3: Add a `LaunchedEffect`/scroll observation** keyed to the current query and list state. Show a compact footer spinner/error retry only while fetching an additional page; preserve existing full-screen first-load/error states.
- [ ] **Step 4: Run history UI tests** with `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryFlowsTest`.
- [ ] **Step 5: Run all QA unit and Android tests** with `./gradlew :app:testFdroidQaUnitTest :app:connectedFdroidQaAndroidTest`; confirm all tests pass.
- [ ] **Step 6: Commit** as `feat: incrementally render call and sms history`.

## Review Focus Tests

All five conditions in Review Focus are covered by `HistoryPaginationTest` or `HistoryFlowsTest`; use test-owned QA data only and never clear the user's production app data.
