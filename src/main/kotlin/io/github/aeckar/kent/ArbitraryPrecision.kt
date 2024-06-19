package io.github.aeckar.kent

import io.github.aeckar.kent.utils.ScaledLong
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer

/*
    When multiplatform support is added, this file will be moved to "/jvmMain".

    This library is primarily useful for performing calculations with a low margin of error.
    Therefore, it would not make sense to code in constructors with a Double argument
    for the composite numbers, despite it being possible.
 */

private val LONG_MAX = Long.MAX_VALUE.toBigInteger()

/**
 * Returns a scaled, 64-bit integer equal to the absolute value of [value].
 */
private fun ScaledLong(value: BigInteger): ScaledLong {
    var int = value.abs()
    var scale = 0
    while (int > LONG_MAX) {
        // Realistically, we could count up to a certain number of bits, but this way is more accurate
        int /= BigInteger.TEN
        ++scale
    }
    return ScaledLong(int.longValueExact(), scale)
}

// ------------------------------ 128-bit integer functions ------------------------------

/**
 * Compares this value to the other.
 *
 * See [compareTo][Comparable.compareTo] for details.
 */
operator fun Int128.compareTo(value: BigDecimal) = toBigDecimal().compareTo(value)

/**
 * Compares this value to the other.
 *
 * See [compareTo][Comparable.compareTo] for details.
 */
operator fun Int128.compareTo(value: BigInteger) = toBigInteger().compareTo(value)

/**
 * Returns an arbitrary-precision decimal equal in value to this.
 */
fun Int128.toBigDecimal() = toBigInteger().toBigDecimal()

/**
 * Returns an arbitrary-precision integer equal in value to this.
 */
fun Int128.toBigInteger(): BigInteger {
    val value = ByteBuffer.allocate(Int128.SIZE_BYTES).apply { putInt(q1); putInt(q2); putInt(q3); putInt(q4) }.array()
    return BigInteger(value)
}

/**
 * Returns a 128-bit integer equal to the given arbitrary-precision number.
 *
 * Any decimal digits are truncated during conversion.
 * @throws ArithmeticException [value] is too large to be represented as an Int128
 */
@Suppress("unused")
fun Int128(value: BigDecimal) = Int128(value.toBigInteger())

/**
 * Returns a 128-bit integer equal to the given arbitrary-precision integer.
 *
 * @throws ArithmeticException [value] is too large to be represented as an Int128
 */
fun Int128(value: BigInteger): Int128 {
    var bytes = value.toByteArray()
    val maxBytes = Int128.SIZE_BYTES
    if (bytes.size > maxBytes) {
        Int128.raiseOverflow(value.toString())
    }
    if (bytes.size != maxBytes) {
        val padding = maxBytes - bytes.size
        bytes = bytes.copyInto(ByteArray(maxBytes), padding)
        val blank = Int128.blank(value.signum() or 1 /* if zero */)
        repeat(padding) { bytes[it] = blank.toByte() }
    }
    val parts = IntArray(4).apply(ByteBuffer.wrap(bytes).asIntBuffer()::get)
    return Int128(parts[0], parts[1], parts[2], parts[3])
}

// ------------------------------ rational number functions ------------------------------

/**
 * Compares this value to the other.
 *
 * See [compareTo][Comparable.compareTo] for details.
 */
operator fun Rational.compareTo(value: BigDecimal) = toBigDecimal().compareTo(value)

/**
 * Compares this value to the other.
 *
 * See [compareTo][Comparable.compareTo] for details.
 */
operator fun Rational.compareTo(value: BigInteger) = toBigInteger().compareTo(value)

/**
 * Returns an arbitrary-precision decimal equal in value to this.
 *
 * Information may be lost during conversion.
 */
fun Rational.toBigDecimal() = numer.toBigDecimal().setScale(-scale) / denom.toBigDecimal() * sign.toBigDecimal()

/**
 * Returns an arbitrary-precision integer equal in value to this.
 *
 * Information may be lost during conversion.
 */
fun Rational.toBigInteger() = numer.toBigInteger() * BigInteger.TEN.pow(scale) * sign.toBigInteger()

/**
 * Returns a rational number equal to the given arbitrary-precision number.
 *
 * Some information may be lost on conversion.
 */
fun Rational(value: BigDecimal): Rational {
    val whole = value.toBigInteger()
    val (unscaledWhole, wholeScale) = ScaledLong(whole)
    val rawFracScale: Int
    val frac = (value - whole.toBigDecimal())
        .also { rawFracScale = it.scale() /* < 0 */ }
        .setScale(rawFracScale - rawFracScale.coerceAtLeast(-19 /* = -log10(Long.MAX_SIZE) */))
    val (unscaledFrac, fracScale) = ScaledLong(frac.toBigInteger())
    return Rational(unscaledWhole, 1L, wholeScale, 1) + Rational(unscaledFrac, 1L, -fracScale, value.signum() or 1)
}

/**
 * Returns a rational number equal to the given arbitrary-precision integer.
 *
 * Some information may be lost on conversion.
 */
fun Rational(value: BigInteger): Rational {
    val (numer, scale) = ScaledLong(value)
    return Rational(numer, 1L, scale, value.signum() or 1)
}