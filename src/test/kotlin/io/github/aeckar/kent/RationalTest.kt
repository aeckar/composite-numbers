package io.github.aeckar.kent

import io.github.aeckar.kent.constants.HUGE_STRING
import io.github.aeckar.kent.constants.random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RationalTest {
    @Nested
    inner class Conversion {
        private val posStr = random.nextInt().toString()
        private val negStr = (-random.nextInt()).toString()
        private val max32Str = Int.MAX_VALUE.toString()
        private val min32Str = Int.MIN_VALUE.toString()
        private val max64Str = Long.MAX_VALUE.toString()
        private val min64Str = Long.MIN_VALUE.toString()

        private val neg1 = Rational("-1")

        @Test
        fun string() {
            /*
            assertEquals(posStr, Rational(posStr).toString())
            assertEquals(negStr, Rational(negStr).toString())
            assertEquals(max32Str, Rational(max32Str).toString())
            assertEquals(min32Str, Rational(min32Str).toString())
            assertEquals(max64Str, Rational(max64Str).toString())
            assertEquals(min64Str, Rational(min64Str).toString())

             */
            assertEquals("1", Rational("1").toString())
            assertEquals("0", Rational("0").toString())
            assertEquals("-1", neg1.toString())

            assertEquals(Rational.NEGATIVE_ONE, neg1)

            assertThrows<NumberFormatException> { Rational("") }
            assertThrows<NumberFormatException> { Rational("3.1.4") }
            assertThrows<NumberFormatException> { Rational("--3.14") }
            assertThrows<NumberFormatException> { Rational("((3.14))") }
            assertThrows<NumberFormatException> { Rational("(3.14") }
            assertDoesNotThrow { Rational(HUGE_STRING) }
        }
    }

    @Nested
    inner class Arithmetic {

    }
}