package spam.blocker.service.bot

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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
            assertTrue(bot.desc.isNotEmpty())

            // Ensure the bot can be serialized without errors
            val json = bot.serialize()
            assertTrue(json.isNotEmpty())
        }
    }
}
