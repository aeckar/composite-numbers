package io.github.aeckar.kanum

import io.github.aeckar.kanum.utils.*
import java.lang.Integer.toBinaryString
import kotlin.math.sign

// ------------------------------ arithmetic ------------------------------

/**
 * Resultant sign represented as 1 or -1.
 * @return the sign of the product/quotient of the two values
 */
private fun productSign(x: Int, y: Int) = if ((x < 0) == (y < 0)) 1 else -1

/**
 * The value of a quarter in a 128-bit integer with value [x] when there is no information stored in it.
 *
 * @return a value where all bits are 1 or 0 depending on whether [sign] is -1 or 1, respectively
 */
private fun blank(x: Long) = if (x < 0L) -1 /* all 1 bits */ else 0

// ------------------------------ class definitions ------------------------------

/**
 * A mutable 128-bit integer.
 *
 * See [Cumulative] for details on composite number mutability.
 */
internal class MutableInt128 : Int128 {
    constructor(lower: Long) : super(lower)
    constructor(unique: Int128) : super(unique.q1, unique.q2, unique.q3, unique.q4)

    override fun immutable() = Int128(q1, q2, q3, q4)
    override fun mutable() = this

    override fun valueOf(q1: Int, q2: Int, q3: Int, q4: Int) = this.also {
        it.q1 = q1; it.q3 = q3
        it.q2 = q2; it.q4 = q4
    }
}

/**
 * Returns a 128-bit integer equal in value to the given string.
 *
 * The given string must be small enough to be representable and
 * not contain any extraneous characters (for example, whitespace).
 * It may optionally be prefixed by a negative sign.
 * @throws NumberFormatException [s] is in an incorrect format
 */
fun Int128(s: String) = Int128.parse(s)

/**
 * A 128-bit integer in two's complement format.
 *
 * Operations performed on instances of this class are analogous to those of built-in integer types, where:
 * - The most significant bit (of the [upper half][high]) determines the sign
 * - Result of division and remainder are truncated
 *
 * Contrary to the behavior of primitive types, operations will throw [ArithmeticException] on overflow.
 */
@Suppress("EqualsOrHashCode")
open class Int128 : CompositeNumber<Int128> {
    internal var q1: Int
    internal var q2: Int
    internal var q3: Int
    internal var q4: Int

    internal val isNegative inline get() = q1 < 0
    private val isPositive inline get() = q1 >= 0

    final override val sign get() = if (isNegative) -1 else 1

    /**
     * Returns a 128-bit integer with its lowest 64 bits equivalent to the given value, unsigned.
     *
     * Performs the same widening conversion as a primitive type would.
     * As such, the sign of the original value is preserved.
     */
    constructor(q3q4: Long) {
        val blank = blank(q3q4)
        this.q1 = blank
        this.q2 = blank
        this.q3 = q3q4.high
        this.q4 = q3q4.low
    }

    /**
     * Returns a 128-bit integer with its lowest 32 bits equivalent to the given value, unsigned.
     *
     * Performs the same widening conversion as a primitive type would.
     * As such, the sign of the original value is preserved.
     */
    constructor(q4: Int) {
        val blank = blank(q4.toLong())
        this.q1 = blank
        this.q2 = blank
        this.q3 = blank
        this.q4 = q4
    }

    constructor(q1: Int, q2: Int, q3: Int, q4: Int) {
        this.q1 = q1
        this.q2 = q2
        this.q3 = q3
        this.q4 = q4
    }

    // ---------------------------------------- instance recycling ----------------------------------------

    /**
     * Returns a non-cumulative 128-bit integer equal in value to this.
     *
     * If chained to an operation, this function should be called second.
     */
    protected open fun immutable() = this

    /**
     * Returns a cumulative 128-bit integer equal in value to this.
     *
     * If chained to an operation, this function should be called first.
     */
    @Cumulative
    internal open fun mutable(): Int128 = MutableInt128(this)

    /**
     * Returns a new instance with the given value, or
     * if [mutable][MutableInt128], the same instance with the value stored.
     */
    protected open fun valueOf(q1: Int, q2: Int, q3: Int, q4: Int) = Int128(q1, q2, q3, q4)

    /**
     * Returns a new instance with the given value, or
     * if [mutable][MutableInt128], the same instance with the value stored.
     */
    // Accessed by Companion.parse
    protected fun valueOf(q4: Int) = valueOf(0, 0, 0, q4)

    /**
     * Returns a new instance with the given value, or
     * if [mutable][MutableInt128], the same instance with the value stored.
     */
    private fun valueOf(other: Int128) = with (other) { valueOf(q1, q2, q3, q4) }

    // ---------------------------------------- destructuring ----------------------------------------

    /**
     * Returns the first (most significant) 32 bits of this integer.
     */
    operator fun component1() = q1

    /**
     * Returns the second 32 bits of this integer.
     */
    operator fun component2() = q2

    /**
     * Returns the third 32 bits of this integer.
     */
    operator fun component3() = q3

    /**
     * Returns the fourth (least significant) 32 bits of this integer.
     */
    operator fun component4() = q4

    /**
     * The value of a quarter when there is no information stored in it.
     *
     * @return a value where all bits are 1 or 0 depending on whether [sign] is -1 or 1, respectively
     */
    private fun blank(sign: Int = this.sign) = sign shr 1

    /**
     * Returns the number of consecutive least significant quarters which are not [blank].
     *
     * @return a value from 0 to 4
     */
    private fun magnitude(): Int {
        val blank = blank()
        return when {
            q1 != blank -> 4
            q2 != blank -> 3
            q3 != blank -> 2
            q4 != blank -> 1
            else -> 0
        }
    }

    /**
     * [q1] cast to a long as an unsigned integer.
     *
     * Does not preserve sign.
     */
    private fun q1w() = q1.widen()

    /**
     * [q2] cast to a long as an unsigned integer.
     *
     * Does not preserve sign.
     */
    private fun q2w() = q2.widen()

    /**
     * [q3] cast to a long as an unsigned integer.
     *
     * Does not preserve sign.
     */
    private fun q3w() = q3.widen()

    /**
     * [q4] cast to a long as an unsigned integer.
     *
     * Does not preserve sign.
     */
    private fun q4w() = q4.widen()

    // ---------------------------------------- bitwise operations ----------------------------------------

    /**
     * Performs a bitwise inversion on this value and returns the result.
     */
    @Suppress("unused")
    @Cumulative
    fun inv() = valueOf(q1.inv(), q2.inv(), q3.inv(), q4.inv())

    /**
     * Performs a bitwise left shift on this value and returns the result.
     *
     * If shifted by more than 128 bits, returns [zero][ZERO].
     * @throws IllegalArgumentException [count] is negative
     */
    infix fun shl(count: Int): Int128 {
        /**
         * Returns the quarter at the current position after a left shift.
         */
        fun q(qCur: Int, qNext: Int, qShift: Int) = (qCur shl qShift) or (qNext ushr (32 - qShift))

        ensureValidShift(count)
        if (count == 0) {
            return this
        }
        if (count > 128) {
            return ZERO
        }
        val qShift = count % 32
        return when (count / 32) {
            0 -> valueOf(q(q1, q2, qShift), q(q2, q3, qShift), q(q3, q4, qShift), q4 shl qShift)
            1 -> valueOf(q(q2, q3, qShift), q(q3, q4, qShift), q4 shl qShift, 0)
            2 -> valueOf(q(q3, q4, qShift), q4 shl qShift, 0, 0)
            else /* = 3 */ -> valueOf(q4 shl qShift, 0, 0, 0)
        }
    }

    /**
     * Performs a bitwise signed right shift on this value and returns the result.
     */
    infix fun shr(count: Int): Int128 {
        ensureValidShift(count)
        if (count == 0) {
            return this
        }
        val blank = blank()
        if (count > 128) {
            valueOf(blank, blank, blank, blank)
        }
        val qShift = count % 32
        val qMove = count / 32
        return when (qMove) {
            0 -> valueOf(q1 shr qShift, q(q1, q2, qShift), q(q2, q3, qShift), q(q3, q4, qShift))
            1 -> valueOf(blank, q1 shr qShift, q(q1, q2, qShift), q(q2, q3, qShift))
            2 -> valueOf(blank, blank, q1 shr qShift, q(q1, q2, qShift))
            else /* = 3 */ -> valueOf(blank, blank, blank, q1 shr qShift)
        }
    }

    /**
     * Performs a bitwise unsigned right shift on this value and returns the result.
     */
    infix fun ushr(count: Int): Int128 {
        ensureValidShift(count)
        return unsignedRightShift(count)
    }

    /**
     * Assumes [count] is non-negative.
     */
    private fun unsignedRightShift(count: Int): Int128 {
        if (count == 0) {
            return this
        }
        if (count > 128) {
            return ZERO
        }
        val qShift = count % 32
        val qMove = count / 32
        return when (qMove) {
            0 -> valueOf(q1 ushr qShift, q(q1, q2, qShift), q(q2, q3, qShift), q(q3, q4, qShift))
            1 -> valueOf(0, q1 ushr qShift, q(q1, q2, qShift), q(q2, q3, qShift))
            2 -> valueOf(0, 0, q1 ushr qShift, q(q1, q2, qShift))
            else /* = 3 */ -> valueOf(0, 0, 0, q1 ushr qShift)
        }
    }

    /**
     * Computes the bitwise `and` of the two values and returns the result.
     */
    @Suppress("unused")
    @Cumulative
    infix fun and(other: Int128) = valueOf(q1 and other.q1, q2 and other.q2, q3 and other.q3, q4 and other.q4)

    /**
     * Computes the bitwise `or` of the two values and returns the result.
     */
    @Suppress("unused")
    @Cumulative
    infix fun or(other: Int128) = valueOf(q1 or other.q1, q2 or other.q2, q3 or other.q3, q4 or other.q4)

    /**
     * Computes the bitwise `xor` of the two values and returns the result.
     */
    @Suppress("unused")
    @Cumulative
    infix fun xor(other: Int128) = valueOf(q1 xor other.q1, q2 xor other.q2, q3 xor other.q3, q4 xor other.q4)

    // ---------------------------------------- arithmetic ----------------------------------------

    @Cumulative
    final override operator fun unaryMinus(): Int128 {
        if (this.stateEquals(MIN_VALUE)) {
            raiseOverflow("Result of negation")
        }
        var q1i = q1.inv()
        var q2i = q2.inv()
        var q3i = q3.inv()
        var q4i = q4.inv()
        val q4plus1 = q4i + 1
        if (addIntOverflows(q4i, 1, q4plus1)) {
            val q3plus1 = q3i + 1
            if (addIntOverflows(q3i, 1, q3plus1)) {
                val q2plus1 = q2i + 1
                if (addIntOverflows(q2i, 1, q2plus1)) {
                    q1i += 1
                }
                q2i = q2plus1
            }
            q3i = q3plus1
        }
        q4i = q4plus1
        return valueOf(q1i, q2i, q3i, q4i)
    }

    @Cumulative
    final override fun pow(power: Int): Int128 {
        var result = this.mutable()
        val pow = when (power) {
            0 -> return ONE
            1 -> return this
            2 -> TWO
            10 -> TEN
            else -> Int128(power)
        }
        try {
            repeat(power) { result *= pow }
        } catch (_: ArithmeticException) {
            raiseOverflow("Result of exponentiation")
        }
        return valueOf(result)
    }

    final override fun signum() = if (q4 == 0 && q3 == 0 && q2 == 0 && q1 == 0) 0 else sign
    
    // ((a << 32) + b) + ((c << 1) + d) = ((a + c) << 32) + (b + d)
    @Cumulative
    final override fun plus(other: Int128): Int128 {
        val q4 = q4w() + other.q4w()
        val q3 = q3w() + other.q3w() + q4.high
        val q2 = q2w() + other.q2w() + q3.high
        val q1 = q1w() + other.q1w() + q2.high
        if (q1.isNotInt()) {
            raiseOverflow("Sum")
        }
        return valueOf(q1.low, q2.low, q3.low, q4.low)
    }

    /*
        Multiplication of 128-Bit Integers
        See https://web.archive.org/web/20240609155726/https://cs.stackexchange.com/questions/140881/how-do-computers-perform-operations-on-numbers-that-are-larger-than-64-bits/140950#140950

        given
            x = (a << 96) + (b << 64) + (c << 32) + d
            y = (e << 96) + (f << 64) + (g << 32) + h,

            or in other words,

            x = a<<32*3 + b<<32*2 + c<<32*1 + d
            y = e<<32*3 + f<<32*2 + g<<32*1 + h,
        let
            xy = ((a * 2^96) + (b * 2^64) + (c * 2^32) + d)((e * 2^96) + (f * 2^64) + (g * 2^32) + h)
               = e(2^192a + 2^160b + 2^128c + 2^96d) +
                 f(2^160a + 2^128b + 2^96c + 2^64d) +
                 a(2^128g + 2^96h) +
                 g(2^96b + 2^64c + 2^32d) +
                 2^64bh +
                 2^32ch +
                 dh
               = e(a<<32*6 + b<<32*5  + c<<32*4  + d<<32*3) +
                 f(a<<32*5 + b<<32*4  + c<<32*3  + d<<32*2) +
                 a(g<<32*4 + h<<32*3) +
                 g(b<<32*3 + c<<32*2  + d<<32*1) +
                 bh<<32*1 +
                 ch<<32*1 +
                 dh

            if a, b, e, & f are 0, xy = cg<<32*2 + (dg + ch)<<32*1 + dh
            if a, b, & c are 0,    xy = de<<32*3 + df<<32*2 + dg<<32*1 + dh
            if a, b, & e are 0,    xy = cf<<32*3 + (df + cg)<<32*2 + (dg + ch)<<32*1 + dh
        where
            - x & y are 128-bit integers
            - a, b, c, ..., h are 32-bit integers, and
            - The product of two 32-bit integers is a 64-bit integer

        For xy to fit within a 128-bit integer:
            - a, b, e, & f must be 0 (given that the product of two 64-bit integers can, at most, be 128 bits long), or
            - a, b, & c must be 0, or
            - a, b & e must be zero and the most significant 32 bits must not carry over a bit

        The above requirements apply for either x * y or y * x.
     */
    final override fun times(other: Int128): Int128 {
        fun addMultiply(q1q2partial: Long, q2q3partial: Long, q3q4partial: Long): Int128 {
            val (q3summand2, q4) = q3q4partial
            val (q1summand, q2summand1) = q1q2partial
            val (q2summand2, q3summand1) = q2q3partial
            val q3 = q3summand1 + q3summand2
            var carry = (addIntOverflows(q3summand1, q3summand2, q3)).toInt()
            val q2carry = q2summand2 + carry
            val q2 = q2summand1 + q2carry
            carry = (addIntOverflows(q2summand1, q2carry, q2) || addIntOverflows(q2summand1, q2summand2, q2)).toInt()
            val q1 = q1summand + carry
            if (addValueOverflows(q1summand, carry, q1)) {
                raiseOverflow("Product")
            }
            return valueOf(q1, q2, q3, q4)
        }

        fun addMultiply(q0q1partial: Long, q1q2partial: Long, q2q3partial: Long, q3q4partial: Long): Int128 {
            if (q0q1partial.high != 0) {
                raiseOverflow("Product")
            }
            val (q1summand1, q2, q3, q4) = addMultiply(q1q2partial, q2q3partial, q3q4partial)
            val q1summand2 = q0q1partial.low
            val q1 = q1summand1 + q1summand2
            if (addValueOverflows(q1summand1, q1summand2, q1)) {
                raiseOverflow("Product")
            }
            return valueOf(q1, q2, q3, q4)
        }

        // cg<<32*2 + (dg + ch)<<32*1 + dh
        fun int64TimesInt64(i64a: Int128 /* c<<32*1 d */, i64b: Int128 /* g<<32*1 h */): Int128 {
            val g = i64b.q3w(); val c = i64a.q3w()
            val h = i64b.q4w(); val d = i64a.q4w()
            return addMultiply(c * g, (d * g) + (c * h), d * h)
        }

        // de<<32*3 + df<<32*2 + dg<<32*1 + dh
        fun int32TimesInt128(d: Long, i128: Int128): Int128 {
            return addMultiply(d * i128.q1w(), d * i128.q2w(), d * i128.q3w(), d * i128.q4w())
        }

        // cf<<32*3 + (df + cg)<<32*2 + (dg + ch)<<32*1 + dh
        fun int64TimesInt96(i64: Int128, i96: Int128): Int128 {
            val f = i96.q2w();  val c = i64.q3w()
            val g = i96.q3w();  val d = i64.q4w()
            val h = i96.q4w()
            return addMultiply(c * f, (d * f) + (c * g), (d * g) + (c * h), d * h)
        }

        // TODO specialize for i32 * i32

        if (other.stateEquals(ONE)) {
            return this
        }
        if (this.stateEquals(ONE)) {
            return other
        }
        val mag = magnitude()
        val otherMag = other.magnitude()
        if (mag == 0 || otherMag == 0) {
            return ZERO
        }
        if (otherMag <= 2) when {
            mag <= 2 -> return int64TimesInt64(this, other)
            otherMag == 1 -> return int32TimesInt128(other.q4w(), this)
            mag == 3 -> return int64TimesInt96(other, this)
        }
        if (mag <= 2) {
            if (mag == 1) {
                return int32TimesInt128(this.q4w(), other)
            }
            if (otherMag == 3) {
                return int64TimesInt96(this, other)
            }
        }
        raiseOverflow("Product")
    }

    /**
     * Returns the result of the [division][div] paired to the result of the [remainder][rem], respectively.
     */
    infix fun divAndRem(other: Int128): Pair<Int128, Int128> = divide(other, DivisionType.BOTH)

    // Uses shift-subtract algorithm
    final override fun div(other: Int128): Int128 = divide(other, DivisionType.QUOTIENT)

    /**
     * Returns a new instance equal in value to the remainder of the division.
     */
    operator fun rem(other: Int128): Int128 = divide(other, DivisionType.REMAINDER)

    private fun <T> divide(other: Int128, division: DivisionType): T {
        if (other.stateEquals(ONE)) {
            return division.result(
                quotient = { this },
                remainder = { ZERO }
            )
        }
        if (other.stateEquals(ZERO)) {
            raiseUndefined("Divisor cannot be 0")
        }
        val sign = productSign(sign, other.sign)
        val divisor = other.mutable().abs() // Will be used as accumulator in some instances
        if (other > this) {
            return division.result(
                quotient = { ZERO },
                remainder = { this.abs() }
            )
        }
        var dividend = this.abs().immutable()
        var pow2 = 0
        do {
            val shifted = dividend.unsignedRightShift(1)
            if (shifted <= divisor) {
                if (shifted == divisor) {
                    return division.result(
                        quotient = { (divisor/* = */.valueOf(1) shl pow2) * valueOf(sign) },
                        remainder = { ZERO }
                    )
                }
                break
            }
            dividend = shifted
            ++pow2
        } while (true)
        var additions = 0
        do {
            divisor +/* = */ divisor
            if (divisor > dividend) {
                val nextMultiple = MutableInt128(divisor)
                val shiftQuotient = MutableInt128(divisor/* = */.valueOf(1) shl pow2)
                return division.result(
                    quotient = { (shiftQuotient + divisor/* = */.valueOf(additions)) * divisor.valueOf(sign) },
                    remainder = { nextMultiple - dividend }
                )
            }
            ++additions
        } while (true)
    }

    private enum class DivisionType {
        QUOTIENT, REMAINDER, BOTH;

        @Suppress("UNCHECKED_CAST")
        inline fun <T : Any> result(quotient: () -> Int128, remainder: () -> Int128) = when (this) {
            QUOTIENT -> quotient()
            REMAINDER -> remainder()
            else -> quotient() to remainder()
        } as T
    }

    // ---------------------------------------- comparison ----------------------------------------

    final override fun compareTo(other: Int128) = when {
        this.isNegative && other.isPositive -> -1
        this.isPositive && other.isNegative -> 1
        else -> valueCompareTo(other)
    }

    final override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + q1
        hash = 31 * hash + q2
        hash = 31 * hash + q3
        hash = 31 * hash + q4
        return hash
    }

    final override fun isWhole() = true

    final override fun isLongValue(): Boolean {
        val blank = blank()
        return q1 == blank && q2 == blank
    }

    final override /* internal */ fun stateEquals(other: Int128): Boolean {
        return q1 == other.q1 && q2 == other.q2 && q3 == other.q3 && q4 == other.q4
    }

    /**
     * Compares this to [other], assuming that both have the same [sign].
     */
    private fun valueCompareTo(other: Int128): Int {
        var difference = q1.compareTo(other.q1);    if (difference != 0) return difference
            difference = q2.compareTo(other.q2);    if (difference != 0) return difference
            difference = q3.compareTo(other.q3);    if (difference != 0) return difference
        return q4.compareTo(other.q4)
    }

    // ---------------------------------------- conversion functions ----------------------------------------

    /**
     * Returns a string representation of this with radix 2 in two's complement form.
     *
     * When passed to the string constructor, creates an instance equal in value to this.
     * The returned string is padded at the start with zeroes, if necessary, to be exactly 128 characters long.
     *
     * To get an ordinary binary representation with an optional negative sign, use [toString].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun twosComplement(): String {
        fun Int.to2c() = toBinaryString(this).padStart(32, '0')

        return q1.to2c() + q2.to2c() + q3.to2c() + q4.to2c()
    }

    /**
     * Returns a string representation of this value with the given radix.
     *
     * When passed to the string constructor, creates an instance equal in value to this.
     *
     * To get a binary representation in two's complement form, use [twosComplement].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun toString(radix: Int): String = toBigInteger().toString(radix)

    final override fun toInt() = q4

    final override fun toLong() =(q3w() shl 32) or q4w()

    final override fun toRational() = ScaledInt64(this).let { (numer, scale) -> Rational(numer, 1L, scale, sign) }

    /**
     * Returns this instance.
     */
    final override fun toInt128() = this

    final override fun toDouble() = ((q1w() shl 32) or q2w()) * 1.8446744073709552E19 /* 2^64 */ + toLong().toDouble()

    /**
     * Returns a string representation of this value in base 10.
     */
    final override fun toString() = toBigInteger().toString()

    companion object {
        val ZERO = Int128(0, 0, 0, 0)
        val ONE = Int128(0, 0, 0, 1)
        val TWO = Int128(0, 0, 0, 2)
        val TEN = Int128(0, 0, 0, 10)

        val MIN_VALUE = Int128(Int.MIN_VALUE, 0, 0, 0)
        val MAX_VALUE = Int128(Int.MAX_VALUE, -1, -1, -1)

        // ---------------------------------------- destructuring ----------------------------------------

        /**
         * Returns the quarter at the current position after a bitwise right shift.
         */
        fun q(qLast: Int, qCur: Int, qShift: Int) = (qLast shl (32 - qShift)) or (qCur ushr qShift)

        // ---------------------------------------- conversion functions ----------------------------------------

        internal fun parse(s: String): Int128 {
            var cursor = s.lastIndex
            val firstIndex: Int
            val negateResult: Boolean
            val first = try {
                s[0]
            } catch (e: StringIndexOutOfBoundsException) {
                raiseIncorrectFormat("empty string")
            }
            if (first == '-') {
                firstIndex = 1
                negateResult = true
            } else {
                firstIndex = 0
                negateResult = false
            }
            val digit: Int128 = MutableInt128(ZERO)
            val value: Int128 = MutableInt128(ZERO)
            var tenPow = ONE
            while (cursor >= firstIndex) try {
                /* digit = */ digit.valueOf(s[cursor].digitToInt())
                /* value = */ value + (/* (maybe) digit = */ digit * tenPow)
                tenPow *= TEN
                --cursor
            } catch (e: IllegalArgumentException) {
                raiseIncorrectFormat("illegal digit", cause = e)
            } catch (e: ArithmeticException) {       // digit * tenPow overflows
                raiseOverflow(cause = e)
            }
            val result = if (negateResult) /* value = */ -value else value
            return result.immutable()
        }

        // ---------------------------------------- arithmetic ----------------------------------------

        /**
         * C-style boolean-to-integer conversion. 1 if true, 0 if false.
         */
        private fun Boolean.toInt() = if (this) 1 else 0

        /**
         * Returns true if any of its most significant 32 bits are 1.
         *
         * Assumes that this value is a sum of two unsigned 32-bit integers and is positive.
         */
        private fun Long.isNotInt() = this > Int.MAX_VALUE

        private fun ensureValidShift(count: Int) = require(count >= 0) { "Shift argument cannot be negative" }
    }
}