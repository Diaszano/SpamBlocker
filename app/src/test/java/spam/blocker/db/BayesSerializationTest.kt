package spam.blocker.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BayesSerializationTest {
    @Test
    fun testExportImportRoundTrip() {
        val samples = listOf(
            BayesSample(id = 1, hash = 1234, isSpam = true, content = "Ganhe dinheiro agora"),
            BayesSample(id = 2, hash = 5678, isSpam = false, content = "Codigo de confirmacao 987")
        )
        val json = BayesTable.serializeSamples(samples)
        val parsed = BayesTable.deserializeSamples(json)
        assertEquals(2, parsed.size)
        assertEquals(samples[0].content, parsed[0].content)
        assertEquals(samples[1].isSpam, parsed[1].isSpam)
        assertEquals(samples[0].hash, parsed[0].hash)
    }
}
