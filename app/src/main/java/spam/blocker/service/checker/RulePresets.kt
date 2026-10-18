package spam.blocker.service.checker

import spam.blocker.R
import spam.blocker.def.Def

data class RulePreset(
    val titleRes: Int,
    val pattern: String,
    val isBlacklist: Boolean = true,
    val flags: Int = Def.FLAG_REGEX_RAW_NUMBER,
    val description: String = ""
)

object RulePresets {
    const val PAT_BR_TELEMARKETING = "^(\\+?55)?0?30[34][0-9]+.*"
    const val PAT_SATELLITE_PREMIUM = "^\\+(881|882|883|870)[0-9]+.*"
    const val PAT_SMS_SUSPICIOUS_URL = "(https?://|www\\.)?([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}|bit\\.ly|tinyurl\\.com|t\\.co|is\\.gd|cutt\\.ly|rb\\.gy|shorte\\.st|ow\\.ly)/?[a-zA-Z0-9_.-]*"

    fun getNumberPresets(): List<RulePreset> {
        return listOf(
            RulePreset(
                titleRes = R.string.preset_br_telemarketing,
                pattern = PAT_BR_TELEMARKETING,
                isBlacklist = true,
                flags = Def.FLAG_REGEX_RAW_NUMBER,
                description = "Brazil 0303/0304 telemarketing"
            ),
            RulePreset(
                titleRes = R.string.preset_satellite_toll,
                pattern = PAT_SATELLITE_PREMIUM,
                isBlacklist = true,
                flags = Def.FLAG_REGEX_RAW_NUMBER,
                description = "Satellite & high-tariff numbers"
            )
        )
    }

    fun getContentPresets(): List<RulePreset> {
        return listOf(
            RulePreset(
                titleRes = R.string.preset_suspicious_url,
                pattern = PAT_SMS_SUSPICIOUS_URL,
                isBlacklist = true,
                flags = Def.DefaultRegexFlags,
                description = "SMS phishing shorteners & direct IPs"
            )
        )
    }
}
