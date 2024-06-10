package io.github.aeckar.kanum

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class Int128Instantiation {
    @Test
    fun from_string() {
        val posStr = Random(0).nextInt().toString()
        val negStr = (-Random(1).nextInt()).toString()
        val max32Str = Int.MAX_VALUE.toString()
        val min32Str = Int.MIN_VALUE.toString()
        val max64Str = Long.MAX_VALUE.toString()
        val min64Str = Long.MIN_VALUE.toString()

        val pos = Int128(posStr)
        val neg = Int128(negStr)
        val max32 = Int128(max32Str)
        val min32 = Int128(min32Str)
        val max64 = Int128(max64Str)
        val min64 = Int128(min64Str)

        assertEquals(posStr, pos.toString())
        assertEquals(negStr, neg.toString())
        assertEquals(max32Str, max32.toString())
        assertEquals(min32Str, min32.toString())
        assertEquals(max64Str, max64.toString())
        assertEquals(min64Str, min64.toString())
    }

    @Test
    fun from_big_integer() {
        TODO()
    }
}