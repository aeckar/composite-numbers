package io.github.aeckar.composite

import io.github.aeckar.composite.constants.HUGE_STRING
import io.github.aeckar.composite.constants.random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigInteger
import kotlin.test.assertEquals

/*
    Right now, the work necessary to publish this library to Maven Central is not worth adding multiplatform support.
    However, if that ever does happen, this file will be moved to "/jvmTest".
 */

class ArbitraryPrecisionTest {
    private val posInt = random.nextInt().toBigInteger()
    private val negInt = (-random.nextInt()).toBigInteger()
    private val max32Int = Int.MAX_VALUE.toBigInteger()
    private val min32Int = Int.MIN_VALUE.toBigInteger()
    private val max64Int = Long.MAX_VALUE.toBigInteger()
    private val min64Int = Long.MIN_VALUE.toBigInteger()
    private val oneInt = BigInteger.ONE
    private val zeroInt = BigInteger.ZERO
    private val neg1Int = (-1).toBigInteger()

    @Nested
    inner class Int128 {
        @Test
        fun big_integer_conversion() {
            assertEquals(posInt, Int128(posInt).toBigInteger())
            assertEquals(negInt, Int128(negInt).toBigInteger())
            assertEquals(max32Int, Int128(max32Int).toBigInteger())
            assertEquals(min32Int, Int128(min32Int).toBigInteger())
            assertEquals(max64Int, Int128(max64Int).toBigInteger())
            assertEquals(min64Int, Int128(min64Int).toBigInteger())
            assertEquals(oneInt, Int128(oneInt).toBigInteger())
            assertEquals(zeroInt, Int128(zeroInt).toBigInteger())
            assertEquals(neg1Int, Int128(neg1Int).toBigInteger())

            assertThrows<ArithmeticException> { Int128(BigInteger(HUGE_STRING)) }
        }
    }

    @Nested
    inner class Rational {
        @Test
        fun big_integer_conversion() {

        }
    }
}