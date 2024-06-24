package io.github.aeckar.kent

import io.github.aeckar.kent.constants.HUGE_STRING
import io.github.aeckar.kent.constants.random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import kotlin.random.nextInt
import kotlin.test.assertEquals

private fun assertEquals2c(x: Int128, y: Int128) {
    try {
        assertEquals(x, y)
    } catch (e: AssertionFailedError) {
        println("Expected :" + x.twosComplement())
        println("Actual   :" + y.twosComplement())
        throw e
    }
}

class Int128Test {
    private fun i128() = Int128(random)

    @Nested
    inner class Conversion {
        private val posStr = random.nextInt().toString()
        private val negStr = (-random.nextInt()).toString()
        private val max32Str = Int.MAX_VALUE.toString()
        private val min32Str = Int.MIN_VALUE.toString()
        private val max64Str = Long.MAX_VALUE.toString()
        private val min64Str = Long.MIN_VALUE.toString()

        private val neg1 = Int128("-1")

        @Test
        fun string() {
            assertEquals(posStr, Int128(posStr).toString())
            assertEquals(negStr, Int128(negStr).toString())
            assertEquals(max32Str, Int128(max32Str).toString())
            assertEquals(min32Str, Int128(min32Str).toString())
            assertEquals(max64Str, Int128(max64Str).toString())
            assertEquals(min64Str, Int128(min64Str).toString())
            assertEquals("1", Int128("1").toString())
            assertEquals("0", Int128("0").toString())
            assertEquals("-1", neg1.toString())

            assertEquals(Int128.NEGATIVE_ONE, neg1)

            assertThrows<NumberFormatException> { Int128("") }
            assertThrows<NumberFormatException> { Int128("3.14") }
            assertThrows<ArithmeticException> { Int128(HUGE_STRING) }
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
            assertEquals2c(Int128.ZERO, Int128(random) shl gte128)
            assertThrows<IllegalArgumentException> { Int128.ONE shl -1 }
        }

        @Test
        fun shr() {
            val random128 = Int128(random)

            assertEquals2c(Int128(pos16 shr shift16), Int128(pos16) shr shift16)
            assertEquals2c(Int128(neg16 shr shift16), Int128(neg16) shr shift16)
            assertEquals2c(Int128(random128.sign shr 1), random128 shr gte128)
            assertThrows<IllegalArgumentException> { Int128.ONE shr -1 }
        }

        @Test
        fun ushr() {
            assertEquals2c(Int128(pos16 ushr shift16), Int128(pos16) ushr shift16)
            assertEquals2c(Int128(-1 ushr shift16, -1, -1, neg16 shr shift16), Int128(neg16) ushr shift16)
            assertEquals2c(Int128.ZERO, Int128(random) ushr gte128)
            assertThrows<IllegalArgumentException> { Int128.ONE ushr -1 }
        }
    }

    @Nested
    inner class Arithmetic {
        private val int64 = random.nextLong()

        @Test
        fun unaryMinus() {
            assertEquals2c(Int128(-int64), -Int128(int64))
            assertEquals(-int64, -Int128(int64).toLong())
            assertThrows<ArithmeticException> { -Int128.MIN_VALUE }
        }

        @Test
        fun plus() {
            do try {    // Ignore random tests where the result overflows
                val i128a = i128()
                val i128b = i128()
                val sum = i128a + i128b
                assertEquals2c(i128a, sum - i128b)
                assertEquals2c(i128b, sum - i128a)
                assertThrows<ArithmeticException> { Int128.MAX_VALUE + Int128.ONE }
                break
            } catch (_: ArithmeticException) { /* no-op */ } while (true)
        }

        @Test
        fun minus() {
            do try {    // Ignore random tests where the result overflows
                val i128a = i128()
                val i128b = i128()
                val difference = i128a - i128b
                assertEquals2c(i128a, difference + i128b)
                assertEquals2c(i128b, i128a - difference)
                assertThrows<ArithmeticException> { Int128.MIN_VALUE - Int128.ONE }
                break
            } catch (_: ArithmeticException) { /* no-op */ }while (true)
        }

        @Test
        fun times() {
            val i128a = Int128(5000)
            val i128b = Int128(1250)
            val product = i128a * i128b

            assertEquals2c(i128a, product / i128b)
            assertEquals2c(i128b, product / i128a)
            assertDoesNotThrow { Int128.MAX_VALUE * Int128.ONE }
            assertThrows<ArithmeticException> { Int128.MAX_VALUE * Int128.TWO }
        }

        @Test
        fun div() { // Comment out to-long conversion in divide() during testing
            val i128a = Int128(5000)
            val i128b = Int128(1250)
            val quotient = i128a / i128b

            assertEquals2c(Int128(4), quotient)
            assertEquals2c(i128a, quotient * i128b)
            assertEquals2c(i128b, i128a / quotient)
            assertDoesNotThrow { Int128.ONE / Int128.MAX_VALUE }
            assertThrows<ArithmeticException> { Int128.ONE / Int128.ZERO }

            assertEquals(Int128.TEN, Int128(101) / Int128.TEN)
            assertEquals(Int128.TEN, Int128(109) / Int128.TEN)
        }
    }
}