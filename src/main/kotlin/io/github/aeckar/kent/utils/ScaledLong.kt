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
        if (i128.compareTo(Long.MIN_VALUE) == 0) {
            this.value = 9223372036854775800L
            this.scale = 0
            return
        }
        if (i128.isLong()) {
            this.value = i128.toLong()
            this.scale = scaleAugment
            return
        }
        val sign = i128.sign
        var value = /* (maybe) i128 = */ i128.abs()
        var scale = scaleAugment
        while (value > Long.MAX_VALUE) {
            /* (maybe) value = */ value /= Int128.TEN
            ++scale
        }
        this.value = value.toLong() * sign
        this.scale = scale * sign
    }

    /**
     * Intended for access by `BigInteger` pseudo-constructor only.
     */
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

    override fun toString() = if (scale < 0) "$value * 10^($scale)" else "$value * 10^$scale"

    companion object {
        private val ZERO = ScaledLong(0, 0)

        /**
         * Assumes that [view] is within bounds and all trailing `0`s have been skipped over.
         *
         * Intended for access by `String`-arg constructor of Rational only.
         */
        fun parse(view: StringView, stop: String): ScaledLong = with(view) {
            val start = index()
            var stopIndex: Int

            // Whole part
            while (satisfies { it != '.' && it !in stop }) {
                if (char() !in '0'..'9') {
                    raiseIncorrectFormat("illegal embedded character")
                }
                move(1)
            }

            // Fractional part
            var scaleAugment = 0
            if (satisfies { it == '.' }) {
                val dotIndex = index()
                do {
                    move(1)
                    if (isWithinBounds()) {
                        if (char() !in '0'..'9') {
                            raiseIncorrectFormat("illegal embedded character")
                        }
                        if (char() != '0') {
                            scaleAugment = dotIndex - index()
                        }
                    }
                } while (satisfies { it !in stop })
                stopIndex = index()
                if (scaleAugment == 0) {
                    move(dotIndex - index())
                    do {
                        move(-1)
                    } while(satisfies { it == '0' })
                    scaleAugment += dotIndex - index() + 1
                }
                move(stopIndex - index())
            }
            stopIndex = index()
            do {
                move(-1)
            } while(satisfies { it == '0' || it == '.' })
            if (isNotWithinBounds()) {
                return ZERO
            }
            // FIXME NOW doesn't work for values that overflow Int128
            val value = ScaledLong(Int128.parse(string, 10, start, index() + 1, ignoreDot = true), scaleAugment)
            move(stopIndex - index())
            value
        }
    }
}