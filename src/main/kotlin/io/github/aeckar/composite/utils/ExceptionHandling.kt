package io.github.aeckar.composite.utils

import io.github.aeckar.composite.Int128
import io.github.aeckar.composite.Rational

private fun Any.receiver() = when (this) {
    is Rational, is Rational.Companion -> "Rational number"
    is Int128, is Int128.Companion -> "128-bit integer"
    else -> "Value"
}

/**
 * @throws ArithmeticException always
 */
internal fun raiseUndefined(message: String): Nothing = throw ArithmeticException(message)

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
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
 * @throws ArithmeticException always
 */
internal fun Any.raiseIncorrectFormat(
    reason: String,
    cause: Throwable? = null
): Nothing {
    val e = NumberFormatException("String does not contain a ${receiver().lowercase()} in the correct format ($reason)")
    throw e.initCause(cause)
}