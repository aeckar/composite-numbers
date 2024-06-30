@file:JvmName("Functions")
@file:JvmMultifileClass
package io.github.aeckar.kent.functions

import io.github.aeckar.kent.Int128
import io.github.aeckar.kent.raiseOverflow
import io.github.aeckar.kent.raiseUndefined

private val int128Factorials = arrayOf(
    Int128.ONE,
    Int128.ONE,
    Int128.TWO,
    Int128(0, 0, 0, 6),
    Int128(0, 0, 0, 24),
    Int128(0, 0, 0, 120),
    Int128(0, 0, 0, 720),
    Int128(0, 0, 0, 5040),
    Int128(0, 0, 0, 40320),
    Int128(0, 0, 0, 362880),
    Int128(0, 0, 0, 3628800),
    Int128(0, 0, 0, 39916800),
    Int128(0, 0, 0, 479001600),
    Int128(0, 0, 1, 1932053504),
    Int128(0, 0, 20, 1278945280),
    Int128(0, 0, 304, 2004310016),
    Int128(0, 0, 4871, 2004189184),
    Int128(0, 0, 82814, -288522240),
    Int128(0, 0, 1490668, -898433024),
    Int128(0, 0, 28322707, 109641728),
    Int128(0, 0, 566454140, -2102132736),
    Int128(0, 2, -989364938, -1195114496),
    Int128(0, 60, -291192141, -522715136),
    Int128(0, 1401, 1892515369, 862453760),
    Int128(0, 33634, -1824271396, -775946240),
    Int128(0, 840864, 1637855376, 2076180480),
    Int128(0, 21862473, -365433172, -1853882368),
    Int128(0, 590286795, -1276761037, 1484783616),
    Int128(3, -651838905, -1389570659, -1375731712),
    Int128(111, -1723459042, -1642843428, -1241513984),
    Int128(3347, -164163690, -2040662563, 1409286144),
    Int128(103786, -794107078, 1163969997, 738197504),
    Int128(3321178, 358377288, -1407665755, -2147483648),
    Int128(109598876, -1058451362, 791670357, -2147483648),
)

/**
 * Returns the factorial of [x] as a 128-bit integer.
 *
 * @throws ArithmeticException x is non-negative or the resultant value overflows
 */
public fun factorial(x: Int): Int128 {
    if (x < 0) {
        raiseUndefined("Factorial of $x is undefined")
    }
    return try {
        exhaustiveFactorial(x)
    } catch (e: FactorialOverflowSignal) {
        Int128.raiseOverflow("$x!", e)
    }
}

/**
 * Assumes [x] is non-negative.
 */
internal fun exhaustiveFactorial(x: Int): Int128 {
    if (x > int128Factorials.lastIndex) {
        throw FactorialOverflowSignal
    }
    return int128Factorials[x]
}

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
internal object FactorialOverflowSignal : Throwable()