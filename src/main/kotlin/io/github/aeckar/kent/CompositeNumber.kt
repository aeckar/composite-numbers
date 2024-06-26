package io.github.aeckar.kent

/**
 * A numeric value composed of multiple primitive values.
 *
 * Provides an interface for classes extending the capabilities of built-in [numeric][Number] types
 * without providing for arbitrary-precision arithmetic.
 *
 * Instances can be converted through truncation to these numeric types.
 * Additionally, composite numbers may also be converted to and from their respective string representation.
 * Conversions from other types using the constructors provided by implementations result in no information loss.
 *
 * Instances of this class are:
 * - Immutable: Public state cannot be modified
 * - Unique: There exists only one possible state for a given value
 * - Limited precision: Precision is fixed to a given number of binary digits (however, they may be scaled)
 * - Efficient: Fixed-precision allows for certain key optimizations to be made.
 * - Accurate: If the result of an operation is too large or small to be represented accurately
 * as a composite number, such as in the event of an integer overflow, an [ArithmeticException] will be thrown
 *
 * Results of computationally expensive operations are not cached,
 * and should be stored in a variable if used more than once.
 * The one exception to this is [toString].
 *
 * When a value is described as being "too large", it is either
 * too high or too low to be accurately represented as the given composite number type.
 * @param T the inheritor of this class
 * @see Int128
 * @see Rational
 */
@Suppress("EqualsOrHashCode")
sealed class CompositeNumber<T : CompositeNumber<T>> : Number(), Comparable<T> {
    protected var lazyString: String? = null

    /**
     * -1 if this value is negative, else 1.
     *
     * Should be used instead of [signum] when equality to 0 is irrelevant.
     */
    abstract val sign: Int

    /**
     * True if this value is negative.
     */
    abstract val isNegative: Boolean

    /**
     * True if this value is positive.
     */
    abstract val isPositive: Boolean


    // ------------------------------ mutability --------------------

    /*
        Conversion between mutable and immutable instances should be restricted the specific inheritor.
        Operations with argument(s) of type T that utilize mutability will generally reside with the same class.
        Restricting this functionality from the user reduces the chances of mutability
        being used incorrectly by causing unwanted side effects.

        Instances where a value is being mutated should be explicitly stated using comments.
        Mutable instances should not be declared using `var`, as it may cause aliasing.
     */

    /**
     * Returns an immutable composite number equal in value to this.
     *
     * If chained to an operation, this function should be called second.
     * If the caller is guaranteed to be immutable, this function does nothing.
     *
     * Overrides of this function should never be marked final.
     */
    internal abstract fun immutable(): T

    /**
     * Returns a mutable composite number equal in value to this.
     *
     * If chained to an operation, this function should be called first.
     * If the caller is guaranteed to be mutable, this function does nothing.
     *
     * Overrides of this function should never be marked final.
     */
    @Cumulative
    internal abstract fun mutable(): T

    /**
     * Returns a unique mutable composite number equal in value to this.
     *
     * If chained to an operation, this function should be called first.
     */
    internal abstract fun uniqueMutable(): T

    /**
     * Returns a new instance with the given value, or if [mutable], the same instance with the value stored.
     *
     * Overrides of this function will typically delegate the responsibility
     * of value creation to another value function.
     *
     * Overrides of this function should never be marked final.
     */
    @Cumulative
    internal abstract fun valueOf(other: T): T

    // ------------------------------ arithmetic ------------------------------

    /**
     * Returns an instance equal in value to the absolute value of this
     */
    @Suppress("UNCHECKED_CAST")
    @Cumulative
    fun abs(): T = if (sign < 0) -this else this as T

    /**
     * Returns an instance equal in value to the difference.
     */
    @Cumulative
    operator fun minus(other: T) = this + (-other)

    /**
     * Returns -1 if this is negative, 0 if zero, or 1 if positive.
     * @see sign
     */
    abstract fun signum(): Int

    /**
     * Returns an instance equal in value to this, negated.
     */
    @Cumulative
    abstract operator fun unaryMinus(): T

    /**
     * Returns an instance equal in value to the sum.
     */
    @Cumulative
    abstract operator fun plus(other: T): T

    /**
     * Returns an instance equal in value to the product.
     *
     * This function is [cumulative][Cumulative] when neither argument is 0 nor 1.
     */
    abstract operator fun times(other: T): T

    /**
     * Returns an instance equal in value to the quotient.
     */
    abstract operator fun div(other: T): T

    /**
     * Returns an instance equal in value to the remainder of the division.
     *
     * The returned value is always non-negative.
     */
    abstract operator fun rem(other: T): T

    /**
     * Returns an instance equal in value to this raised to [power].
     *
     * When this and `power` are both 0, this function returns a value equal to 1.
     */
    abstract fun pow(power: Int): T

    // ------------------------------ comparison ------------------------------

    /**
     * Compares this value to the other.
     *
     * See [compareTo][Comparable.compareTo] for details.
     */
    operator fun compareTo(value: Int) = compareTo(value.toLong())

    /**
     * Compares this value to the other.
     *
     * See [compareTo][Comparable.compareTo] for details.
     */
    operator fun compareTo(value: Long) = if (!this.isLong()) sign else toLong().compareTo(value)

    /**
     * Comparison to composite numbers, built-in [numbers][Number], and
     * strings agreeing with the format specified by the string constructor allowed.
     *
     * Does not test for equality to arbitrary-precision numbers. To do this, use compareTo instead.
     * @return true if the numerical values of the objects are equal
     */
    final override fun equals(other: Any?): Boolean {
        return other === this || when (other) {
            is Rational -> other.stateEquals(toRational())
            is Int128 -> other.stateEquals(toInt128())

            is String -> {
                if (this is Rational) Rational(other).stateEquals(this) else Int128(other).stateEquals(this as Int128)
            }

            is Double, is Float -> (other as Number).toDouble() == toDouble()
            is Number -> isLong() && other.toLong() == toLong()
            else -> false
        }
    }

    abstract override fun hashCode(): Int

    internal abstract fun stateEquals(other: T): Boolean

    /**
     * If true, this can be represented as a 64-bit integer without losing information.
     */
    internal abstract fun isLong(): Boolean

    // ------------------------------ conversions ------------------------------

    final override fun toByte() = toInt().toByte()
    final override fun toShort() = toInt().toShort()
    final override fun toFloat() = toDouble().toFloat()

    /**
     * Returns a rational number equal in value to this.
     */
    abstract fun toRational(): Rational

    /**
     * Returns a 128-bit integer equal in value to this.
     */
    abstract fun toInt128(): Int128

    /**
     * Returns a string representation of this value in base 10.
     *
     * When passed to the string constructor of the inheritor, creates an instance equal in value to this.
     */
    abstract override fun toString(): String
}