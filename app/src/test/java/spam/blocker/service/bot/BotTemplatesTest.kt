package spam.blocker.service.bot

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import spam.blocker.R
import spam.blocker.db.parseBot
import spam.blocker.db.serialize

class BotTemplatesTest {
    @Test
    fun testAllTemplatesConstructValidChains() {
        val templates = BotTemplates.getPredefinedTemplates()
        assertTrue(templates.isNotEmpty())
        templates.forEach { template ->
            val bot = template.factory()
            assertNotNull(bot.trigger)
            assertTrue(bot.actions.isNotEmpty())
            assertTrue(
                bot.actions.allChainable(),
                "${bot.desc}: ${bot.actions.map { it::class.simpleName }}",
            )
            assertTrue(bot.desc.isNotEmpty())

            // Ensure the bot can be serialized without errors
            val json = bot.serialize()
            assertTrue(json.isNotEmpty())
            assertEquals(json, json.parseBot().serialize())
        }
    }

    @Test
    fun autoBackupTemplateWritesExportedConfigToAFile() {
        val bot = BotTemplates.getPredefinedTemplates()
            .single { it.titleRes == R.string.bot_template_auto_backup }
            .factory()

        assertTrue(bot.actions[0] is BackupExport)
        assertTrue(bot.actions.getOrNull(1) is WriteFile)
        assertEquals("SpamBlocker.auto.gz", (bot.actions.getOrNull(1) as? WriteFile)?.filename)
    }
}
