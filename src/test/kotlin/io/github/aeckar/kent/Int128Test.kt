package io.github.aeckar.kent

import io.github.aeckar.kent.Int128.Companion.MAX_VALUE
import io.github.aeckar.kent.Int128.Companion.MIN_VALUE
import io.github.aeckar.kent.Int128.Companion.NEGATIVE_ONE
import io.github.aeckar.kent.Int128.Companion.ONE
import io.github.aeckar.kent.Int128.Companion.TEN
import io.github.aeckar.kent.Int128.Companion.TWO
import io.github.aeckar.kent.Int128.Companion.ZERO
import io.github.aeckar.kent.Int128.Companion.factorial
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import kotlin.random.nextInt
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun assertEquals2c(x: Int128, y: Int128) {
    try {
        assertEquals(x, y)
    } catch (e: AssertionFailedError) {
        println("Expected :" + x.binaryString())
        println("Actual   :" + y.binaryString())
        throw e
    }
}

class Int128Test {
    @Test
    fun comparison() {
        assertTrue { NEGATIVE_ONE < MAX_VALUE }
        assertTrue { ONE > MIN_VALUE }
    }

    @Nested
    inner class StringConversion {
        private val posStr = random.nextInt(1..Int.MAX_VALUE).toString()
        private val negStr = random.nextInt(Int.MIN_VALUE..-1).toString()
        private val max32Str = Int.MAX_VALUE.toString()
        private val min32Str = Int.MIN_VALUE.toString()
        private val max64Str = Long.MAX_VALUE.toString()
        private val min64Str = Long.MIN_VALUE.toString()

        private val neg1 = Int128("-1")

        @Test
        fun base10() {
            assertEquals(posStr, Int128(posStr).toString())
            assertEquals(negStr, Int128(negStr).toString())
            assertEquals(max32Str, Int128(max32Str).toString())
            assertEquals(min32Str, Int128(min32Str).toString())
            assertEquals(max64Str, Int128(max64Str).toString())
            assertEquals(min64Str, Int128(min64Str).toString())
            assertEquals("1", Int128("1").toString())
            assertEquals("0", Int128("0").toString())
            assertEquals("-1", neg1.toString())

            assertEquals(NEGATIVE_ONE, neg1)

            assertThrows<CompositeFormatException> { Int128("") }
            assertThrows<CompositeFormatException> { Int128("3.14") }
            assertThrows<CompositeArithmeticException> { Int128(HUGE_STRING) }
        }

        @Test
        fun baseN() {
            // TODO
        }
    }

    @Nested
    inner class BitwiseShift {
        private val pos16 = random.nextInt(0..Short.MAX_VALUE)
        private val neg16 = random.nextInt(Short.MIN_VALUE..-1)
        private val shift16 = random.nextInt(0..<16)
        private val gte128 = random.nextInt(128..160)

        @Test
        fun shl() {
            assertEquals2c(Int128(pos16 shl shift16), Int128(pos16) shl shift16)
            assertEquals2c(Int128(neg16 shl shift16), Int128(neg16) shl shift16)
            assertEquals2c(ZERO, random.nextInt128() shl gte128)
            assertThrows<IllegalArgumentException> { ONE shl -1 }
        }

        @Test
        fun shr() {
            val value = random.nextInt128()

            assertEquals2c(Int128(pos16 shr shift16), Int128(pos16) shr shift16)
            assertEquals2c(Int128(neg16 shr shift16), Int128(neg16) shr shift16)
            assertEquals2c(Int128(value.sign shr 1), value shr gte128)
            assertThrows<IllegalArgumentException> { ONE shr -1 }
        }

        @Test
        fun ushr() {
            assertEquals2c(Int128(pos16 ushr shift16), Int128(pos16) ushr shift16)
            assertEquals2c(Int128(-1 ushr shift16, -1, -1, neg16 shr shift16), Int128(neg16) ushr shift16)
            assertEquals2c(ZERO, random.nextInt128() ushr gte128)
            assertThrows<IllegalArgumentException> { ONE ushr -1 }
        }
    }

    @Nested
    inner class Arithmetic {
        private val int64 = random.nextLong()

        @Test
        fun unaryMinus() {
            assertEquals2c(Int128(-int64), -Int128(int64))
            assertEquals(-int64, -Int128(int64).toLong())
            assertThrows<CompositeArithmeticException> { -MIN_VALUE }
        }

        @Test
        fun plus() {
            do try {    // Ignore random tests where the result overflows
                val a = random.nextInt128()
                val b = random.nextInt128()
                val sum = a + b
                assertEquals2c(a, sum - b)
                assertEquals2c(b, sum - a)
                assertDoesNotThrow { MAX_VALUE + ZERO }
                assertDoesNotThrow { MAX_VALUE + NEGATIVE_ONE }
                assertThrows<CompositeArithmeticException> { MAX_VALUE + ONE }
                break
            } catch (_: CompositeArithmeticException) { /* no-op */ } while (true)
        }

        @Test
        fun minus() {
            do try {    // Ignore random tests where the result overflows
                val a = random.nextInt128()
                val b = random.nextInt128()
                val difference = a - b
                assertEquals2c(a, difference + b)
                assertEquals2c(b, a - difference)
                assertDoesNotThrow { MIN_VALUE - ZERO }
                assertDoesNotThrow { MIN_VALUE - NEGATIVE_ONE }
                assertThrows<CompositeArithmeticException> { MIN_VALUE - ONE }
                break
            } catch (_: CompositeArithmeticException) { /* no-op */ }while (true)
        }

        @Test
        fun times() {
            val a = Int128(5000)
            val b = Int128(1250)
            val product = a * b

            assertEquals2c(a, product / b)
            assertEquals2c(b, product / a)
            assertThrows<CompositeArithmeticException> { MAX_VALUE * TWO }
            assertDoesNotThrow { MAX_VALUE * ONE }
        }

        @Test
        fun div() {
            val a = Int128(5000)
            val b = Int128(1250)
            val quotient = a / b
            assertEquals2c(Int128(4), quotient)
            assertEquals2c(a, quotient * b)
            assertEquals2c(b, a / quotient)
            assertDoesNotThrow { ONE / MAX_VALUE }
            assertThrows<CompositeArithmeticException> { ONE / ZERO }

            // Inexact results
            assertEquals(TEN, Int128(101) / TEN)
            assertEquals(TEN, Int128(109) / TEN)
        }

        @Test
        fun rem() {
            val a = Int128(5000)
            val b = Int128(1267)
            val quotientWholePart = Int128(3)
            val remainder = a % b
            assertEquals2c(Int128(1199), remainder)
            assertEquals2c(a, (b * quotientWholePart) + remainder)
            assertEquals2c(b, (a - remainder) / quotientWholePart)
            assertDoesNotThrow { ONE % MAX_VALUE }
            assertThrows<CompositeArithmeticException> { ONE % ZERO }

            // Inexact results
            assertEquals(ONE, Int128(101) % TEN)
            assertEquals(Int128(9), Int128(109) % TEN)
        }

        @Test
        fun pow() {
            val a = Int128(19)
            assertEquals2c(Int128(130321), a.pow(4))
            assertThrows<CompositeArithmeticException> { a.pow(Int.MAX_VALUE) }
        }

        @Test
        fun factorial() {
            assertEquals2c(ONE, factorial(0))
            assertEquals2c(ONE, factorial(1))
            assertEquals2c(Int128("8683317618811886495518194401280000000"), factorial(33))
            assertThrows<CompositeArithmeticException> { factorial(34) }
        }
    }
}