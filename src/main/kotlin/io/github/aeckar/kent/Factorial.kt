package io.github.aeckar.kent

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import kotlinx.atomicfu.update

/**
    Contains previously calculated factorials.

    Array members are immutable.
    Has a size of 37, as 37! is the largest factorial that can fit inside a 128-bit integer.
 */
private val cache = atomicArrayOfNulls<Int128>(37).apply { this[0].getAndSet(Int128.ONE) }

private var cachePos = atomic(0)

/**
 * Returns the factorial of [x] as a 128-bit integer.
 *
 * @throws ArithmeticException x is non-negative or the resultant value overflows
 */
fun factorial(x: Int): Int128 {
    var largestCached = cachePos.value  // Ensure state does not change halfway through
    if (largestCached >= x) {
        return try {
            cache[x].value!!
        } catch (_: IndexOutOfBoundsException) {
            raiseUndefined("Factorial of $x does not exist")
        }
    }
    var result = cache[largestCached].value!!
    repeat (x - largestCached) {
        try {
            result *= (cache.size + 1).toInt128()
        } catch (e: ArithmeticException) {  // x > 42
            Int128.raiseOverflow("$x!", e)
        }
        ++largestCached
        cache[largestCached].getAndSet(result)
    }
    cachePos.update { if (it >= largestCached) it else largestCached }
    return result
}