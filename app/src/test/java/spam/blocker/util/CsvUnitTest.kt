package spam.blocker.util

import java.io.PushbackReader
import java.io.StringReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CsvUnitTest {

    @Test
    fun parsesCommaDelimitedQuotedFieldsAndCrLf() {
        val csv = parse("pattern,description\r\n123,\"sale, offer\"\r\n")

        assertEquals(listOf("pattern", "description"), csv.headers)
        assertEquals(listOf(listOf("123", "sale, offer")), csv.rows)
    }

    @Test
    fun detectsSemicolonDelimiter() {
        val csv = parse("pattern;priority\n123;4")

        assertEquals(listOf("pattern", "priority"), csv.headers)
        assertEquals(listOf(listOf("123", "4")), csv.rows)
    }

    @Test
    fun detectsPipeDelimiter() {
        val csv = parse("pattern|priority\n123|4")

        assertEquals(listOf("pattern", "priority"), csv.headers)
        assertEquals(listOf(listOf("123", "4")), csv.rows)
    }

    @Test
    fun keepsTheWholeHeaderAfterUtf8Bom() {
        val csv = parse("\uFEFFpattern,priority\n123,4")

        assertEquals(listOf("pattern", "priority"), csv.headers)
        assertEquals(listOf(listOf("123", "4")), csv.rows)
    }

    @Test
    fun includesLastRecordWithoutTrailingNewline() {
        val csv = parse("pattern,priority\n123,4")

        assertEquals(listOf(listOf("123", "4")), csv.rows)
    }

    private fun parse(input: String): Csv = CSVParser(PushbackReader(StringReader(input))).parse()
}
