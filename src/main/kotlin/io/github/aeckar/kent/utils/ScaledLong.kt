package io.github.aeckar.kent.utils

import io.github.aeckar.kent.Int128

/**
 * Destructuring of a value into the closest scaled 64-bit integer to this and its scale.
 */
internal class ScaledLong {
    private val value: Long
    private val scale: Int

    /**
     * Does not preserve the state of mutable values.
     *
     * Some information may be lost during conversion.
     */
    constructor(i128: Int128, scaleAugment: Int = 0) {
        val sign = i128.sign.toShort()
        var abs = i128.abs()
        var scale = scaleAugment
        while (abs > Int.MAX_VALUE.toLong()) {
            abs /= Int128.TEN // Division is not cumulative
            ++scale
        }
        this.value = abs.toLong() * sign
        this.scale = scale * sign
    }

    // Accessed by BigInteger pseudo-constructor only
    constructor(value: Long, scale: Int) {
        this.value = value
        this.scale = scale
    }

    /**
     * The closest value n, for x=n*10^scale, where n <= [Int.MAX_VALUE].
     */
    operator fun component1() = value

    /**
     * See [Rational.scale][io.github.aeckar.kent.Rational.scale] for details.
     */
    operator fun component2() = scale

    companion object {
        /**
         * Assumes that the character at the current position in [iterator] exists,
         * and that all trailing zeros have been skipped over.
         */
        // Accessed by string constructor of Rational only
        fun at(iterator: StringIndexIterator): ScaledLong {
            fun fromInteger(start: Int, curIndex: StringIndexIterator): ScaledLong {
                val string = curIndex.string
                val position = curIndex.position
                val positiveAugment = position % 38
                return ScaledLong(Int128.parse(string, 10, start, position - positiveAugment), positiveAugment)
            }

            var curIndex = iterator
            val start = curIndex.position
            var digitCount = 0
            // Largest representable 128-bit integer is 39 decimal digits long
            while (curIndex.char { it != '.' && it != '/' }) {
                ++digitCount
                ++curIndex
            }
            if (curIndex.doesNotExist() || curIndex.char() == '/') {
                return fromInteger(start, curIndex)
            }
            val positionAtDot = curIndex.position
            do {
                ++curIndex
            } while (curIndex.char { it in '0'..'9' })
            do {
                --curIndex
            } while (curIndex.char() == '0')
            val negativeAugment = positionAtDot - curIndex.position
            if (negativeAugment == 0) {    // Only zeros after dot
                return fromInteger(start, curIndex)
            }
            val string = curIndex.string
            return ScaledLong(Int128.parse(string, 10, start, curIndex.position + 1, true), negativeAugment)
        }
    }
}