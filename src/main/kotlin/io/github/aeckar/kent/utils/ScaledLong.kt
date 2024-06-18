package io.github.aeckar.kent.utils

import io.github.aeckar.kent.Int128
import io.github.aeckar.kent.raiseIncorrectFormat

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
        if (i128.isLong()) {
            this.value = i128.toLong()
            this.scale = scaleAugment
            return
        }
        val sign = i128.sign.toShort()
        var value = i128.abs()  // May mutate `i128`
        var scale = scaleAugment
        // TODO if convertible to long, use that instead
        while (value > Int.MAX_VALUE.toLong()) {
            value /= Int128.TEN // Division is not cumulative
            ++scale
        }
        this.value = value.toLong() * sign
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
        fun at(iterator: StringIndexIterator, sentinels: String): ScaledLong {
            fun fromInteger(start: Int, curIndex: StringIndexIterator): ScaledLong {
                val string = curIndex.string
                val endExclusive = curIndex.position
                val positiveAugment = if (endExclusive <= 38) 0 else endExclusive % 38
                return ScaledLong(Int128.parse(string, 10, start, endExclusive - positiveAugment), positiveAugment)
            }

            var curIndex = iterator
            val start = curIndex.position
            while (curIndex.char { it != '.' && it != '/' }) {
                ++curIndex
            }
            if (curIndex.doesNotExist() || curIndex.char() == '/') {
                return fromInteger(start, curIndex)
            }
            val positionAtDot = curIndex.position
            do {
                ++curIndex
            } while (curIndex.char { it in '0'..'9' })
            if (curIndex.char { it !in sentinels }) {
                raiseIncorrectFormat("illegal embedded character")
            }
            do {
                --curIndex
            } while (curIndex.char() == '0')
            val negativeAugment = positionAtDot - curIndex.position
            if (negativeAugment == 0) {    // Only zeros after dot
                return fromInteger(start, curIndex)
            }
            return ScaledLong(Int128.parse(curIndex.string, 10, start, curIndex.position + 1, true), negativeAugment)
        }
    }
}