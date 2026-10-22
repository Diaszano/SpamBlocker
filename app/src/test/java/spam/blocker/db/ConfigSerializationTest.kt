package spam.blocker.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import spam.blocker.service.bot.Manual
import spam.blocker.util.MyJson
import spam.blocker.config.Category
import spam.blocker.config.CategorySelection
import spam.blocker.config.Configs
import spam.blocker.config.NumberRules

class ConfigSerializationTest {
    @Test
    fun queryApiConfigRoundTrips() {
        val api = QueryApi(
            desc = "local query",
            enabled = true,
            actions = emptyList(),
        )

        val restored = MyJson.decodeFromString<QueryApi>(MyJson.encodeToString(api))

        assertEquals(api, restored)
    }

    @Test
    fun reportApiConfigRoundTrips() {
        val api = ReportApi(
            desc = "local report",
            enabled = true,
            autoReportTypes = AutoReportTypes.STIR or AutoReportTypes.Regex,
            autoReportRegexFilter = "bank|loan",
            actions = emptyList(),
        )

        val restored = MyJson.decodeFromString<ReportApi>(MyJson.encodeToString(api))

        assertEquals(api, restored)
    }

    @Test
    fun manualBotConfigRoundTripsWithCustomTags() {
        val bot = Bot(
            desc = "local bot",
            trigger = Manual(),
            actions = emptyList(),
            customTags = mutableMapOf("cursor" to "42"),
        )

        val restored = bot.serialize().parseBot()

        assertEquals(bot.id, restored.id)
        assertEquals(bot.desc, restored.desc)
        assertEquals(Manual::class, restored.trigger::class)
        assertEquals(bot.actions, restored.actions)
        assertEquals(bot.customTags, restored.customTags)
    }

@Test
fun regexBackupArchiveRoundTripsSelectedCategoryAndRules() {
val rule = RegexRule(pattern = "qa-valid-backup-778899", description = "QA restored rule")
val config = Configs().apply {
categories = CategorySelection(emptySet()).select(Category.REGEX_RULES)
numberRules = NumberRules().apply { rules.add(rule) }
}

val restored = Configs.fromByteArray(config.toByteArray())
val restoredRule = restored.numberRules!!.rules.single()
assertEquals(setOf(Category.REGEX_RULES), restored.categories!!.all)
assertEquals(rule.pattern, restoredRule.pattern)
assertEquals(rule.description, restoredRule.description)
}
}
