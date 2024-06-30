@file:JvmName("Conversions")
@file:JvmMultifileClass
package io.github.aeckar.kent

import io.github.aeckar.kent.functions.floor
import io.github.aeckar.kent.utils.*
import io.github.aeckar.kent.utils.productSign
import kotlin.math.absoluteValue

private const val LONG_MIN_UNSCALED = 922337203685477580L

/**
 * Returns a rational number equal to this value over the other as a fraction after simplification.
 * @throws CompositeArithmeticException [other] is 0 or the value is too large or small to be represented accurately
 */
@JvmName("toRational")
public infix fun Int.over(other: Int): Rational = Rational(this.toLong(), other.toLong(), 0)

/**
 * Returns a rational number equal to this value over the other as a fraction after simplification.
 *
 * If this value is [Long.MIN_VALUE], the value stored will be equal to −2^63 + 8.
 * @throws CompositeArithmeticException [other] is 0 or the value is too large or small to be represented accurately
 */
@JvmName("toRational")
@Suppress("unused")
public infix fun Long.over(other: Long): Rational = Rational(this, other, 0)

/**
 * Returns a rational number equal to this value.
 *
 * For numbers that are known to be smaller than -1 or larger than 16,
 * the `Long`-args constructor should be used instead.
 */
public fun Int.toRational(): Rational = when (this) {
    -1 -> Rational.NEGATIVE_ONE
    0 -> Rational.ZERO
    1 -> Rational.ONE
    2 -> Rational.TWO
    else -> this over 1
}

/**
 * See [toRational] for details.
 */
public fun Long.toRational(): Rational = when (this) {
    -1L -> Rational.NEGATIVE_ONE
    0L -> Rational.ZERO
    1L -> Rational.ONE
    2L -> Rational.TWO
    else -> this over 1
}

internal fun MutableRational(numer: Int128, denom: Int128): MutableRational {
    val sign = productSign(numer.sign, denom.sign)  // May be mutated by abs()
    return Rational.ONE.mutable().valueOf(numer.abs(), denom.abs(), 0, sign) { "Instantiation" } as MutableRational
}

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

    override fun toString() = stringValue() // Cannot be cached
}

/**
 * Returns a rational number equal in value to [x].
 *
 * Some information may be lost after conversion.
 */
@JvmName("toRational")
public fun Rational(x: Int128): Rational {
    val (numer, scale) = ScaledLong(x)
    return Rational(numer, 1, scale, x.sign)
}

/**
 * Returns a rational number with the given [numerator][numer] and [denominator][denom] after simplification.
 * @throws CompositeArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
 */
@JvmName("toRational")
public fun Rational(numer: Long, denom: Long = 1, scaleAugment: Int = 0): Rational {
    fun gcf(x: Long, y: Long): Long {
        tailrec fun euclideanGCF(max: Long, min: Long): Long {
            val rem = max % min
            return if (rem == 0L) min else euclideanGCF(min, rem)
        }

        val max = maxOf(x, y)
        val min = if (max == x) y else x
        if (min == 0L) {
            return max
        }
        return euclideanGCF(max, min)
    }

    var scale = scaleAugment
    val numerAbs = if (numer == Long.MIN_VALUE) {
        ++scale
        LONG_MIN_UNSCALED
    } else {
        numer.absoluteValue
    }
    val denomAbs = if (denom == Long.MIN_VALUE) {
        --scale
        LONG_MIN_UNSCALED
    } else {
        denom.absoluteValue
    }
    val gcf = gcf(numerAbs, denomAbs)
    val simplifiedNumer = numerAbs / gcf
    val simplifiedDenom = denomAbs / gcf
    return Rational(simplifiedNumer, simplifiedDenom, scale, productSign(numer, denom))
}

/**
 * Returns a rational number with the [numerator][numer] and [denominator][denom] after simplification.
 *
 * Some information may be lost during conversion.
 * @throws CompositeArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
 */
@JvmName("toRational")
public fun Rational(numer: Int128, denom: Int128, scaleAugment: Int = 0): Rational {
    val sign = productSign(numer.sign, denom.sign)  // May be mutated by abs()
    return Rational.ONE.valueOf(numer.abs(), denom.abs(), scaleAugment, sign) { "Instantiation" }
}

@JvmName("toRational")
public fun Rational(s: String): Rational = Rational.parse(s)

/**
 * A rational number.
 *
 * Instances of this class are comprised of the following:
 * - 64-bit integer numerator
 * - 64-bit integer denominator
 * - 32-bit scalar, n, by which this value is multiplied by 10^n
 * - sign value, 1 or -1, by which this value is multiplied by
 *
 * Avoids the performance impact of arbitrary-precision arithmetic, while
 * allowing all instances to be readily converted to their fractional form.
 *
 * All 64-bit integer values, aside from [Long.MIN_VALUE], can be stored without losing information.
 * Furthermore, all values are guaranteed to be accurate to at least 19 digits
 * before considering error accumulated through calls to multiple operations.
 */
@Suppress("EqualsOrHashCode")
public open class Rational internal constructor(
    numer: Long,
    denom: Long,
    scale: Int,
    sign: Int
) : CompositeNumber<Rational>() {
    /**
     * The numerator when this value is represented as a fraction.
     */
    public var numer: Long = numer
        protected set

    /**
     * The denominator when this value is represented as a fraction.
     */
    public var denom: Long = denom
        protected set

    /**
     * A non-zero scalar, n, by which this value is multiplied to 10^n.
     * 
     * Validation should be used to ensure this value never holds the value
     * of [Int.MIN_VALUE] to prevent incorrect operation results.
     */
    public var scale: Int = scale
        protected set

    final override var sign: Int = sign
        protected set

    final override val isNegative: Boolean get() = sign == -1
    final override val isPositive: Boolean get() = sign == 1

    // ---------------------------------------- mutability ----------------------------------------

    override fun immutable() = this

    override fun mutable() = MutableRational(this)

    final override fun uniqueMutable() = MutableRational(this)

    final override fun valueOf(other: Rational) = with (other) { this@Rational.valueOf(numer, denom, scale, sign) }

    /**
     * Value function with delegation to by-property constructor.
     */
    internal open fun valueOf(numer: Long, denom: Long, scale: Int, sign: Int): Rational {
        return Rational(numer, denom, scale, sign)
    }

    /**
     * Value function with delegation to by-property constructor.
     * @throws CompositeArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
     */
    internal inline fun valueOf(
        numer: Int128,
        denom: Int128,
        scaleAugment: Int,
        sign: Int,
        crossinline additionalInfo: () -> String
    ): Rational {
        if (denom.stateEquals(Int128.ZERO)) {
            raiseUndefined("Denominator cannot be zero (numer = $numer)")
        }
        val gcf = gcf(numer, denom)
        val (unscaledNumer, numerScale) = ScaledLong(numer / gcf)   // Consume `numer`
        val (unscaledDenom, denomScale) = ScaledLong(denom / gcf)   // Consume `denom`
        try {
            if (addOverflowsValue(numerScale, denomScale)) {
                raiseOverflow()
            }
            var scale = numerScale - denomScale
            if (addOverflowsValue(scale, scaleAugment)) {
                raiseOverflow()
            }
            scale += scaleAugment
            return valueOf(unscaledNumer, unscaledDenom, scale, sign)
        } catch (e: CompositeArithmeticException) {
            raiseOverflow(additionalInfo())
        }
    }

    /**
     * Value function with delegation to by-property constructor.
     *
     * Returns a ratio with the given [numerator][numer] and [denominator][denom] after simplification.
     * @throws CompositeArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
     */
    private fun valueOf(numer: Long, denom: Long, scaleAugment: Int): Rational {
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
        val numerScale = scale(numerAbs)
        val denomScale = scale(denomAbs)
        if (addOverflowsValue(numerScale, denomScale)) {
            raiseOverflow()
        }
        var scale = numerScale - denomScale
        if (addOverflowsValue(scale, scaleAugment)) {
            raiseOverflow()
        }
        scale += scaleAugment
        val unscaledNumer = (numerAbs / tenPow(numerScale))
        val unscaledDenom = (denomAbs / tenPow(denomScale))
        val gcf = gcf(unscaledNumer, unscaledDenom)
        return valueOf(unscaledNumer / gcf, unscaledDenom / gcf, scale, productSign(numer, denom))
    }

    // ---------------------------------------- arithmetic ----------------------------------------

    /**
     * Returns an instance equal to this when the numerator and denominator are swapped.
     */
    @Cumulative
    public fun reciprocal(): Rational = valueOf(denom, numer, -scale, sign)

    final override fun signum(): Int = if (numer == 0L) 0 else sign

    @Cumulative
    final override fun unaryMinus(): Rational = valueOf(numer, denom, scale, -sign)

    // a/b + c/d = (ad + bc)/bd
    @Cumulative
    final override fun plus(other: Rational): Rational {
        val scaleDiff = this.scale.toLong() - other.scale
        val alignedNumer: Int128
        val otherAlignedNumer: Int128
        var scaleAugment = this.scale
        if (scaleDiff < 0) {    // Must be compared separately since LONG_MIN_SCALE != -LONG_MAX_SCALE
            if (scaleDiff < LONG_MIN_SCALE) { // 10^n is not representable as Long, addition is negligible
                return if (sign == -1) this else valueOf(other)
            }
            alignedNumer = MutableInt128(numer)
            otherAlignedNumer = MutableInt128(other.numer) */* = */ tenPow(-scaleDiff.toInt()).toInt128()
        } else if (scaleDiff > 0) {
            if (scaleDiff > LONG_MAX_SCALE) {
                return if (sign == -1) valueOf(other) else this
            }
            alignedNumer = MutableInt128(numer) */* = */ tenPow(scaleDiff.toInt()).toInt128()
            otherAlignedNumer = MutableInt128(other.numer)
            scaleAugment = other.scale
        } else {
            alignedNumer = MutableInt128(numer)
            otherAlignedNumer = MutableInt128(other.numer)
        }
        val ad = alignedNumer */* = */ other.denom.toInt128()
        val bc = otherAlignedNumer */* = */ denom.toInt128()
        val sign: Int
        val numer = if (this.sign == other.sign) {
            sign = if (this.isNegative) -1 else 1
            ad +/* = */ bc
        } else {
            val minuend = if (this.isNegative) bc else ad
            minuend -/* = */ if (minuend === bc) ad else bc
            sign = if (minuend.isNegative) -1 else 1
            minuend/* = */.abs()
        }
        val bd = denom times other.denom
        return valueOf(numer/* = */.abs(), bd/* = */.abs(), scaleAugment, sign) { "$this + $other" }
    }

    // a/b * c/d = ac/cd
    final override fun times(other: Rational): Rational {
        when {
            other.stateEqualsOne() -> return this
            other.stateEquals(NEGATIVE_ONE) -> return -this
            this.stateEqualsOne() -> return other
            other.numer == 0L || this.numer == 0L -> return ZERO
        }
        val numer: Int128 = numer times other.numer
        val denom: Int128 = denom times other.denom
        if (addOverflowsValue(scale, other.scale)) {
            raiseOverflow("$this * $other")
        }
        val scale = scale + other.scale
        return valueOf(numer, denom, scale, productSign(sign, other.sign)) { "$this * $other" }
    }

    // a/b / c/d = a/b * d/c
    final override fun div(other: Rational): Rational = this * other.reciprocal()

    // a/b % c/d = a/b - floor(ad/bc) * c/d
    final override fun rem(other: Rational): Rational {
        val abs = abs()
        val otherAbs = other.abs()
        if (abs <= otherAbs) {
            return if (abs.stateEquals(otherAbs)) ZERO else abs
        }
        return abs - floor(abs / otherAbs) * otherAbs
    }

    // (a/b)^k = a^k/b^k
    final override fun pow(power: Int): Rational {
        if (power == Int.MIN_VALUE) {
            raiseOverflow("$this ^ Int.MIN_VALUE")
        }
        return if (power < 0) powUnsigned(-power).reciprocal() else powUnsigned(power)
    }

    /**
     * Assumes [power] is non-negative.
     */
    private fun powUnsigned(power: Int): Rational {
        if (power == 0 || this.stateEqualsOne()) {
            return ONE
        }
        if (power == 1) {
            return this
        }
        val pow = power.absoluteValue
        val startingNumer = numer
        val startingDenom = denom
        var numer = numer
        var denom = denom
        repeat(pow - 1) {   // Since both fractional components are positive or zero, sign is not an issue
            val lastNumer = numer
            val lastDenom = denom
            numer *= startingNumer
            denom *= startingDenom
            if (numer < lastNumer || denom < lastDenom) {   // Product overflows, perform widening
                return valueOf(
                    MutableInt128(lastNumer).pow(pow),
                    MutableInt128(lastDenom).pow(pow),
                    scale,
                    sign
                ) { "$this ^ $power" }
            }
        }
        return try {
            valueOf(numer * sign, denom, scale) // Long.MIN_VALUE is not power of 10, so negation can never overflow
        } catch (e: CompositeArithmeticException) {
            raiseOverflow("$this ^ $power", e)
        }
    }

    // ---------------------------------------- comparison ----------------------------------------

    final override fun compareTo(other: Rational): Int {
        val scaleDiff = this.sign - other.sign
        when {
            sign != other.sign -> return sign.compareTo(other.sign)
            scaleDiff < LONG_MIN_SCALE -> return -1
            scaleDiff > LONG_MAX_SCALE -> return 1
            this.stateEquals(other) -> return 0
        }
        // ...the digits (radix 10) of the two values may overlap
        return (this.immutable() - other).sign
    }

    final override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + numer.hashCode()
        hash = 31 * hash + denom.hashCode()
        hash = 31 * hash + scale
        return hash * sign
    }

    /**
     * If [other] is guaranteed to be 1, call [stateEqualsOne] instead.
     */
    final override fun stateEquals(other: Rational): Boolean {
        return numer == other.numer && denom == other.denom && scale == other.scale && sign == other.sign
    }

    /**
     * Optimized to only perform one comparison.
     */
    private fun stateEqualsOne() = numer == denom

    final override fun isLong() = denom == 1L && scale >= 0 && scale(numer) + scale <= 18

    // ---------------------------------------- conversion functions ----------------------------------------

    final override fun toInt(): Int = toLong().toInt()

    final override fun toLong(): Long = (numer / denom) * tenPowExact(scale) * sign

    final override fun toDouble(): Double = (numer.toDouble() / denom) * tenPowExact(scale) * sign

    /**
     * Returns this instance.
     */
    final override fun toRational(): Rational = this

    /**
     * Returns a 128-bit integer equal in value to this.
     *
     * Some information may be lost during conversion.
     */
    final override fun toInt128(): Int128 {
        return if (scale < 0) {
            Int128.ZERO
        } else {
            ((MutableInt128(numer) * Int128.TEN.pow(scale)) / Int128(denom)).immutable()
        }
    }

    /**
     * Returns a string representation of this value, in terms of its components, in base-10.
     *
     * If the value of any component would otherwise have no effect on the value of
     * this composite numer as a whole, it is omitted from the returned string
     * (for example, if the [denominator][denom] is 1).
     * If the denominator is 1, the returned string will be in scientific notation.
     *
     * For details on what instances of this class are composed of, see [Rational].
     *
     * On the JVM, to get a string representation of this value after the division is evaluated,
     * call `.toBigDecimal().toString()`.
     */
    @Suppress("RedundantOverride")
    override fun toString(): String = super.toString()

    // TODO remove public
    public final override fun stringValue(): String {
        if (denom == 1L) return buildString {    // Print in scientific notation
            val numerDigits = numer.toString()
            val negativeScale = scale < 0
            append(numerDigits)
            if (negativeScale) {
                if (numerDigits.length == 1) {
                    insert(0, '0')
                    ++scale
                }
                insert(1, '.')
            }
            if (sign == -1) {
                insert(0, '-')
            }
            if (scale != 0) {
                append('e')
                val exponent = if (negativeScale) scale.toLong() - numerDigits.length + 1 else scale.toLong()
                append(exponent)
            }
        }
        val minusSign = if (this.isNegative) "-" else ""
        return if (scale != 0) "($minusSign$numer/$denom)e$scale" else "$minusSign$numer/$denom"
    }

    public companion object {
        @JvmStatic public val NEGATIVE_ONE: Rational = ConstantRational(1, 1, 0, -1, "-1")
        @JvmStatic public val ZERO: Rational = ConstantRational(0, 1, 0, 1, "0")
        @JvmStatic public val ONE: Rational = ConstantRational(1, 1, 0, 1, "1")
        @JvmStatic public val TWO: Rational = ConstantRational(2, 1, 0, 1, "2")

        @JvmStatic public val E: Rational
                = ConstantRational(2718281828459045235, 1, -18, 1, "2.718281828459045235")
        @JvmStatic public val HALF_PI: Rational
                = ConstantRational(1570796326794896619, 1, -18, 1, "1.570796326794896619")
        @JvmStatic public val PI: Rational
                = ConstantRational(3141592653589793238, 1, -18, 1, "3.141592653589793238")
        @JvmStatic public val TWO_PI: Rational
                = ConstantRational(6283185307179586477, 1, -18, 1, "6.283185307179586477")

        @JvmStatic public val MIN_VALUE: Rational
            = ConstantRational(Long.MAX_VALUE, 1, Int.MAX_VALUE, -1, "-9.223372036854775807e2147483665")
        @JvmStatic public val MAX_VALUE: Rational
            = ConstantRational(Long.MAX_VALUE, 1, Int.MAX_VALUE, 1, "9.223372036854775807e2147483665")

        /**
         * The largest integer k where n * 10^k can fit within a 64-bit integer.
         *
         * Equal in value to `-`[LONG_MIN_SCALE]` + 2`.
         */
        private const val LONG_MAX_SCALE = 19

        /**
         * The smallest integer k where n * 10^k is not equal to 0,
         * where n is a 64-bit integer.
         *
         * Equal in value to `-`[LONG_MAX_SCALE]` - 2`.
         */
        private const val LONG_MIN_SCALE = -17

        private class ConstantRational(
            numer: Long,
            denom: Long,
            scale: Int,
            sign: Int,
            override val stringValue: String
        ) : Rational(numer, denom, scale, sign), Constant {
            override fun toString() = stringValue
        }

        // ------------------------------ string conversion ------------------------------

        /**
         * Returns a rational number equal in value to the given string.
         *
         * A string is considered acceptable if it contains:
         * 1. Negative/positive sign *(optional)*
         *    - May be placed inside or outside parentheses
         * 2. Decimal numerator
         *    - A sequence of digits, `'0'..'9'`, optionally containing `'.'`
         *    - Leading and trailing zeros are allowed, but a single dot is not
         * 3. Denominator *(optional)*
         *    - `'/'`, followed by a decimal denominator in the same format as the numerator
         * 4. Exponent in scientific notation *(optional)*
         *    - `'e'` or `'E'`, followed by a signed integer
         *    - Value must be able to fit within 32 bits
         *
         * The decimal numerator and denominator may optionally be surrounded by a single pair of parentheses.
         * However, if an exponent is provided, parentheses are mandatory.
         *
         * The given string must be small enough to be representable and
         * not contain any extraneous characters (for example, whitespace).
         *
         * @throws CompositeFormatException [s] is in an incorrect format
         * @throws CompositeArithmeticException the value cannot be represented accurately as a rational number
         */
        internal fun parse(s: String): Rational {
            fun parseExponent(view: StringView): Int {
                val sign = if (view.char() == '-') {
                    view.move(1)
                    -1
                } else {
                    1
                }
                var exponent = 0L
                do {
                    exponent *= 10
                    exponent += try {
                        view.char().digitToInt()
                    } catch (e: StringIndexOutOfBoundsException) {  // Caught on first iteration
                        raiseIncorrectFormat("missing exponent value", e)
                    } catch (e: IllegalArgumentException) {
                        raiseIncorrectFormat("illegal character embedded in exponent value", e)
                    }
                    if (exponent > Int.MAX_VALUE) {
                        raiseOverflow()
                    }
                    view.move(1)
                } while (view.isWithinBounds())
                return exponent.toInt() * sign
            }

            if (s.isEmpty()) {
                raiseIncorrectFormat("empty string")
            }
            val view = StringView(s)
            var hasExplicitPositive = false
            var insideParentheses = false
            var sign = 1
            while (true) try {
                when (view.char()) {
                    '-' -> {
                        if (sign == -1 || hasExplicitPositive) {
                            raiseIncorrectFormat("illegal embedded sign character")
                        }
                        sign = -1
                        view.move(1)
                    }
                    '+' -> {
                        if (sign == -1 || hasExplicitPositive) {
                            raiseIncorrectFormat("illegal embedded sign character")
                        }
                        hasExplicitPositive = true
                        view.move(1)
                    }
                    '(' -> {
                        if (insideParentheses) {
                            raiseIncorrectFormat("illegal embedded open parenthesis")
                        }
                        insideParentheses = true
                        view.move(1)
                    }
                    '0' -> {
                        view.move(1)
                        if (view.isNotWithinBounds()) {
                            return ZERO
                        }
                        view.move(-1)
                    }
                    '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> break
                    else -> raiseIncorrectFormat("illegal embedded character")
                }
            } catch (e: StringIndexOutOfBoundsException) {
                raiseIncorrectFormat("character expected", e)
            }
            val (numer, numerScale) = ScaledLong.parse(view, stop = "/eE)")
            var denom = 1L
            var denomScale = 0
            if (view.satisfies { it == '/' }) {
                while (view.satisfies { it == '0' }) {
                    view.move(1)
                }
                val denomWithScale = ScaledLong.parse(view, stop = "eE)")
                denom = denomWithScale.component1()
                denomScale = denomWithScale.component2()
            }
            if (insideParentheses) {
                if (!view.satisfies { it == ')' }) {
                    raiseIncorrectFormat("missing closing parenthesis")
                }
                view.move(1)
            }
            var scale = if (view.satisfies { it == 'e' || it == 'E' }) {
                view.move(1)
                parseExponent(view)
            } else {
                0
            }
            if (addOverflowsValue(scale, numerScale)) {
                raiseOverflow()
            }
            scale += numerScale
            if (addOverflowsValue(scale, denomScale)) {
                raiseOverflow()
            }
            scale += denomScale
            return Rational(numer, denom, scale, sign)
        }

        // ------------------------------ helpers ------------------------------

        /**
         * If the exponentiation causes signed integer overflow, throws [CompositeArithmeticException].
         */
        private fun tenPowExact(scale: Int): Long {
            if (scale > 62) {   // log10(10^n) >= log10(2^63 - 1)
                Long.raiseOverflow()
            }
            return tenPow(scale)
        }

        private fun tenPow(scale: Int): Long {
            var result = 1L
            repeat(scale) { result *= 10 }
            return result
        }

        /**
         * Resultant sign represented as 1 or -1.
         * @return the sign of the product/quotient of the two values
         */
        private fun productSign(x: Long, y: Long) = if ((x < 0L) == (y < 0L)) 1 else -1

        /**
         * Returns the base-10 scale of the given value.
         *
         * Assumes [x] is non-negative.
         */
        private fun scale(x: Long): Int {
            var value = x
            var scale = 0
            while (value != 0L && value % 10 == 0L) {
                value /= 10
                ++scale
            }
            return scale
        }

        /**
         * Preserves the state of the supplied arguments.
         *
         * The returned value is immutable and does not alias either argument.
         */
        private fun gcf(x: Int128, y: Int128): Int128 {
            tailrec fun euclideanGCF(max: Int128, min: Int128): Int128 {
                val rem = max % min
                return if (rem == Int128.ZERO) min else euclideanGCF(min, rem.immutable())
            }

            val max = maxOf(x, y)
            val min = if (max === x) y else x
            if (min.stateEquals(Int128.ZERO)) {
                return max.immutable()
            }
            return euclideanGCF(max.uniqueMutable(), min.immutable())
        }

        /**
         * Returns true if the sum is the result of a signed integer overflow.
         *
         * If a result of multiple additions must be checked, this function must be called for each intermediate sum.
         * Also checks for the case [Int.MIN_VALUE] - 1.
         */
        private fun addOverflowsValue(x: Int, y: Int) = (x.toLong() + y) !in Int.MIN_VALUE..Int.MAX_VALUE
    }
}