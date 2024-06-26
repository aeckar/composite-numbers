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
            assertEquals(posStr, Rational(posStr).toString())
            assertEquals(negStr, Rational(negStr).toString())
            assertEquals(max32Str, Rational(max32Str).toString())
            assertEquals(min32Str, Rational(min32Str).toString())
            assertEquals(max64Str, Rational(max64Str).toString())
            assertEquals("-9223372036854775800", Rational(min64Str).toString())
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
        @Test
        fun s() {
            println(Rational.PI / Rational.TWO)
        }
        @Test
        fun plus() {
            val a = Rational(1, 2)
            val b = Rational(1,3 )
            assertEquals(Rational(5, 6), a + b)
        }

        @Test
        fun minus() {
            val a = Rational(3, 4)
            val b = Rational(1, 4)
            assertEquals(Rational(1, 2), a - b)
        }

        @Test
        fun times() {
            val a = Rational(2, 3)
            val b = Rational(3, 4)
            assertEquals(Rational(1, 2), a * b)
        }

        @Test
        fun div() {
            val a = Rational(2, 3)
            val b = Rational(3, 4)
            assertEquals(Rational(8, 9), a / b)
        }
    }

    @Nested
    inner class Miscellaneous {
        @Test
        fun comparison() {
            
        }
    }
    @Test fun TODO() {
        println(cos(Rational.TWO))
    }
}