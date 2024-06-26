package io.github.aeckar.kent

import io.github.aeckar.kent.Rational.Companion.TWO_PI

// TODO test all

/**
 * Returns -1 to the power of [n].
 *
 * Describes an alternating series.
 */
private fun neg1Pow(n: Int): Int128 = if (n and 1 == 0) Int128.ONE else Int128.NEGATIVE_ONE

/**
 * Returns an approximation of an elementary operation using the MacLaurin series (Taylor series at a = 0).
 *
 * Example: sin(x) =
 *
 *    (-1)^n * x^(2n+1)   -> +1 means pow0=1
 *    ----------------- = x - x^3/3! + x^5/5! ...
 *         (2n+1)!
 *
 * where pow0 = 1 and powStep = 2
 *
 * @param termNumer numerator of the coefficient
 * @param termDenom denominator of the coefficient
 * @param powConstant the integer added to `kn` in the exponent
 * @param powCoefficient the integer, `k`, multiplied by `n` in the exponent
 */
@Cumulative
private /* noinline */ fun seriesApprox(    // FIXME
    x: Rational,
    termNumer: (n: Int) -> Int128,
    termDenom: (n: Int) -> Int128,
    powConstant: Int,
    powCoefficient: Int
): Rational {
    /*
         Approximation of Elementary Functions using the MacLaurin Series  

         Definitions:
            powCoefficient E {1, 2}
            powConstant E {0, 1}
            
            numerBase = x.numer^powCoefficient
            numerFactor = x.numer^powConstant                             
            denomBase = x.denom^powCoefficient
            denomFactor = x.denom^powConstant
            
         Proof:
             termNumer()     x.numer^(powCoefficient(n) + powConstant)
            ------------- * ------------------------------------------- =>
             termDenom()     x.denom^(powCoefficient(n) + powConstant)
         
             numerBase^n  * numerFactor * termNumer() 
            ------------------------------------------
             denomBase^n  * denomFactor * termDenom()
     */

    val numerBase = Int128(x.numer).pow(powCoefficient)
    val denomBase = Int128(x.denom).pow(powCoefficient)
    val numerFactor: Int128
    val denomFactor: Int128
    if (powConstant == 0) {
        numerFactor = Int128.ONE
        denomFactor = Int128.ONE
    } else {
        numerFactor = Int128(x.numer).pow(powConstant)
        denomFactor = Int128(x.denom).pow(powConstant)
    }

    // n = 0
    var numer = numerFactor * termNumer(0)
    var denom = denomFactor * termDenom(0)
    var result = Rational(numer, denom)
    var lastResult: Rational

    var n = 1   // Since 38! overflows a 128-bit integer, may not exceed 37.
    do try {
        lastResult = result
        numer = numerBase.mutable().pow(n) */* = */ numerFactor */* = */ termNumer(n)
        denom = denomBase.mutable().pow(n) */* = */ denomFactor */* = */ termDenom(n)
        result = Rational(numer, denom) // Reduces 128-bit integers to scaled Longs whose values determine convergence
        ++n
    } catch (_: ArithmeticException) {  // Multiplication overflows
        break
    } while (!result.stateEquals(lastResult))
    return result
}

// ------------------------------ natural logarithm ------------------------------

// TODO may not be accurate for all ranges. Test to ensure correctness

/**
 * Returns an instance approximately equal to the natural logarithm of [x].
 */
@Cumulative
fun ln(x: Rational) = seriesApprox(x - Rational.ONE,
    termNumer = { neg1Pow(it) },
    termDenom = { it.toInt128() },
    powConstant = 0,
    powCoefficient = 1
)

// ------------------------------ trigonometry ------------------------------

/**
 * Returns an instance approximately equal to the sine of [x].
 */
@Cumulative
fun sin(x: Rational) = seriesApprox(x % TWO_PI,
    termNumer = { neg1Pow(it) },
    termDenom = { factorial(2 * it + 1) },
    powConstant = 1,
    powCoefficient = 2
)

/**
 * Returns an instance approximately equal to the cosine of [x].
 */
@Cumulative
fun cos(x: Rational) = seriesApprox(x % TWO_PI,
    termNumer = { neg1Pow(it) },
    termDenom = { factorial(2 * it) },
    powConstant = 0,
    powCoefficient = 2
)

/**
 * Returns an instance approximately equal to the tangent of [x].
 */
@Cumulative
fun tan(x: Rational) = sin(x) / cos(x)

// ------------------------------ hyperbolic trigonometry ------------------------------

/**
 * Returns an instance approximately equal to the hyperbolic sine of [x].
 */
@Cumulative
fun sinh(x: Rational) = seriesApprox(x % TWO_PI,
    termNumer = { Int128.ONE },
    termDenom = { factorial(2 * it + 1) },
    powConstant = 1,
    powCoefficient = 2
)

/**
 * Returns an instance approximately equal to the hyperbolic cosine of [x].
 */
@Cumulative
fun cosh(x: Rational) = seriesApprox(x % TWO_PI,
    termNumer = { Int128.ONE },
    termDenom = { Int128.TWO * factorial(it) },
    powConstant = 0,
    powCoefficient = 2
)

/**
 * Returns an instance approximately equal to the hyperbolic tangent of [x].
 */
@Cumulative
fun tanh(x: Rational) = sinh(x) / cosh(x)

// ------------------------------ inverse trigonometry ------------------------------

/**
 * Returns an instance approximately equal to the inverse sine of [x].
 */
@Cumulative
fun arcsin(x: Rational) = seriesApprox(x,
    termNumer = { factorial(2*it) },
    termDenom = {
        val square = Int128.TWO.pow(it) * factorial(it)
        square * square * Int128(2 * it + 1)
    },
    powConstant = 1,
    powCoefficient = 2
)

/**
 * Returns an instance approximately equal to the inverse cosine of [x].
 */
// pi - x does not matter, just use the result of the subtraction
// Special cases and shortcuts covered by elem func symbols in simplify()
@Cumulative
fun arccos(x: Rational) = Rational.HALF_PI - arcsin(x)

/**
 * Returns an instance approximately equal to the inverse tangent of [x].
 */
@Cumulative
fun arctan(x: Rational) = seriesApprox(x,
    termNumer = { neg1Pow(it) },
    termDenom = { Int128(2 * it + 1) },
    powConstant = 1,
    powCoefficient = 2
)