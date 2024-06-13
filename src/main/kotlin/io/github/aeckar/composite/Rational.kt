package io.github.aeckar.composite

import io.github.aeckar.composite.utils.productSign
import io.github.aeckar.composite.utils.raiseUndefined
import io.github.aeckar.composite.utils.raiseOverflow
import kotlin.math.absoluteValue

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
    override fun valueOf(numer: Long, denom: Long, scale: Short, sign: Int) = this.also {
        it.numer = numer;   it.scale = scale
        it.denom = denom;   it.sign = sign
    }
}

/**
 * Returns a rational number equal to this value over the other as a fraction after simplification.
 * @throws ArithmeticException [other] is 0 or the value is too large to be representable
 */
infix fun Int.over(other: Int) = Rational.ONE.valueOf(this.toLong(), other.toLong(), 0)

/**
 * Returns a rational number equal to this value over the other as a fraction after simplification.
 * @throws ArithmeticException [other] is 0 or the value is too large to be representable
 */
infix fun Long.over(other: Long) = Rational.ONE.valueOf(this, other, 0)

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
fun Rational(numer: Long, denom: Long = 1, scaleAugment: Int = 0): Rational {
    return Rational.ONE.valueOf(numer, denom, scaleAugment.toShort())
}

/**
 * A rational number.
 *
 * Instances of this class are comprised of:
 * - A 64-bit integer numerator
 * - A 64-bit integer denominator
 * - A 32-bit scalar, n, by which this value is multiplied by 10^n
 * - A sign value, 1 or -1, by which this value is multiplied by
 *
 * Designed for minimal information loss,
 *
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
    var scale: Short
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

    internal constructor(numer: Long, denom: Long, scale: Short, sign: Int) {
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
    internal open fun valueOf(numer: Long, denom: Long, scale: Short, sign: Int): Rational {
        return Rational(numer, denom, scale, sign)
    }

    /**
     * Value function with delegation to by-property constructor.
     *
     * Returns a ratio with the given [numerator][numer] and [denominator][denom] after simplification.
     * @throws ArithmeticException [denom] is 0 or the value is too large to be representable
     */
    internal fun valueOf(numer: Long, denom: Long, scaleAugment: Short): Rational {
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
        var scale = (numerScale - denomScale).toShort()
        if (addValueOverflows(numerScale, denomScale, scale)) {
            raiseOverflow()
        }
        val augmentedScale = (scale + scaleAugment).toShort()
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
        scaleAugment: Short,
        sign: Int,
        additionalInfo: () -> String
    ): Rational {
        val gcf = gcf(numer, denom)
        val (unscaledNumer, numerScale) = ScaledInt64(numer / gcf)
        val (unscaledDenom, denomScale) = ScaledInt64(denom / gcf)
        var scale = (numerScale - denomScale).toShort()
        try {
            if (addValueOverflows(numerScale, denomScale, scale)) {
                raiseOverflow()
            }
            val augmentedScale = (scale + scaleAugment).toShort()
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
    fun reciprocal() = valueOf(denom, numer, (-scale).toShort(), sign)

    final override fun signum() = if (numer == 0L) 0 else sign

    @Cumulative
    final override fun unaryMinus() = valueOf(numer, denom, scale, -sign)

    // a/b + c/d = (ad + bc)/bd
    @Cumulative
    final override fun plus(other: Rational): Rational {
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
    final override fun times(other: Rational): Rational {
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
        val scale = (scale + other.scale).toShort()
        if (addValueOverflows(scale, other.scale, scale)) {
            raiseOverflow("$this * $other")
        }
        return valueOf(numer, denom, scale, productSign(sign, other.sign)) { "$this * $other" }
    }

    // a/b / c/d = a/b * d/c
    final override fun div(other: Rational) = this * other.reciprocal()

    // (a/b)^k = a^k/b^k
    @Cumulative
    final override fun pow(power: Int): Rational {
        if (power == Int.MIN_VALUE) {
            raiseOverflow("$this ^ Int.MIN_VALUE")
        }
        return if (power < 0) raiseTo(-power)/* = */.reciprocal() else raiseTo(power)
    }

    @Cumulative
    private fun raiseTo(power: Int): Rational {
        if (power == 1) {
            return this
        }
        if (power == 0) {
            return valueOf(ONE)
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
            valueOf(numer * sign, denom, scale)
        } catch (e: ArithmeticException) {
            raiseOverflow("$this ^ $power", e)
        }
    }

    // ---------------------------------------- elementary functions ----------------------------------------

    // TODO implement elementary functions using taylor series expansion

    // ---------------------------------------- comparison ----------------------------------------

    final override fun compareTo(other: Rational): Int {
        return if (sign != other.sign) sign.compareTo(other.sign) else toBigDecimal().compareTo(other.toBigDecimal())
    }

    final override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + numer.hashCode()
        hash = 31 * hash + denom.hashCode()
        hash = 31 * hash + scale
        return hash * sign
    }

    final override /* internal */ fun stateEquals(other: Rational): Boolean {
        return numer == other.numer && denom == other.denom && scale == other.scale && sign == other.sign
    }

    final override /* protected */ fun isLong() = denom == 1L && scale >= 0 && log10(numer) + scale > 18

    // ---------------------------------------- conversion functions ----------------------------------------

    final override fun toInt() = toLong().toInt()

    final override fun toLong() = (numer / denom) * tenPowExact(scale) * sign

    final override fun toDouble() = (numer.toDouble() / denom) * tenPowExact(scale) * sign

    /**
     * Returns this instance.
     */
    final override fun toRational() = this

    /**
     * Returns a 128-bit integer equal in value to this.
     *
     * Some information may be lost during conversion.
     */
    final override fun toInt128(): Int128 {
        return if (scale < 0) {
            Int128.ZERO
        } else {
            ((MutableInt128(numer) * Int128.TEN.pow(scale.toInt())) / Int128(denom)).immutable()
        }
    }

    final override fun toString(): String {
        lazyString?.let { return it }
        val sign = if (sign != -1) "" else "-"
        val denom = if (denom == 1L) "" else "/$denom"
        val scale = if (scale == 0.toShort()) "" else " * 10^$scale"
        return "$sign$numer$denom$scale".also { lazyString = it }
    }

    companion object {
        val NEGATIVE_ONE = Rational(1, 1, 0, -1)
        val ZERO = Rational(0, 1, 0, 0)
        val ONE = Rational(1, 1, 0, 0)
        val TWO = Rational(2, 1, 0, 0)
        val TEN = Rational(10, 1, 0, 0)

        // ------------------------------ helper functions ------------------------------

        /**
         * If the exponentiation causes signed integer overflow, throws [ArithmeticException].
         */
        private fun tenPowExact(scale: Short): Long {
            if (scale > 62) {   // log10(10^n) >= log10(2^63 - 1)
                Long.raiseOverflow()
            }
            return tenPow(scale)
        }

        private fun tenPow(scale: Short): Long {
            var result = 1L
            repeat(scale.toInt()) { result *= 10 }
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
        private fun log10(x: Long): Short {
            if (x <= 0) {
                raiseUndefined("Log10 of $x does not exist")
            }
            var arg = x
            var result: Short = 0
            while (arg >= 10L) {
                arg /= 10L
                ++result
            }
            return result
        }

        private fun gcf(x: Int128, y: Int128): Int128 {
            tailrec fun euclideanGCF(max: Int128, min: Int128): Int128 {
                val rem = max % min
                return if (rem == Int128.ZERO) min else euclideanGCF(min, rem)
            }

            val max = maxOf(x, y)
            val min = if (max === x) y else x
            return euclideanGCF(max, min)
        }

        /**
         * Returns true if the sum is the result of a signed integer overflow.
         *
         * If a result of multiple additions must be checked, this function must be called for each intermediate sum.
         * Also checks for the case [Short.MIN_VALUE] - 1.
         */
        private fun addValueOverflows(x: Short, y: Short, sum: Short): Boolean {
            val neg1 = (-1).toShort()
            if (x == Short.MIN_VALUE && y == neg1 || y == Short.MIN_VALUE && x == neg1) {
                return true
            }
            val isNegative = x < 0
            return isNegative == (y < 0) && isNegative xor (sum < 0)
        }
    }
}