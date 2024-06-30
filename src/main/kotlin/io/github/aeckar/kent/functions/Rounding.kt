@file:JvmName("Functions")
@file:JvmMultifileClass
package io.github.aeckar.kent.functions

import io.github.aeckar.kent.Rational

/**
 * Returns the whole number closest to this value, rounding towards negative infinity.
 */
public fun floor(x: Rational): Rational {
    val whole = x.toWhole()
    return if (x < Rational.ZERO) whole - Rational.ONE else whole
}

/**
 * Returns the whole number closest to this value, rounding towards positive infinity.
 */
public fun ceil(x: Rational): Rational {
    val whole = x.toWhole()
    return if (x < Rational.ZERO) whole + Rational.ONE else whole
}