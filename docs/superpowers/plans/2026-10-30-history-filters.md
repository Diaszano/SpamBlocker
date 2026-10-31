# Filtros Avançados do Histórico Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users narrow call and SMS history by time and decision category while preserving current toggles and fuzzy search.

**Architecture:** Add typed filter state to `HistoryViewModel`, map saved `HistoryRecord` outcomes to stable display categories, and pass applicable constraints through the paginated history query. Add a compact filter dialog and active-filter summary in the existing history screen.

**Tech Stack:** Kotlin, Jetpack Compose, Android SQLite, JUnit 5, AndroidX Compose UI Test.

**Spec:** `docs/superpowers/specs/2026-10-30-history-filters-design.md`

## Global Constraints

- Depends on the cursor/page API in `docs/superpowers/plans/2026-10-30-history-pagination.md`.
- Time presets are exactly `24 hours`, `7 days`, `30 days`, and `Everything`.
- Retain allowed/blocked toggles and fuzzy search; filters compose with both.
- Do not renumber or reinterpret persisted `Def` result codes.
- Reset cursor and cancel in-flight loads whenever filters change.

## Review Focus

- Local day boundaries and daylight-saving transitions: fixed-duration presets use elapsed milliseconds; labels must say the duration they actually mean.
- A result with `anythingWrongScreening=true`: expose the error category without changing whether it was blocked or allowed.
- No category selected / `Everything`: reproduce current unfiltered results.
- Call-only or SMS-only result values: do not show irrelevant categories for the active tab.
- Search and filter change while pagination is running: old results must not append to the new filter set.

## File and Test Map

- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt`
- Modify: `app/src/main/java/spam/blocker/db/HistoryTable.kt`
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryScreen.kt`
- Add if the dialog warrants a focused component: `app/src/main/java/spam/blocker/ui/history/HistoryFiltersDialog.kt`
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and locale string resources following project convention.
- Extend `app/src/test/java/spam/blocker/ui/history/HistoryEmptyUnitTest.kt` or add `HistoryFiltersTest.kt`.
- Extend `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`.

## Interfaces

- `enum class HistoryPeriod { LAST_24_HOURS, LAST_7_DAYS, LAST_30_DAYS, EVERYTHING }`
- `enum class HistoryResultFilter { RULE, SPAM_DATABASE, LOCAL_AI, ONLINE_QUERY, SCREENING_ERROR }`
- `data class HistoryFilters(val period: HistoryPeriod, val categories: Set<HistoryResultFilter>)`
- `HistoryViewModel.filters` owns current selection; changes call the pagination reset/reload path from plan 1.
- `HistoryTable.listPage(...)` gains a typed optional `HistoryFilters` argument or equivalent bound query object; no raw SQL is accepted from UI.

---

### Task 1: Define filter semantics and test result mapping

**Files:**
- Create: `app/src/test/java/spam/blocker/ui/history/HistoryFiltersTest.kt`
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryViewModel.kt` (or a focused `HistoryFilters.kt`)

- [ ] **Step 1: Write failing pure tests** for each `HistoryPeriod` boundary, all result categories, `anythingWrongScreening`, and empty category set.
- [ ] **Step 2: Run** `./gradlew :app:testFdroidQaUnitTest --tests spam.blocker.ui.history.HistoryFiltersTest`; confirm missing filter model/mapping fails.
- [ ] **Step 3: Implement typed filters and mapping** from existing result constants; preserve the independent blocked/allowed decision and expose error as an additional dimension.
- [ ] **Step 4: Run the focused JVM test** and verify exact inclusion at the lower timestamp boundary.
- [ ] **Step 5: Commit** as `feat: define history filter semantics`.

### Task 2: Apply filters to paginated database reads

**Files:**
- Modify: `app/src/main/java/spam/blocker/db/HistoryTable.kt`
- Test: `app/src/androidTest/java/spam/blocker/db/HistoryPaginationTest.kt`

**Interfaces:** Consumes `HistoryFilters`; returns a normal `HistoryPage` with the same cursor contract.

- [ ] **Step 1: Add failing database tests** for period plus category combinations and cursor continuation under an active filter.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.db.HistoryPaginationTest`; confirm query filtering is absent.
- [ ] **Step 3: Add bound SQL predicates** for period timestamps and result codes. Bind all values; keep contact-name fuzzy matching in Kotlin after each page is read.
- [ ] **Step 4: Run database pagination tests**; verify a page cursor does not skip matching rows separated by nonmatching rows.
- [ ] **Step 5: Commit** as `feat: filter paginated history queries`.

### Task 3: Add filter controls and active-filter state

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/history/HistoryScreen.kt`
- Add if needed: `app/src/main/java/spam/blocker/ui/history/HistoryFiltersDialog.kt`
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching translations.
- Test: `app/src/androidTest/java/spam/blocker/ui/history/HistoryFlowsTest.kt`

- [ ] **Step 1: Add failing UI tests** for opening the filter panel, selecting each period/category, visible active summary, clearing filters, and applying filters independently to calls and SMS.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.history.HistoryFlowsTest` and verify controls are absent.
- [ ] **Step 3: Add compact dialog controls** using existing project widgets. Filter edits update typed VM state and restart page loading; retain current allowed/blocked buttons and `SearchBox`.
- [ ] **Step 4: Run the focused UI tests**; verify list, empty state, and active summary stay synchronized.
- [ ] **Step 5: Run** `./gradlew :app:testFdroidQaUnitTest :app:connectedFdroidQaAndroidTest`; confirm all tests pass.
- [ ] **Step 6: Commit** as `feat: add advanced call and sms history filters`.

## Review Focus Tests

The five listed cases map to `HistoryFiltersTest`, `HistoryPaginationTest`, and `HistoryFlowsTest`. Do not use local wall-clock formatting as the stored comparison value; compare epoch milliseconds.
