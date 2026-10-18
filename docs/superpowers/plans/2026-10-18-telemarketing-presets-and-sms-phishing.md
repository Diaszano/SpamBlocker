# Telemarketing Presets and SMS Link Detection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add one-tap presets for official national telemarketing prefixes (e.g. Brazil 0303/0304) to Regex rules and introduce an intelligent SMS phishing/suspicious URL link detector rule.

**Architecture:** Extend regex rule creation dialogs with a "Presets" dropdown that populates pre-tested patterns without manual regex typing. In `Checker.kt`, introduce an optional content rule check for suspicious URL patterns and shorteners (`bit.ly`, `tinyurl`, IP-based URLs) on incoming SMS from non-contacts.

**Tech Stack:** Kotlin, Jetpack Compose, Room SQLite database, Android CallScreeningService & SmsReceiver.

**Spec:** Local-first, privacy-preserving, zero external telemetry. All regexes execute on-device.

## Global Constraints

- Product code changes strictly in `app/src/main/java/spam/blocker/ui/` and `app/src/main/java/spam/blocker/service/`.
- Preserve existing database schema and table structures; presets are standard `RegexRule` records inserted into existing tables.
- All new user-facing strings in English in `res/values/strings_ui_ux.xml` with `translatable="false"`. No translation bundle edits.
- Maintain dark compact theme and existing touch-target minimums (48dp).

---

### Task 1: Rule Preset Generator and Dialog Integration

**Files:**
- Create: `app/src/main/java/spam/blocker/service/checker/RulePresets.kt`
- Modify: `app/src/main/java/spam/blocker/ui/setting/regex/EditRegexDialog.kt`
- Test: `app/src/test/java/spam/blocker/service/checker/RulePresetsTest.kt`

**Interfaces:**
- Produces: `object RulePresets { fun getNumberPresets(): List<RulePreset>; fun getContentPresets(): List<RulePreset> }`
- `data class RulePreset(val nameRes: Int, val pattern: String, val isBlacklist: Boolean, val flags: Int, val description: String)`

- [x] **Step 1: Write unit test for preset regex patterns**

```kotlin
package spam.blocker.service.checker

import org.junit.Assert.assertTrue
import org.junit.Test

class RulePresetsTest {
    @Test
    fun testBrazilTelemarketing0303Matches() {
        val pattern = RulePresets.PAT_BR_TELEMARKETING.toRegex()
        assertTrue(pattern.containsMatchIn("03031234567"))
        assertTrue(pattern.containsMatchIn("+553031234567"))
        assertTrue(pattern.containsMatchIn("03049876543"))
    }

    @Test
    fun testSuspiciousUrlPatternMatches() {
        val pattern = RulePresets.PAT_SMS_SUSPICIOUS_URL.toRegex(RegexOption.IGNORE_CASE)
        assertTrue(pattern.containsMatchIn("Acesse bit.ly/promo agora"))
        assertTrue(pattern.containsMatchIn("Seu pacote: http://192.168.1.1/track"))
        assertTrue(pattern.containsMatchIn("Clique aqui: tinyurl.com/xyz"))
    }
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.service.checker.RulePresetsTest"`
Expected: FAIL (unresolved reference `RulePresets`)

- [x] **Step 3: Implement `RulePresets.kt` and add Preset picker in `EditRegexDialog.kt`**

Add preset definitions and a compact "Presets" chip/button in `EditRegexDialog` that populates `pattern`, `patternFlags`, and `description`.

- [x] **Step 4: Run test to verify it passes**

Run: `./gradlew testFdroidDebugUnitTest --tests "spam.blocker.service.checker.RulePresetsTest"`
Expected: PASS

- [x] **Step 5: Verify build & commit**

Run: `./gradlew assembleDebug lint`
Commit: `feat: add telemarketing and link detection rule presets`
