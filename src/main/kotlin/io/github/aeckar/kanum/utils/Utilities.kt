package io.github.aeckar.kanum.utils

// ------------------------------ arithmetic ------------------------------

/**
 * Returns true if the sum is the result of an integer overflow.
 */
fun addOverflows(x: Int, y: Int, sum: Int = x + y): Boolean {
    val isNegative = x < 0
    return isNegative == (y < 0) && isNegative != (sum  < 0)
}

// ------------------------------ exception handling ------------------------------

fun raiseUndefined(message: String): Nothing = throw ArithmeticException(message)

fun raiseOverflow(result: String, cause: Throwable? = null): Nothing {
    throw ArithmeticException("$result is too large to be representable").initCause(cause)
}

fun raiseIncorrectFormat(result: String, cause: Throwable): Nothing {
    throw NumberFormatException("String does not contain a $result in the correct format").initCause(cause)
}