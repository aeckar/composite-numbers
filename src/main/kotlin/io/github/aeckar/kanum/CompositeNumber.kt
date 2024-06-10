package io.github.aeckar.kanum

// ------------------------------ arithmetic ------------------------------



// ------------------------------ class definition ------------------------------

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
 * Implementations of this class are:
 * - Immutable: Public state cannot be modified
 * - Unique: There exists only one possible state for a given value
 * - Limited precision: Precision is fixed to a given number of binary digits (however, they may be scaled)
 * - Efficient: Fixed-precision allows for certain key optimizations to be made
 *
 * @param T the inheritor of this class
 */
@Suppress("EqualsOrHashCode")
sealed class CompositeNumber<T : CompositeNumber<T>> : Number(), Comparable<T> {
    /**
     * -1 if this value is negative, else 1.
     *
     * Should be used instead of [signum] when equality to 0 is irrelevant.
     */
    abstract val sign: Int

    // ------------------------------ arithmetic ------------------------------

    /**
     * Returns an instance equal in value to the absolute value of this
     */
    @Suppress("UNCHECKED_CAST")
    fun abs(): T = if (sign < 0) -this else this as T

    /**
     * Returns a new instance equal in value to the difference.
     */
    operator fun minus(other: T) = this + (-other)

    /**
     * Returns -1 if this is negative, 0 if zero, or 1 if positive.
     * @see sign
     */
    abstract fun signum(): Int

    /**
     * Returns a new instance equal in value to this, negated.
     */
    abstract operator fun unaryMinus(): T

    /**
     * Returns a new instance equal in value to the sum.
     */
    abstract operator fun plus(other: T): T

    /**
     * Returns a new instance equal in value to the product.
     */
    abstract operator fun times(other: T): T

    /**
     * Returns a new instance equal in value to the quotient.
     */
    abstract operator fun div(other: T): T

    /**
     * Returns an instance equal in value to this raised to [power].
     */
    abstract fun pow(power: Int): T

    // ------------------------------ comparison ------------------------------

    /**
     * Compares this value to the other.
     *
     * See [compareTo][Comparable.compareTo] for details.
     */
    operator fun compareTo(value: Long) = toLong().compareTo(value)

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
            is Number -> isLongValue() && other.toLong() == toLong()
            else -> false
        }
    }

    abstract override fun hashCode(): Int

    internal abstract fun stateEquals(other: T): Boolean

    protected abstract fun isWhole(): Boolean

    /**
     * If true, this can be represented as a 64-bit integer without losing information.
     */
    protected abstract fun isLongValue(): Boolean

    // ------------------------------ conversion functions ------------------------------

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
     * Returns a string representation of this value.
     *
     * When passed to the string constructor, creates an instance equal in value to this.
     */
    abstract override fun toString(): String
}