package io.github.aeckar.kent.utils

/**
 * C-style boolean-to-integer conversion. 1 if true, 0 if false.
 */
internal fun Boolean.toInt() = if (this) 1 else 0

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
internal fun productSign(x: Int, y: Int) = if ((x < 0) == (y < 0)) 1 else -1