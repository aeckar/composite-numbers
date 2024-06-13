package io.github.aeckar.composite

import io.github.aeckar.composite.Rational.Companion.ZERO
import io.github.aeckar.composite.utils.addValueOverflows
import io.github.aeckar.composite.utils.productSign
import io.github.aeckar.composite.utils.raiseUndefined
import io.github.aeckar.composite.utils.raiseOverflow
import kotlin.math.absoluteValue

// ---------------------------------------- arithmetic ----------------------------------------

/**
 * Multiplies the two 64-bit integers and stores the result accurately within a 128-bit integer.
 */
private infix fun Long.times(other: Long): MutableInt128 {
    fun multiplyValueOverflows(x: Long, y: Long, product: Long = x * y): Boolean {
        if (x == Long.MIN_VALUE && y == -1L || y == Long.MIN_VALUE && x == -1L) {
            return true
        }
        return x != 0L && y != 0L && x != product / y
    }
    val product = this * other
    if (multiplyValueOverflows(this, other, product)) {
        return (MutableInt128(this) */* = */ Int128(other)) as MutableInt128
    }
    return MutableInt128(product)
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
internal class MutableRational(unique: Rational) : Rational(unique.numer, unique.denom, unique.scale, unique.sign) {
    override fun immutable() = Rational(numer, denom, scale, sign)

    @Cumulative
    override fun mutable() = this

    @Cumulative
    override fun valueOf(numer: Long, denom: Long, scale: Int, sign: Int) = this.also {
        it.numer = numer;   it.scale = scale
        it.denom = denom;   it.sign = sign
    }
}

/**
 * Returns a ratio equal to this value over the other as a fraction after simplification.
 * @throws ArithmeticException [other] is 0 or the value is too large to be representable
 */
infix fun Int.over(other: Int) = Rational(this.toLong(), other.toLong(), 0, productSign(this, other))

/**
 * Returns a ratio equal to this value over the other as a fraction after simplification.
 * @throws ArithmeticException [other] is 0 or the value is too large to be representable
 */
infix fun Long.over(other: Long) = Rational(this, other, 0, productSign(this, other))

/**
 * Returns a rational number equal in value to [x].
 *
 * Some information may be lost after conversion.
 */
fun Rational(x: Int128): Rational {
    val (numer, scale) = ScaledInt64(x)
    return Rational(numer, 1, scale, x.sign)
}

/**
 * Returns a ratio with the given [numerator][numer] and [denominator][denom] after simplification.
 * @throws ArithmeticException [denom] is 0 or the value is too large to be representable
 */
fun Rational(numer: Long, denom: Long = 1, scaleAugment: Int = 0) = Rational(numer, denom, scaleAugment, 0)

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
    var numer: Long
        protected set

    /**
     * The denominator of this value as a fraction.
     */
    var denom: Long
        protected set

    /**
     * A non-zero scalar, n, by which this value is multiplied to 10^n.
     * 
     * Validation should be used to ensure this value never holds the value
     * of [Int.MIN_VALUE] to prevent incorrect operation results.
     */
    var scale: Int
        protected set

    final override var sign: Int
        protected set

    /**
     * Returns a rational number equal in value to the given string.
     *
     * // TODO
     * Fractional components with leading 0 digits are allowed.
     *
     * The given string must be small enough to be representable and
     * not contain any extraneous characters (for example, whitespace).
     * It may optionally be prefixed by a negative sign.
     *
     * @throws NumberFormatException [s] is in an incorrect format
     * @throws ArithmeticException the value cannot be represented accurately as a rational number
     */
    constructor(s: String) {
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

    // ---------------------------------------- mutability ----------------------------------------

    override fun immutable() = this

    override fun mutable(): Rational = MutableRational(this)

    final override fun valueOf(other: Rational) = with (other) { valueOf(numer, denom, scale, sign) }

    /**
     * Value function with delegation to by-property constructor.
     */
    internal open fun valueOf(numer: Long, denom: Long, scale: Int, sign: Int): Rational {
        return Rational(numer, denom, scale, sign)
    }

    /**
     * Value function with delegation to by-property constructor.
     *
     * Returns a ratio with the given [numerator][numer] and [denominator][denom] after simplification.
     * @throws ArithmeticException [denom] is 0 or the value is too large to be representable
     */
    internal fun valueOf(numer: Long, denom: Long, scaleAugment: Int): Rational {
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
            return valueOf(0, 0, 0, 0)
        }
        val numerAbs = numer.absoluteValue
        val denomAbs = denom.absoluteValue
        val numerScale = log10(numerAbs)
        val denomScale = log10(denomAbs)
        var scale = numerScale - denomScale
        if (addValueOverflows(numerScale, denomScale, scale)) {
            raiseOverflow()
        }
        val augmentedScale = scale + scaleAugment
        if (addValueOverflows(scale, scaleAugment, augmentedScale)) {
            raiseOverflow()
        }
        scale = augmentedScale
        val unscaledNumer = numerAbs - tenPow(numerScale)
        val unscaledDenom = denomAbs - tenPow(denomScale)
        val gcf = gcf(unscaledNumer, unscaledDenom)
        return valueOf(unscaledNumer / gcf, unscaledDenom / gcf, scale, productSign(numer, denom))
    }

    /**
     * Value function with delegation to by-property constructor.
     * @throws ArithmeticException [denom] is 0 or the value is too large to be representable
     */
    // Accessed only by raiseTo(), plus(), and times()
    private inline fun valueOf(
        numer: Int128,
        denom: Int128,
        scaleAugment: Int,
        sign: Int,
        additionalInfo: () -> String
    ): Rational {
        val gcf = gcf(numer, denom)
        val (unscaledNumer, numerScale) = ScaledInt64(numer / gcf)
        val (unscaledDenom, denomScale) = ScaledInt64(denom / gcf)
        var scale = numerScale - denomScale
        try {
            if (addValueOverflows(numerScale, denomScale, scale)) {
                raiseOverflow()
            }
            val augmentedScale = scale + scaleAugment
            if (addValueOverflows(scale, scaleAugment, augmentedScale)) {
                raiseOverflow()
            }
            scale = augmentedScale
             return valueOf(unscaledNumer, unscaledDenom, scale, sign)
        } catch (e: ArithmeticException) {
            raiseOverflow(additionalInfo())
        }
    }

    // ---------------------------------------- arithmetic ----------------------------------------

    /**
     * Returns a new instance equal to this when the numerator and denominator are swapped.
     */
    @Cumulative
    fun reciprocal() = valueOf(denom, numer, -scale, sign)

    override fun signum() = if (numer == 0L) 0 else sign

    @Cumulative
    override fun unaryMinus() = valueOf(numer, denom, scale, -sign)

    // a/b + c/d = (ad + bc)/bd
    @Cumulative
    override fun plus(other: Rational): Rational {
        val ad = numer times other.denom
        val bc = other.numer times denom
        val sign: Int
        val numer = if (this.sign == other.sign) {
            sign = if (this.sign < 0) -1 else 1
            ad +/* = */ bc
        } else {    // (positive addend) - (abs. value of negative addend)
            val isNegative = this.sign < 0
            val minuend = if (isNegative) bc else ad
            val subtrahend = if (isNegative) ad else bc
            (minuend -/* = */ subtrahend).also { sign = if (it.isNegative) -1 else 1 }/* = */.abs()
        }
        val bd = denom times other.denom
        return valueOf(numer/* = */.abs(), bd/* = */.abs(), scale, sign) { "$this + $other" }
    }

    // a/b * c/d = ac/cd
    override fun times(other: Rational): Rational {
        if (other.stateEquals(ONE)) {
            return this
        }
        if (this.stateEquals(ONE)) {
            return other
        }
        if (other.stateEquals(ZERO) || this.stateEquals(ZERO)) {
            return ZERO
        }
        val numer = numer times other.numer
        val denom = denom times other.denom
        val scale = scale + other.scale
        if (addValueOverflows(scale, other.scale, scale)) {
            raiseOverflow("$this * $other")
        }
        return valueOf(numer, denom, scale, productSign(sign, other.sign)) { "$this * $other" }
    }

    // a/b / c/d = a/b * d/c
    override fun div(other: Rational) = this * other.reciprocal()

    // (a/b)^k = a^k/b^k
    override fun pow(power: Int): Rational {
        if (power == Int.MIN_VALUE) {
            raiseOverflow("$this ^ Int.MIN_VALUE")
        }
        val value = if (power < 0) {
            raiseTo(-power)/* = */.reciprocal()
        } else {
            raiseTo(power)
        }
        return value
    }

    private fun raiseTo(power: Int): Rational {
        if (power == 1) {
            return this
        }
        if (power == 0) {
            return ONE
        }
        val pow = power.absoluteValue
        var numer = numer
        var denom = denom
        repeat(pow) {     // Since both fractional components are positive or zero, sign is not an issue
            val lastNumer = numer
            val lastDenom = denom
            numer *= numer
            denom *= denom
            if (numer < lastNumer || denom < lastDenom) {   // Product overflows, perform widening
                return valueOf(
                    MutableInt128(lastNumer)/* = */.pow(pow),
                    MutableInt128(lastDenom)/* = */.pow(pow),
                    scale,
                    sign
                ) { "$this ^ $power" }
            }
        }
        return try {
            Rational(numer * sign, denom, scale)
        } catch (e: ArithmeticException) {
            raiseOverflow("$this ^ $power")
        }
    }

    // ---------------------------------------- elementary functions ----------------------------------------

    // TODO implement elementary functions using taylor series expansion

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
            -(Int128(numer) / (denom times tenPow(-scale)))
        } else {
            (numer times tenPow(scale)) / Int128(denom)
        }
    }

    override fun toString(): String {
        lazyString?.let { return it }
        val sign = if (sign != -1) "" else "-"
        val denom = if (denom == 1L) "" else "/$denom"
        val scale = if (scale == 0) "" else " * 10^$scale"
        return "$sign$numer$denom$scale".also { lazyString = it }
    }

    companion object {
        val NEGATIVE_ONE = Rational(1, 1, 0, -1)
        val ZERO = Rational(0, 1, 0, 0)
        val ONE = Rational(1, 1, 0, 0)
        val TWO = Rational(2, 1, 0, 0)
        val TEN = Rational(10, 1, 0, 0)

        // ------------------------------ arithmetic ------------------------------
        private fun gcf(x: Int128, y: Int128): Int128 {
            tailrec fun euclideanGCF(max: Int128, min: Int128): Int128 {
                val rem = max % min
                return if (rem == Int128.ZERO) min else euclideanGCF(min, rem)
            }

            val max = maxOf(x, y)
            val min = if (max === x) y else x
            return euclideanGCF(max, min)
        }
    }
}
/*
module kanum {
    requires kotlin.stdlib;

    exports io.github.aeckar.kanum;
}
 */