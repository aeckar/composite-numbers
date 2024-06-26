package io.github.aeckar.kent

import io.github.aeckar.kent.functions.floor
import io.github.aeckar.kent.utils.*
import io.github.aeckar.kent.utils.StringIndexIterator
import io.github.aeckar.kent.utils.productSign
import kotlin.math.absoluteValue

/**
 * Returns a rational number equal to this value over the other as a fraction after simplification.
 * @throws ArithmeticException [other] is 0 or the value is too large or small to be represented accurately
 */
infix fun Int.over(other: Int) = Rational(this.toLong(), other.toLong(), 0)

/**
 * Returns a rational number equal to this value over the other as a fraction after simplification.
 *
 * If this value is [Long.MIN_VALUE], the value stored will be equal to −2^63 + 8.
 * @throws ArithmeticException [other] is 0 or the value is too large or small to be represented accurately
 */
@Suppress("unused")
infix fun Long.over(other: Long) = Rational(this, other, 0)

fun Int.toRational() = when (this) {
    -1 -> Rational.NEGATIVE_ONE
    0 -> Rational.ZERO
    1 -> Rational.ONE
    2 -> Rational.TWO
    else -> this over 1
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
}

/**
 * Returns a rational number equal in value to [x].
 *
 * Some information may be lost after conversion.
 */
fun Rational(x: Int128): Rational {
    val (numer, scale) = ScaledLong(x)
    return Rational(numer, 1, scale, x.sign)
}

/**
 * Returns a rational number with the given [numerator][numer] and [denominator][denom] after simplification.
 * @throws ArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
 */
fun Rational(numer: Long, denom: Long = 1, scaleAugment: Int = 0): Rational {
    return Rational(numer, denom, scaleAugment, productSign(numer.toInt(), denom.toInt()))
}

/**
 * Returns a rational number with the [numerator][numer] and [denominator][denom] after simplification.
 *
 * Some information may be lost during conversion.
 * @throws ArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
 */
fun Rational(numer: Int128, denom: Int128, scaleAugment: Int = 0): Rational {
    return Rational.ONE.valueOf(numer, denom, scaleAugment, productSign(numer.sign, denom.sign)) { "Instantiation" }
}
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
 *
 * Instances of this class are immutable.
 */
@Suppress("EqualsOrHashCode")
open class Rational : CompositeNumber<Rational> {
    // super.lazyString used to cache toString() only

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

    final override val isNegative get() = sign == -1
    final override val isPositive get() = sign == 1
    final override var sign: Int
        protected set

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
     * @throws NumberFormatException [s] is in an incorrect format
     * @throws ArithmeticException the value cannot be represented accurately as a rational number
     */
    constructor(s: String) {
        fun exponentAt(iterator: StringIndexIterator): Int {
            var curIndex = iterator
            val sign = if (curIndex.char() == '-') {
                ++curIndex
                -1
            } else {
                1
            }
            var exponent = 0L
            do {
                exponent *= 10
                exponent += try {
                    curIndex.char().digitToInt()
                } catch (e: NoSuchElementException) {   // Caught on first iteration
                    raiseIncorrectFormat("missing exponent value", e)
                } catch (e: IllegalArgumentException) {
                    raiseIncorrectFormat("illegal character embedded in exponent value", e)
                }
                if (exponent > Int.MAX_VALUE) {
                    raiseOverflow()
                }
                ++curIndex
            } while (curIndex.exists())
            return exponent.toInt() * sign
        }

        if (s.isEmpty()) {
            raiseIncorrectFormat("empty string")
        }
        var curIndex = StringIndexIterator(s)
        var hasExplicitPositive = false
        var insideParentheses = false
        this.sign = 1
        while (true) try {
            when (curIndex.char()) {
                '-' -> {
                    if (sign == -1 || hasExplicitPositive) {
                        raiseIncorrectFormat("illegal embedded sign character")
                    }
                    sign = -1
                    ++curIndex
                }
                '+' -> {
                    if (sign == -1 || hasExplicitPositive) {
                        raiseIncorrectFormat("illegal embedded sign character")
                    }
                    hasExplicitPositive = true
                    ++curIndex
                }
                '(' -> {
                    if (insideParentheses) {
                        raiseIncorrectFormat("illegal embedded open parenthesis")
                    }
                    insideParentheses = true
                    ++curIndex
                }
                '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> break
                '0' -> {
                    ++curIndex
                    if (curIndex.doesNotExist() || curIndex.char() != '0' ) {
                        --curIndex
                        break
                    }
                }
                else -> raiseIncorrectFormat("illegal embedded character")
            }
        } catch (e: StringIndexOutOfBoundsException) {
            raiseIncorrectFormat("character expected", e)
        }
        val (unscaledNumer, numerScale) = ScaledLong.at(curIndex, "/eE)")
        var unscaledDenom = 1L
        var denomScale = 0
        if (curIndex.char { it == '/' }) {
            val denom = ScaledLong.at(curIndex, "eE)")
            unscaledDenom = denom.component1()
            denomScale = denom.component2()
        }
        if (insideParentheses) {
            if (curIndex.char { it != ')' }) {
                raiseIncorrectFormat("missing closing parenthesis")
            }
            ++curIndex
        }
        this.numer = unscaledNumer
        this.denom = unscaledDenom
        this.scale = if (curIndex.char { it == 'e' || it == 'E'}) {
            ++curIndex
            exponentAt(curIndex)
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
    }

    internal constructor(numer: Long, denom: Long, scale: Int, sign: Int) {
        this.numer = numer
        this.denom = denom
        this.scale = scale
        this.sign = sign
    }

    // ---------------------------------------- mutability ----------------------------------------

    override fun immutable() = this

    override fun mutable() = MutableRational(this)

    final override fun uniqueMutable() = MutableRational(this)

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
     * @throws ArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
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

    /**
     * Value function with delegation to by-property constructor.
     * @throws ArithmeticException [denom] is 0 or the value is too large or small to be represented accurately
     */
    // Accessed only by raiseTo(), plus(), and times()
    internal inline fun valueOf(
        numer: Int128,
        denom: Int128,
        scaleAugment: Int,
        sign: Int,
        additionalInfo: () -> String
    ): Rational {
        if (denom.stateEquals(Int128.ZERO)) {
            raiseUndefined("Denominator cannot be zero (numer = $numer)")
        }
        val gcf = gcf(numer, denom)
        val (unscaledNumer, numerScale) = ScaledLong(numer / gcf)   // consume `numer`
        val (unscaledDenom, denomScale) = ScaledLong(denom / gcf)   // consume `denom`
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
        } catch (e: ArithmeticException) {
            raiseOverflow(additionalInfo())
        }
    }

    // ---------------------------------------- arithmetic ----------------------------------------

    /**
     * Returns an instance equal to this value with its decimal part truncated.
     */
    fun toWhole() = when {
        scale < LONG_MIN_SCALE -> ZERO
        scale > LONG_MAX_SCALE || denom == 1L && scale >= 0 -> this
        else -> valueOf(numer / tenPow(-scale), 1, 0, sign)
    }

    /**
     * Returns an instance equal to this when the numerator and denominator are swapped.
     */
    @Cumulative
    fun reciprocal() = valueOf(denom, numer, -scale, sign)

    final override fun signum() = if (numer == 0L) 0 else sign

    @Cumulative
    final override fun unaryMinus() = valueOf(numer, denom, scale, -sign)

    // a/b + c/d = (ad + bc)/bd
    @Cumulative
    final override fun plus(other: Rational): Rational {
        val scaleDiff = this.scale - other.scale
        val alignedNumer: Int128
        val otherAlignedNumer: Int128
        var scale = this.scale
        if (scaleDiff < 0) {
            if (scaleDiff < LONG_MIN_SCALE) { // 10^n is not representable as Long, addition is negligible
                return valueOf(other)
            }
            alignedNumer = MutableInt128(numer)
            otherAlignedNumer = MutableInt128(other.numer) */* = */ tenPow(-scaleDiff).toInt128()
        } else if (scaleDiff > 0) {
            if (scaleDiff > LONG_MAX_SCALE) {
                return this
            }
            alignedNumer = MutableInt128(numer) */* = */ tenPow(scaleDiff).toInt128()
            otherAlignedNumer = MutableInt128(other.numer)
            scale = other.scale
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
        return valueOf(numer/* = */.abs(), bd/* = */.abs(), scale, sign) { "$this + $other" }
    }

    // a/b * c/d = ac/cd
    final override fun times(other: Rational): Rational {
        when {
            other.stateEquals(ONE) -> return this
            other.stateEquals(NEGATIVE_ONE) -> return -this
            this.stateEquals(ONE) -> return other
            other.stateEquals(ZERO) || this.stateEquals(ZERO) -> return ZERO
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
    final override fun div(other: Rational) = this * other.reciprocal()

    // a/b % c/d = floor(ad/bc) * c/d
    final override fun rem(other: Rational): Rational {
        if (this <= other) {
            return this
        }
        return floor(this / other) * other
    }

    // (a/b)^k = a^k/b^k
    final override fun pow(power: Int): Rational {
        if (power == Int.MIN_VALUE) {
            raiseOverflow("$this ^ Int.MIN_VALUE")
        }
        return if (power < 0) powUnchecked(-power)/* = */.reciprocal() else powUnchecked(power)
    }

    /**
     * Assumes [power] is not [Int.MIN_VALUE].
     */
    private fun powUnchecked(power: Int): Rational {
        if (power == 0 || this.stateEquals(ONE)) {
            return ONE
        }
        if (power == 1) {
            return this
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
                    MutableInt128(lastNumer).pow(pow),
                    MutableInt128(lastDenom).pow(pow),
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

    final override /* internal */ fun stateEquals(other: Rational): Boolean {
        return numer == other.numer && denom == other.denom && scale == other.scale && sign == other.sign
    }

    final override /* internal */ fun isLong() = denom == 1L && scale >= 0 && log10(numer) + scale <= 18

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
    final override fun toString(): String {
        lazyString?.let { return it }
        val string: String
        if (denom == 1L) {  // Print in scientific notation
            string = buildString {
                val numer = numer.toString()
                val scale = scale - (numer.length - 1)
                append(numer)
                insert(1, '.')
                if (scale != 0) {
                    append('e')
                    append(scale)
                }
            }
            return string.also { lazyString = it }
        }
        val minusSign = if (this.isNegative) "-" else ""
        val scale = if (scale != 0) "e$scale" else ""
        string = if (scale.isNotEmpty()) "($minusSign$numer/$denom)$scale" else "$minusSign$numer/$denom$scale"
        return string.also { lazyString = it }
    }

    companion object {
        val NEGATIVE_ONE = Rational(1, 1, 0, -1)
        val ZERO = Rational(0, 1, 0, 1)
        val ONE = Rational(1, 1, 0, 1)
        val TWO = Rational(2, 1, 0, 1)
        val E = Rational(2718281828459045235, 1, -18, 1)
        val HALF_PI = Rational(1570796326794896619, 1, -18, 1)
        val PI = Rational(3141592653589793238, 1, -18, 1)
        val TWO_PI = Rational(6283185307179586477, 1, -18, 1)

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

        // ------------------------------ helper functions ------------------------------

        /**
         * If the exponentiation causes signed integer overflow, throws [ArithmeticException].
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

        /**
         * Preserves the state of the supplied arguments.
         *
         * The returned value is mutable and does not alias either argument.
         */
        private fun gcf(x: Int128, y: Int128): Int128 {
            tailrec fun euclideanGCF(max: Int128, min: Int128): Int128 {
                val rem = max %/* = */ min.immutable()
                return if (rem == Int128.ZERO) min else euclideanGCF(min, rem)
            }

            val max = maxOf(x, y)
            val min = if (max === x) y else x
            if (min.stateEquals(Int128.ZERO)) {
                return max.immutable()
            }
            return euclideanGCF(max.uniqueMutable(), min.uniqueMutable())
        }

        /**
         * Returns true if the sum is the result of a signed integer overflow.
         *
         * If a result of multiple additions must be checked, this function must be called for each intermediate sum.
         * Also checks for the case [Int.MIN_VALUE] - 1.
         */
        private fun addOverflowsValue(x: Int, y: Int): Boolean {
            val sum = x.toLong() + y
            return if (sum < 0L) sum < Int.MIN_VALUE else sum > Int.MAX_VALUE
        }
    }
}