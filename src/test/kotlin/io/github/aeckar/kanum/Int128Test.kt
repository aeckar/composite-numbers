package io.github.aeckar.kanum

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.assertEquals

class Int128Test {
    private val posStr = Random(0).nextInt().toString()
    private val negStr = (-Random(1).nextInt()).toString()
    private val max32Str = Int.MAX_VALUE.toString()
    private val min32Str = Int.MIN_VALUE.toString()
    private val max64Str = Long.MAX_VALUE.toString()
    private val min64Str = Long.MIN_VALUE.toString()

    private val pos = Int128(posStr)
    private val neg = Int128(negStr)
    private val max32 = Int128(max32Str)
    private val min32 = Int128(min32Str)
    private val max64 = Int128(max64Str)
    private val min64 = Int128(min64Str)

    // ------------------------------ instantiation ------------------------------

    @Test
    fun instantiate_from_string() {
        assertEquals(posStr, pos.toString())
        assertEquals(negStr, neg.toString())
        assertEquals(max32Str, max32.toString())
        assertEquals(min32Str, min32.toString())
        assertEquals(max64Str, max64.toString())
        assertEquals(min64Str, min64.toString())

        assertThrows<NumberFormatException> { Int128("3.14") }
        assertThrows<NumberFormatException> { Int128("") }
        assertThrows<ArithmeticException> { Int128("1461501637330902918203684832716283019655932542976") }
    }

    @Test
    fun instantiate_from_big_integer() {
        TODO()
    }

    // ------------------------------ bitwise operations ------------------------------

    private val pos32 = 365
    private val neg32 = -128908
    private val shiftAmount = 5

    @Test
    fun shl() {
        assertEquals(Int128(pos32 shl shiftAmount), Int128(pos32) shl shiftAmount)
        assertEquals(Int128(neg32 shl shiftAmount), Int128(neg32) shl shiftAmount)
    }
    
    @Test
    fun shr() {
        assertEquals(Int128(pos32 shr shiftAmount), Int128(pos32) shr shiftAmount)
        assertEquals(Int128(neg32 shr shiftAmount), Int128(neg32) shr shiftAmount)
    }
    
    @Test
    fun ushr() {
        assertEquals(Int128(pos32 ushr shiftAmount), Int128(pos32) ushr shiftAmount)
        assertEquals(Int128(-1 ushr shiftAmount, -1, -1, neg32 shr shiftAmount), Int128(neg32) ushr shiftAmount)
    }

    // ------------------------------ arithmetic ------------------------------

    @Test
    fun unaryMinus() {
        assertThrows<> {  }
        assertThrows<ArithmeticException> { -Int128.MIN_VALUE }
    }

    // TODO
}