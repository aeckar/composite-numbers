package io.github.aeckar.kent

private fun Any.receiver() = when (this) {
    is Rational, is Rational.Companion -> "Rational number"
    is Int128, is Int128.Companion -> "128-bit integer"
    else -> "Value"
}

/**
 * @throws ArithmeticException always
 */
internal fun raiseUndefined(message: String): Nothing = throw CompositeArithmeticException(message)

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * @throws ArithmeticException always
 */
internal fun Any.raiseOverflow(
    additionalInfo: String? = null,
    cause: Throwable? = null
): Nothing {
    throw CompositeArithmeticException("${receiver()} overflows${additionalInfo?.let { " ($it)" }.orEmpty()}", cause)
}

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * @throws ArithmeticException always
 */
internal fun Any.raiseIncorrectFormat(
    reason: String,
    cause: Throwable? = null
): Nothing {
    throw CompositeFormatException(
            "String does not contain a ${receiver().lowercase()} in the correct format ($reason)", cause)
}

/**
 * Thrown when an operation involving [composite numbers][CompositeNumber] cannot proceed due to overflow or an undefined result.
 */
public class CompositeArithmeticException internal constructor(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown from a `String`-arg pseudo-constructor for a [composite number][CompositeNumber] class,
 * indicating that the given string is malformed.
 */
public class CompositeFormatException internal constructor(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)