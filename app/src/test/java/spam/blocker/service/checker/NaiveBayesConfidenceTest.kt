package spam.blocker.service.checker

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import spam.blocker.def.Def

class NaiveBayesConfidenceTest {

    @Test
    fun testConfidenceRoundTrip() {
        val result = ByNaiveBayes(Def.RESULT_BLOCKED_BY_NAIVE_BAYES, confidence = 95)
        assertEquals(95, result.confidence)
        assertEquals("95", result.reasonToDb())

        val uncalibrated = ByNaiveBayes(Def.RESULT_BLOCKED_BY_NAIVE_BAYES, confidence = 0)
        assertEquals("", uncalibrated.reasonToDb())
    }

    @Test
    fun testConfidenceCalculation() {
        val probSpam = 0.942
        val confidenceSpam = (probSpam * 100).toInt()
        assertEquals(94, confidenceSpam)

        val probHam = 0.08
        val confidenceHam = ((1.0 - probHam) * 100).toInt()
        assertEquals(92, confidenceHam)
    }
}
