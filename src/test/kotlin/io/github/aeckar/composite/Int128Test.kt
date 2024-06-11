package io.github.aeckar.composite

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import java.math.BigInteger
import kotlin.random.Random
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
    private val rng = Random(0)

    private fun i128() = Int128(rng)

    @Nested
    inner class Conversion {
        private val posStr = rng.nextInt().toString()
        private val negStr = (-rng.nextInt()).toString()
        private val max32Str = Int.MAX_VALUE.toString()
        private val min32Str = Int.MIN_VALUE.toString()
        private val max64Str = Long.MAX_VALUE.toString()
        private val min64Str = Long.MIN_VALUE.toString()

        private val posInt = rng.nextInt().toBigInteger()
        private val negInt = (-rng.nextInt()).toBigInteger()
        private val max32Int = Int.MAX_VALUE.toBigInteger()
        private val min32Int = Int.MIN_VALUE.toBigInteger()
        private val max64Int = Long.MAX_VALUE.toBigInteger()
        private val min64Int = Long.MIN_VALUE.toBigInteger()
        private val oneInt = BigInteger.ONE
        private val zeroInt = BigInteger.ZERO
        private val neg1Int = (-1).toBigInteger()

        private val largeStr = "1461501637330902918203684832716283019655932542976"

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
            assertEquals("-1", Int128("-1").toString())

            assertThrows<NumberFormatException> { Int128("") }
            assertThrows<NumberFormatException> { Int128("3.14") }
            assertThrows<ArithmeticException> { Int128(largeStr) }
        }

        @Test
        fun big_integer() {
            assertEquals(posInt, Int128(posInt).toBigInteger())
            assertEquals(negInt, Int128(negInt).toBigInteger())
            assertEquals(max32Int, Int128(max32Int).toBigInteger())
            assertEquals(min32Int, Int128(min32Int).toBigInteger())
            assertEquals(max64Int, Int128(max64Int).toBigInteger())
            assertEquals(min64Int, Int128(min64Int).toBigInteger())
            assertEquals(oneInt, Int128(oneInt).toBigInteger())
            assertEquals(zeroInt, Int128(zeroInt).toBigInteger())
            assertEquals(neg1Int, Int128(neg1Int).toBigInteger())

            assertThrows<ArithmeticException> { Int128(BigInteger(largeStr)) }
        }
    }

    @Nested
    inner class BitwiseShift {
        private val pos16 = rng.nextInt(0..Short.MAX_VALUE)
        private val neg16 = rng.nextInt(Short.MIN_VALUE..-1)
        private val shift16 = rng.nextInt(0..<16)
        private val gte128 = rng.nextInt(128..160)

        @Test
        fun shl() {
            println(neg16.toString(2))
            println((neg16 shl shift16).toString(2))
            println((Int128(neg16 shl shift16)).twosComplement())
            assertEquals2c(Int128(pos16 shl shift16), Int128(pos16) shl shift16)
            assertEquals2c(Int128(neg16 shl shift16), Int128(neg16) shl shift16)
            assertEquals2c(Int128.ZERO, Int128(rng) shl gte128)
            assertThrows<IllegalArgumentException> { Int128.ONE shl -1 }
        }

        @Test
        fun shr() {
            assertEquals2c(Int128(pos16 shr shift16), Int128(pos16) shr shift16)
            assertEquals2c(Int128(neg16 shr shift16), Int128(neg16) shr shift16)
            assertEquals2c(Int128(-1), Int128(rng) shr gte128)
            assertThrows<IllegalArgumentException> { Int128.ONE shr -1 }
        }

        @Test
        fun ushr() {
            assertEquals2c(Int128(pos16 ushr shift16), Int128(pos16) ushr shift16)
            assertEquals2c(Int128(-1 ushr shift16, -1, -1, neg16 shr shift16), Int128(neg16) ushr shift16)
            assertEquals2c(Int128.ZERO, Int128(rng) ushr gte128)
            assertThrows<IllegalArgumentException> { Int128.ONE ushr -1 }
        }
    }

    @Nested
    inner class Arithmetic {
        private val int64 = rng.nextLong()

        @Test
        fun unaryMinus() {
            assertEquals2c(Int128(-int64), -Int128(int64))
            assertEquals(-int64, -Int128(int64).toLong())
            assertThrows<ArithmeticException> { -Int128.MIN_VALUE }
        }

        @Test
        fun plus() {
            val i128a = i128()
            val i128b = i128()
            val sum = i128a + i128b
            assertEquals2c(i128a, sum - i128b)
            assertEquals2c(i128b, sum - i128a)
            assertThrows<ArithmeticException> { Int128.MAX_VALUE + Int128.ONE }
        }

        @Test
        fun minus() {
            val i128a = i128()
            val i128b = i128()
            val difference = i128a - i128b
            assertEquals2c(i128a, difference + i128b)
            assertEquals2c(i128b, i128a - difference)
            assertThrows<ArithmeticException> { Int128.MIN_VALUE - Int128.ONE }
        }

        @Test
        fun times() {
            val i128a = i128()
            val i128b = i128()
            val product = i128a * i128b
            assertEquals2c(i128a, product / i128b)
            assertEquals2c(i128b, product / i128a)
            assertDoesNotThrow { Int128.MAX_VALUE * Int128.ONE }
            assertThrows<ArithmeticException> { Int128.MAX_VALUE * Int128.TWO }
        }

        @Test
        fun divide() {
            // TODO make tests for every possible path in divide()
            val i128a = Int128(5000)
            val i128b = Int128(1250)
            val quotient = i128a / i128b
            assertEquals2c(i128a, quotient * i128b)
            assertEquals2c(i128b, i128a / quotient)
            assertDoesNotThrow { Int128.ONE / Int128.MAX_VALUE }
            assertThrows<ArithmeticException> { Int128.ONE / Int128.ZERO }
        }
    }
}