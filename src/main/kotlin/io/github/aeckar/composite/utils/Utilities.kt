package io.github.aeckar.composite.utils

import io.github.aeckar.composite.Int128
import io.github.aeckar.composite.Rational

// ------------------------------ arithmetic ------------------------------

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
internal fun productSign(x: Int, y: Int) = if ((x < 0) == (y < 0)) 1 else -1

/**
 * Returns true if the sum is the result of a signed integer overflow.
 *
 * If a result of multiple additions must be checked, this function must be called for each intermediate sum.
 * Also checks for the case [Int.MIN_VALUE] - 1.
 */
internal fun addValueOverflows(x: Int, y: Int, sum: Int): Boolean {
    if (x == Int.MIN_VALUE && y == -1 || y == Int.MIN_VALUE && x == -1) {
        return true
    }
    val isNegative = x < 0
    return isNegative == (y < 0) && isNegative xor (sum < 0)
}

// ------------------------------ exception handling ------------------------------

/**
 * @throws ArithmeticException always
 */
internal fun raiseUndefined(message: String): Nothing = throw ArithmeticException(message)

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * For specific operations, an explicit result name may be preferable.
 *
 * @throws ArithmeticException always
 */
internal fun Any.raiseOverflow(
    additionalInfo: String? = null,
    cause: Throwable? = null
): Nothing {
    val info = additionalInfo?.let { " ($it)" } ?: ""
    throw ArithmeticException("${receiver()} overflows$info").initCause(cause)
}

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * For specific operations, an explicit result name may be preferable.
 *
 * @throws ArithmeticException always
 */
internal fun Any.raiseIncorrectFormat(
    reason: String,
    cause: Throwable? = null
): Nothing {
    val e = NumberFormatException("String does not contain a ${receiver().lowercase()} in the correct format ($reason)")
    throw e.initCause(cause)
}

private fun Any.receiver() = when (this) {
    is Rational, is Rational.Companion -> "Rational number"
    is Int128, is Int128.Companion -> "128-bit integer"
    else -> throw IllegalArgumentException("Receiver is not a CompositeNumber or companion of one")
}