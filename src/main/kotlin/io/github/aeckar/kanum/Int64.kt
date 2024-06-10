package io.github.aeckar.kanum

import java.math.BigInteger

/**
 * Destructuring of a value into the closest scaled 64-bit integer to this and its scale.
 *
 * Some information may be lost during conversion.
 */
internal class Int64 {
    private val value: Long
    private val scale: Int

    constructor(i128: Int128) {
        val value = MutableInt128(i128)/* = */.abs()
        var scale = 0
        while (value > Int.MAX_VALUE.toLong()) {
            value / /* = */ Int128.TEN
            ++scale
        }
        this.value = value.toLong() * i128.sign
        this.scale = scale
    }

    constructor(value: Long, scale: Int) {
        this.value = value
        this.scale = scale
    }

    /**
     * The closest value n, for x=n*10^scale, where n <= [Int.MAX_VALUE].
     */
    operator fun component1() = value

    /**
     * A non-zero, base-10 scalar.
     */
    operator fun component2() = scale
}