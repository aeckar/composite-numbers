package io.github.aeckar.kanum.utils

import io.github.aeckar.kanum.Int128
import io.github.aeckar.kanum.Rational

// ------------------------------ arithmetic ------------------------------

internal fun Int.widen() = this.toUInt().toLong()

/**
 * Returns true if the sum is the result of a signed integer overflow.
 */
internal fun addValueOverflows(x: Int, y: Int, sum: Int = x + y): Boolean {
    val isNegative = x < 0
    return isNegative == (y < 0) && isNegative != (sum  < 0)
}

/**
 * Returns true if the sum is the result of an unsigned integer overflow.
 */
internal fun addIntOverflows(x: Int, y: Int, sum: Int = x + y) = (x.widen() + y.widen()).high != 0

/**
 * The most significant 32 bits of this value.
 */
internal val Long.high inline get() = (this ushr 32).toInt()

/**
 * The least significant 32 bits of this value.
 */
internal val Long.low inline get() = this.toInt()

/**
 * The [upper half][high] of this value.
 */
internal operator fun Long.component1() = high

/**
 * The [lower half][low] of this value.
 */
internal operator fun Long.component2() = low

// ------------------------------ exception handling ------------------------------

internal fun raiseUndefined(message: String): Nothing = throw ArithmeticException(message)

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * For specific operations, an explicit result name may be preferable.
 */
internal fun Any.raiseOverflow(result: String = name(), cause: Throwable? = null): Nothing {
    throw ArithmeticException("$result is too large to be representable").initCause(cause)
}

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * For specific operations, an explicit result name may be preferable.
 */
internal fun Any.raiseIncorrectFormat(reason: String, result: String = name(), cause: Throwable? = null): Nothing {
    val e = NumberFormatException("String does not contain a ${result.lowercase()} in the correct format ($reason)")
    throw e.initCause(cause)
}

private fun Any.name() = when (this) {
    is Rational, is Rational.Companion -> "Rational number"
    is Int128, is Int128.Companion -> "128-bit integer"
    else -> "Composite number"  // Normally should never be reached
}