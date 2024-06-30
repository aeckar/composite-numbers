package io.github.aeckar.kent

import io.github.aeckar.kent.Int128.Companion
import io.github.aeckar.kent.Rational.Companion.MAX_VALUE
import io.github.aeckar.kent.Rational.Companion.MIN_VALUE
import io.github.aeckar.kent.Rational.Companion.NEGATIVE_ONE
import io.github.aeckar.kent.Rational.Companion.ONE
import io.github.aeckar.kent.Rational.Companion.TWO
import io.github.aeckar.kent.Rational.Companion.ZERO
import io.github.aeckar.kent.Rational.Companion.factorial
import io.github.aeckar.kent.functions.ceil
import io.github.aeckar.kent.functions.floor
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.math.PI
import kotlin.random.nextInt
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RationalTest {
    @Test
    fun comparison() {
        assertTrue { NEGATIVE_ONE < MAX_VALUE }
        assertTrue { ONE > MIN_VALUE }
    }

    @Test
    fun identity() {
        val randA = random.nextInt(0..Int.MAX_VALUE)
        val randB = random.nextInt(0..Int.MAX_VALUE)
        assertEquals(randA over randA, randB over randB)
        assertEquals(0 over randA, 0 over randB)
        assertEquals(PI, -(-PI))
    }


    @Nested
    inner class StringConversion {
        @Test
        fun base10() {
            val posStr = random.nextInt(1..Int.MAX_VALUE).toString()
            val negStr = random.nextInt(Int.MIN_VALUE..-1).toString()
            val max32Str = Int.MAX_VALUE.toString()
            val min32Str = Int.MIN_VALUE.toString()
            val max64Str = Long.MAX_VALUE.toString()
            val min64Str = Long.MIN_VALUE.toString()
            val neg1 = Rational("-1")

            assertEquals(posStr, Rational(posStr).toString())
            assertEquals(negStr, Rational(negStr).toString())
            assertEquals(max32Str, Rational(max32Str).toString())
            assertEquals(min32Str, Rational(min32Str).toString())
            assertEquals(max64Str, Rational(max64Str).toString())
            assertEquals("-922337203685477580e1", Rational(min64Str).toString()) // Lossy
            assertEquals("1", Rational("1").toString())
            assertEquals("0", Rational("0").toString())
            assertEquals("-1", neg1.toString())
            assertEquals(NEGATIVE_ONE, neg1)

            assertEquals("17/31", Rational("17/31").toString())     // Fraction
            assertEquals("0.05", Rational(".05").toString())        // Leading dot
            assertEquals("0.000000000000000014e-351", Rational("14e-367").toString()) // Exponent

            assertThrows<CompositeFormatException> { Rational("") }
            assertThrows<CompositeFormatException> { Rational("3.1.4") }
            assertThrows<CompositeFormatException> { Rational("--3.14") }
            assertThrows<CompositeFormatException> { Rational("((3.14))") }
            assertThrows<CompositeFormatException> { Rational("(3.14") }
            assertDoesNotThrow { Rational(HUGE_STRING) }
        }

        @Test
        fun sciNotation() {
            TODO()
        }
    }

    @Nested
    inner class BasicArithmetic {
        @Test
        fun plus() {
            val a = 1 over 2
            val b = 1 over 3
            val sum = a + b
            assertEquals(5 over 6, sum)
            assertEquals(b, sum - a)
            assertEquals(a, sum - b)
            assertDoesNotThrow { MAX_VALUE + ZERO }
            assertDoesNotThrow { MAX_VALUE + ONE }
            assertThrows<CompositeArithmeticException> { MAX_VALUE + MAX_VALUE }
        }

        @Test
        fun minus() {
            val a = 3 over 4
            val b = 1 over 4
            val difference = a - b
            val hugeNeg = Rational(Long.MIN_VALUE + 1, 1, Int.MAX_VALUE)
            assertEquals(1 over 2, difference)
            assertEquals(a, b + difference)
            assertEquals(b, a - difference)
            assertDoesNotThrow { MIN_VALUE - ZERO }
            assertDoesNotThrow { MIN_VALUE - ONE }
            assertThrows<CompositeArithmeticException> { MIN_VALUE + MIN_VALUE }    // Long.MIN_VALUE is scaled up by 1
            assertThrows<CompositeArithmeticException> { hugeNeg + hugeNeg }
        }

        @Test
        fun times() {
            val a = 2 over 3
            val b = 3 over 4
            val product = a * b
            assertEquals(1 over 2, product)
            assertEquals(a, product / b)
            assertEquals(b, product / a)
            assertDoesNotThrow { MAX_VALUE * ONE }
            assertThrows<CompositeArithmeticException> { MAX_VALUE * TWO }
        }

        @Test
        fun div() {
            val a = 2 over 3
            val b = 3 over 4
            val quotient = a / b
            assertEquals(8 over 9, quotient)
            assertEquals(a, quotient * b)
            assertThrows<CompositeArithmeticException> { a / ZERO }
        }

        @Test
        fun rem() {
            val a = 5 over 3
            val b = 2 over 3
            assertEquals(1 over 3, a % b)
            assertEquals(0 over 1, a % a)
            assertThrows<CompositeArithmeticException> { a % ZERO }
        }

        @Test
        fun pow() {
            val base = 2 over 3
            assertEquals(1 over 1, base.pow(0))
            assertEquals(base, base.pow(1))
            assertEquals(8 over 27, base.pow(3))
            assertEquals(27 over 8, base.pow(-3))
        }

        @Test
        fun factorial() {
            assertEquals(ONE, factorial(0))
            assertEquals(ONE, factorial(1))
            assertEquals(Rational("8683317618811886483000000000000000000"), factorial(33))
        }
    }

    @Nested
    inner class ElementaryFunctions {
        @Test
        fun ln() {
            // TODO
        }

        @Test
        fun sin() {
            // TODO
        }

        @Test
        fun cos() {
            // TODO
        }

        @Test
        fun sinh() {
            // TODO
        }

        @Test
        fun cosh() {
            // TODO
        }

        @Test
        fun arcsin() {
            // TODO
        }

        @Test
        fun arctan() {
            // TODO
        }
    }

    @Nested
    inner class Rounding {
        private val min64 = Rational(Long.MIN_VALUE)

        @Test
        fun ceil() {
            assertEquals(TWO, ceil(5 over 3))
            assertEquals(-ONE, ceil(-5 over 3))
            assertEquals(min64, ceil(Long.MIN_VALUE over 1))
        }

        @Test
        fun floor() {
            assertEquals(ONE, floor(5 over 3))
            assertEquals(-TWO, floor(-5 over 3))
            assertEquals(min64, ceil(Long.MIN_VALUE over 1))
        }
    }
}