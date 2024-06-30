package io.github.aeckar.kent.utils

internal const val LONG_MAX_STRING = "9223372036854775807"

/**
 * Passed to a constructor of a class to distinguish it from a pseudo-constructor with the same arguments.
 */
internal object PrivateAPIFlag

/**
 * C-style boolean-to-integer conversion. 1 if true, 0 if false.
 */
internal fun Boolean.toInt() = if (this) 1 else 0

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
internal fun productSign(x: Int, y: Int) = if ((x < 0) == (y < 0)) 1 else -1

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
internal fun productSign(x: Long, y: Long) = if ((x < 0) == (y < 0)) 1 else -1