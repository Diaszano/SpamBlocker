package spam.blocker.service.bot

import spam.blocker.R
import spam.blocker.db.Bot

data class WorkflowTemplate(
    val titleRes: Int,
    val descriptionRes: Int,
    val factory: () -> Bot,
)

object BotTemplates {
    fun getPredefinedTemplates(): List<WorkflowTemplate> {
        return listOf(
            WorkflowTemplate(
                titleRes = R.string.bot_template_prune_history,
                descriptionRes = R.string.bot_template_prune_history_desc,
                factory = {
                    Bot(
                        desc = "Auto Prune History (30 days)",
                        trigger = Schedule().apply {
                            enabled = true
                        },
                        actions = listOf(
                            PruneHistory(expiry = 30)
                        )
                    )
                }
            ),
            WorkflowTemplate(
                titleRes = R.string.bot_template_webhook_alert,
                descriptionRes = R.string.bot_template_webhook_alert_desc,
                factory = {
                    Bot(
                        desc = "Webhook on Blocked Call",
                        trigger = CallEvent(),
                        actions = listOf(
                            HttpRequest(
                                method = HTTP_POST,
                                url = "https://example.com/webhook",
                                header = "Content-Type: application/json",
                                body = """{"event": "call_blocked", "number": "{raw_number}"}"""
                            )
                        )
                    )
                }
            ),
            WorkflowTemplate(
                titleRes = R.string.bot_template_auto_backup,
                descriptionRes = R.string.bot_template_auto_backup_desc,
                factory = {
                    Bot(
                        desc = "Scheduled Auto Backup",
                        trigger = Schedule().apply {
                            enabled = true
                        },
                        actions = listOf(
                            BackupExport(),
                            WriteFile(filename = "SpamBlocker.auto.gz"),
                        )
                    )
                }
            )
        )
    }
}
