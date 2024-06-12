package io.github.aeckar.composite

import io.github.aeckar.composite.utils.addValueOverflows
import io.github.aeckar.composite.utils.raiseUndefined
import io.github.aeckar.composite.utils.raiseOverflow
import kotlin.math.absoluteValue

// ---------------------------------------- arithmetic ----------------------------------------

private infix fun Long.times128(other: Long): Int128 {
    fun multiplyValueOverflows(x: Long, y: Long, product: Long = x * y): Boolean {
        if (x == Long.MIN_VALUE && y == -1L || y == Long.MIN_VALUE && x == -1L) {
            return true
        }
        return x != 0L && y != 0L && x != product / y
    }

    val product = this * other
    if (multiplyValueOverflows(this, other, product)) {
        return MutableInt128(this) * Int128(other)
    }
    return Int128(product)
}

private fun tenPow(power: Int): Long {
    var result = 1L
    repeat(power) { result *= 10 }
    return result
}

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
private fun productSign(x: Long, y: Long) = if ((x < 0L) == (y < 0L)) 1 else -1

/**
 * Computes the base-10 logarithm of [x], floored if not a whole number.
 *
 * Formally, the returned values are, in order:
 * - the integer base-10 logarithm of x
 * - x - 10^result
 * @return the result paired to the remainder
 */
private fun log10(x: Long): Int {
    if (x <= 0) {
        raiseUndefined("Log10 of $x does not exist")
    }
    var arg = x
    var result = 0
    while (arg >= 10L) {
        arg /= 10L
        ++result
    }
    return result
}

// ---------------------------------------- class definitions ----------------------------------------

/**
 * A mutable rational number.
 *
 * See [Cumulative] for details on composite number mutability.
 */
class MutableRational {
    constructor(rational: Rational) {

    }
}
// TODO implement AFTER testing of immutable class

/**
 * Returns [x] as a ratio.
 *
 * Some information may be lost after conversion.
 */
fun Rational(x: Int128): Rational {
    val (numer, scale) = ScaledInt64(x)
    return Rational(numer, 1, scale, x.sign)
}

/**
 * A ratio of two 32-bit integers designed for minimal information loss.
 *
 * Instances of this class are scalable by a base-10, 32-bit integer.
 * Conversion from an arbitrary-precision number is currently unsupported.
 */
@Suppress("EqualsOrHashCode")
open class Rational : CompositeNumber<Rational> {
    /**
     * The numerator of this value as a fraction.
     */
    val numer: Long

    /**
     * The denominator of this value as a fraction.
     */
    val denom: Long

    /**
     *
     */
    val scale: Int

    final override val sign: Int
    override fun immutable(): Rational {
        TODO("Not yet implemented")
    }

    override fun mutable(): Rational {
        TODO("Not yet implemented")
    }

    override fun valueOf(other: Rational): Rational {
        TODO("Not yet implemented")
    }

    /**
     * Returns a ratio with the given [numerator][numer] and [denominator][denom] after simplification.
     * @throws ArithmeticException [denom] is 0 or the value is too large to be representable
     */
    constructor(numer: Long, denom: Long = 1L, scaleAugment: Int = 0) {
        fun gcf(x: Long, y: Long): Long {
            tailrec fun euclideanGCF(max: Long, min: Long): Long {
                val rem = max % min
                return if (rem == 0L) min else euclideanGCF(min, rem)
            }

            val max = maxOf(x, y)
            val min = minOf(x, y)
            return euclideanGCF(max, min)
        }

        if (denom == 0L) {
            raiseUndefined("Denominator cannot be zero")
        }
        if (numer == 0L) {  // Logarithm of 0 is undefined
            this.numer = 0
            this.denom = 0
            this.scale = 0
            this.sign = 0
            return
        }
        val numerValue = numer.absoluteValue
        val denomValue = denom.absoluteValue
        val numerScale = log10(numerValue)
        val denomScale = log10(denomValue)
        val unscaledNumer = numerValue - tenPow(numerScale)
        val unscaledDenom = denomValue - tenPow(denomScale)
        val gcf = gcf(unscaledNumer, unscaledDenom)
        this.numer = unscaledNumer / gcf
        this.denom = unscaledDenom / gcf
        val rawScale = numerScale - denomScale
        this.scale = rawScale + scaleAugment
        if (addValueOverflows(rawScale, scaleAugment, scale)) {
            raiseOverflow()
        }
        this.sign = productSign(numer, denom)
    }

    /**
     * Returns a ratio with the given [numerator][numer] and [denominator][denom] after simplification.
     * @throws ArithmeticException [denom] is 0 or the value is too large to be representable
     */
    constructor(numer: Int, denom: Int = 1, scaleAugment: Int = 0) : this(numer.toLong(), denom.toLong(), scaleAugment)

    /**
     * TODO
     * @throws IllegalArgumentException [s] is in an incorrect format
     */
    constructor(s: String) {
        fun illegalFormat(s: String, e: Throwable? = null): Throwable {
            return IllegalArgumentException("Illegal format: $s").initCause(e)
        }

        val chars = s.iterator()
        this.numer = 0
        this.denom = 0
        this.scale = 0
        this.sign = 0
        // TODO
    }

    internal constructor(numer: Long, denom: Long, scale: Int, sign: Int) {
        this.numer = numer
        this.denom = denom
        this.scale = scale
        this.sign = sign
    }

    // Used by arithmetic operations working with large numbers
    private constructor(numer: Int128, denom: Int128, scaleAugment: Int, sign: Int) {
        fun gcf(x: Int128, y: Int128): Int128 {
            tailrec fun euclideanGCF(max: Int128, min: Int128): Int128 {
                val rem = max % min
                return if (rem == Int128.ZERO) min else euclideanGCF(min, rem)
            }

            val max = maxOf(x, y)
            val min = if (max === x) y else x
            return euclideanGCF(max, min)
        }

        val gcf = gcf(numer, denom)
        val (unscaledNumer, numerScale) = ScaledInt64(numer / gcf)
        val (unscaledDenom, denomScale) = ScaledInt64(denom / gcf)
        this.numer = unscaledNumer
        this.denom = unscaledDenom
        this.scale = (numerScale - denomScale) + scaleAugment
        if (scale < scaleAugment) { // Sum overflows
            raiseOverflow()    // TODO when valueOf is implemented, catch this and specify result
        }
        this.sign = sign
    }

    // ---------------------------------------- arithmetic ----------------------------------------

    /**
     * Returns a new instance equal to this when the numerator and denominator are swapped.
     */
    fun reciprocal() = Rational(denom, numer, -scale, sign)

    override fun signum() = if (numer == 0L) 0 else sign

    override fun unaryMinus() = Rational(numer, denom, scale, -sign)

    // a/b + c/d = (ad + bc)/bd
    override fun plus(other: Rational): Rational {
        fun leftAddend() = numer times128 other.denom   // = ad
        fun rightAddend() = other.numer times128 denom  // = bc

        val sign: Int
        val numer = if (this.sign == other.sign) {
            sign = if (this.sign < 0) -1 else 1
            leftAddend() + rightAddend()
        } else {
            // (positive addend) - (abs. value of negative addend)
            val isNegative = this.sign < 0
            val minuend = if (isNegative) rightAddend() else leftAddend()
            val subtrahend = if (isNegative) leftAddend() else rightAddend()
            (minuend - subtrahend).also { sign = if (it.isNegative) -1 else 1 }.abs()
        }
        val denom = denom times128 other.denom  // = bd
        return Rational(numer.abs(), denom.abs(), scale, sign)
    }

    // a/b * c/d = ac/cd
    override fun times(other: Rational): Rational {
        val numer = numer times128 other.numer
        val denom = denom times128 other.denom
        return Rational(numer, denom, scale, productSign(sign.toLong(), other.sign.toLong()))
    }

    // a/b / c/d = a/b * d/c
    override fun div(other: Rational) = this * other.reciprocal()

    // (a/b)^k = a^k/b^k
    override fun pow(power: Int): Rational {
        if (power == 1) {
            return this
        }
        if (power == 0) {
            return ONE
        }
        var numer = numer
        var denom = denom
        repeat(power) {     // Since both are positive/zero, sign is not an issue
            val lastNumer = numer
            val lastDenom = denom
            numer *= numer
            denom *= denom
            if (numer < lastNumer || denom < lastDenom) {   // Product overflows, perform widening
                return Rational(
                    MutableInt128(lastNumer).pow(power),
                    MutableInt128(lastDenom).pow(power),
                    scale,
                    sign
                )
            }
        }
        return Rational(numer * sign, denom, scale)
    }

    // ---------------------------------------- elementary functions ----------------------------------------

    // TODO implement elementary functions

    // ---------------------------------------- comparison ----------------------------------------

    override fun compareTo(other: Rational): Int {
        return if (sign != other.sign) sign.compareTo(other.sign) else toBigDecimal().compareTo(other.toBigDecimal())
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + numer.hashCode()
        hash = 31 * hash + denom.hashCode()
        hash = 31 * hash + scale
        return hash * sign
    }

    override /* internal */ fun stateEquals(other: Rational): Boolean {
        return numer == other.numer && denom == other.denom && scale == other.scale && sign == other.sign
    }

    override /* protected */ fun isLong() = denom == 1L && scale >= 0 && log10(numer) + scale > 18

    // ---------------------------------------- conversion functions ----------------------------------------

    override fun toInt() = toLong().toInt()

    override fun toLong() = (numer / denom) * tenPow(scale) * sign

    override fun toDouble() = (numer.toDouble() / denom) * tenPow(scale) * sign

    /**
     * Returns this instance.
     */
    override fun toRational() = this

    override fun toInt128(): Int128 {
        return if (scale < 0) {
            -(Int128(numer) / (denom times128 tenPow(-scale)))
        } else {
            (numer times128 tenPow(scale)) / Int128(denom)
        }
    }

    override fun toString(): String {
        val sign = if (sign != -1) "" else "-"
        val denom = if (denom == 1L) "" else "/$denom"
        val scale = if (scale == 0) "" else " * 10^$scale"
        return "$sign$numer$denom$scale"
    }

    companion object {
        val NEGATIVE_ONE = Rational(1, 1, 0, -1)
        val ZERO = Rational(0, 1, 0, 0)
        val ONE = Rational(1, 1, 0, 0)
        val TWO = Rational(2, 1, 0, 0)
        val TEN = Rational(10, 1, 0, 0)
    }
}
/*
module kanum {
    requires kotlin.stdlib;

    exports io.github.aeckar.kanum;
}
 */