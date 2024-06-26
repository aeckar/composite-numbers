package io.github.aeckar.kent

/**
 * Returns an instance equal to this value with its decimal part [truncated][Rational.toWhole].
 *
 * If negative, 1 is subtracted from the resultant value.
 */
fun floor(x: Rational): Rational {
    val whole = x.toWhole()
    return if (x < Rational.ZERO) whole - Rational.ONE else whole
}

/**
 * Returns an instance equal to this value with its decimal part [truncated][Rational.toWhole].
 *
 * If positive, 1 is added to the resultant value.
 */
fun ceil(x: Rational): Rational {
    val whole = x.toWhole()
    return if (x < Rational.ZERO) whole + Rational.ONE else whole
}