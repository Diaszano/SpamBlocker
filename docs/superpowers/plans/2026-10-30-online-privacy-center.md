# Transparência das Integrações Online Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give users an offline, local view of configured online integrations, their purpose, and the kinds of data each can send.

**Architecture:** Derive disclosures from enabled/disabled query APIs, report APIs, and workflow `HttpRequest` actions already stored in SQLite. Convert action templates into safe summaries that expose known variable names while redacting values, credentials, headers, and payload contents; render links to the existing editors.

**Tech Stack:** Kotlin, Jetpack Compose, SQLite models (`IApi`, `Bot`), JUnit 5, AndroidX Compose UI Test.

**Spec:** `docs/superpowers/specs/2026-10-30-online-privacy-center-design.md`

## Global Constraints

- The privacy center is read-only and works offline.
- Inspect query APIs, report APIs, and workflow HTTP actions; do not omit custom workflow requests.
- Never display tokens, passwords, raw header values, or complete request bodies.
- Do not add telemetry, request logging, server dependencies, or automatic changes to integrations.
- Clearly mark custom expressions whose resulting payload cannot be fully inspected.

## Review Focus

- URL includes token/user information: show only normalized domain and redact the rest.
- Header includes `Authorization`, cookie, or custom secret: disclose header presence/type without value.
- Body contains known number/message tags: identify those data categories without rendering the body.
- Body includes unknown custom expressions: label uninspected content and do not claim a complete inventory.
- Disabled API or workflow: display disabled state and do not imply requests are currently made.

## File and Test Map

- Add: `app/src/main/java/spam/blocker/ui/setting/misc/OnlinePrivacy.kt` for disclosure model and UI entry.
- Add if kept separate: `app/src/main/java/spam/blocker/service/bot/HttpDisclosure.kt` for safe template summarization.
- Reuse: `app/src/main/java/spam/blocker/db/ApiTable.kt`, `BotTable.kt`, `Actions1.kt` (`HttpRequest`), `app/src/main/java/spam/blocker/ui/setting/api/ApiViewModel.kt`, and `app/src/main/java/spam/blocker/ui/setting/bot/BotViewModel.kt`.
- Modify: `app/src/main/java/spam/blocker/ui/setting/SettingScreen.kt` or `misc/About.kt` to add the entry point.
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching locale resources.
- Add: `app/src/test/java/spam/blocker/service/bot/HttpDisclosureTest.kt`.
- Add: `app/src/test/java/spam/blocker/ui/setting/OnlinePrivacyTest.kt`.
- Add: `app/src/androidTest/java/spam/blocker/ui/setting/OnlinePrivacyFlowsTest.kt`.

## Interfaces

- `data class OnlineIntegrationDisclosure(id: String, name: String, domain: String?, purpose: DisclosurePurpose, enabled: Boolean, dataCategories: Set<DisclosureData>, hasUninspectedContent: Boolean)`
- `data class HttpActionDisclosure(val domain: String?, val method: String, val dataCategories: Set<DisclosureData>, val hasUninspectedContent: Boolean, val containsSensitiveHeader: Boolean)`; it contains no raw header/body values.
- `fun describeHttpAction(action: HttpRequest, customTags: Map<String, String>): HttpActionDisclosure`; inspect custom-tag names only, never their values.
- `fun buildOnlineDisclosures(apis: List<IApi>, bots: List<Bot>): List<OnlineIntegrationDisclosure>` is pure and testable.
- `fun loadOnlineDisclosures(ctx: Context): List<OnlineIntegrationDisclosure>` reads configured APIs/workflows only and does not execute actions.
- UI navigation consumes a stable integration source/id to open the existing query API, report API, or bot editor.

---

### Task 1: Define safe HTTP disclosure summaries

**Files:**
- Add: `app/src/main/java/spam/blocker/service/bot/HttpDisclosure.kt`
- Test: `app/src/test/java/spam/blocker/service/bot/HttpDisclosureTest.kt`

- [ ] **Step 1: Add failing tests** for number/message tags, custom tags, unknown expressions, URL credentials, authorization/cookie headers, empty body, and GET versus POST.
- [ ] **Step 2: Run** `./gradlew :app:testFdroidQaUnitTest --tests spam.blocker.service.bot.HttpDisclosureTest` and confirm there is no disclosure parser.
- [ ] **Step 3: Implement `describeHttpAction`** using the same tag vocabulary as `HttpRequest`; return data categories and an `hasUninspectedContent` flag, never resolved values. Normalize domain through the existing URL helper and discard path/query/userinfo.
- [ ] **Step 4: Run focused tests** and assert secrets and literal request content are absent from every output field.
- [ ] **Step 5: Commit** as `feat: summarize configured http data disclosures`.

### Task 2: Collect enabled and disabled integrations locally

**Files:**
- Add: `app/src/main/java/spam/blocker/ui/setting/misc/OnlinePrivacy.kt`
- Reuse: `app/src/main/java/spam/blocker/db/ApiTable.kt`, `BotTable.kt`, and `HttpDisclosure.kt`.
- Test: `app/src/test/java/spam/blocker/ui/setting/OnlinePrivacyTest.kt`

- [ ] **Step 1: Add failing tests** for query API, report API, workflow HTTP action, disabled integration, no configured endpoints, and stable edit target IDs.
- [ ] **Step 2: Run** `./gradlew :app:testFdroidQaUnitTest --tests spam.blocker.ui.setting.OnlinePrivacyTest`.
- [ ] **Step 3: Implement pure `buildOnlineDisclosures(apis, bots)`** and a thin `loadOnlineDisclosures(ctx)` that reads existing API and bot rows. Include purpose (lookup/report/workflow) and enabled state; never invoke `IAction.execute`.
- [ ] **Step 4: Run focused tests** and confirm loading disclosure data causes zero network activity.
- [ ] **Step 5: Commit** as `feat: collect local online integration disclosures`.

### Task 3: Add privacy center UI and editor links

**Files:**
- Modify: `app/src/main/java/spam/blocker/ui/setting/SettingScreen.kt` or `app/src/main/java/spam/blocker/ui/setting/misc/About.kt`
- Modify: `app/src/main/res/values/strings_ui_ux.xml` and matching locale resources.
- Add: `app/src/androidTest/java/spam/blocker/ui/setting/OnlinePrivacyFlowsTest.kt`

- [ ] **Step 1: Add failing UI tests** for empty/local-only state, configured endpoint list, disabled entries, redaction, uninspected warning, and opening the matching editor.
- [ ] **Step 2: Run** `./gradlew :app:connectedFdroidQaAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=spam.blocker.ui.setting.OnlinePrivacyFlowsTest`.
- [ ] **Step 3: Add a compact `Online privacy` destination** using existing app navigation/widgets. Show domain, purpose, enabled state, known data categories, uninspected marker, and a link to existing settings. Explain that IP/device network metadata is visible to remote servers when online requests occur.
- [ ] **Step 4: Run focused UI tests** and inspect rendered content to ensure secrets never appear.
- [ ] **Step 5: Run** `./gradlew :app:testFdroidQaUnitTest :app:connectedFdroidQaAndroidTest`; confirm all tests pass.
- [ ] **Step 6: Commit** as `feat: add online privacy center`.

## Review Focus Tests

Cover all five cases in `HttpDisclosureTest` and `OnlinePrivacyFlowsTest`; treat unresolved custom expressions conservatively and never claim that a custom payload is fully understood.
