package io.github.aeckar.kanum

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Int128Bitwise {
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
}