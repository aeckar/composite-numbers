package io.github.aeckar.kent.utils

@Suppress("NOTHING_TO_INLINE")
internal inline infix fun Short.plus(other: Short): Short = (this + other).toShort()

@Suppress("NOTHING_TO_INLINE")
internal inline infix fun Short.minus(other: Short): Short = (this - other).toShort()

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
internal fun productSign(x: Int, y: Int) = if ((x < 0) == (y < 0)) 1 else -1