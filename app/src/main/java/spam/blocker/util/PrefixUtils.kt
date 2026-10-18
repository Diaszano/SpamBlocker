package spam.blocker.util

object PrefixUtils {
    /**
     * Suggests a wildcard prefix pattern for a phone number (e.g. for blocking telemarketing ranges).
     * Masks the trailing digits (typically subscriber line suffix of 3-4 digits).
     */
    fun suggestPrefixPattern(rawNumber: String): String {
        val trimmed = rawNumber.trim()
        if (trimmed.isEmpty()) return ".*"

        val dropCount = when {
            trimmed.length >= 10 -> 4
            trimmed.length in 6..9 -> 3
            trimmed.length in 4..5 -> 2
            else -> 0
        }

        val prefix = if (dropCount > 0 && trimmed.length > dropCount) {
            trimmed.substring(0, trimmed.length - dropCount)
        } else {
            trimmed
        }

        return "$prefix.*"
    }
}
