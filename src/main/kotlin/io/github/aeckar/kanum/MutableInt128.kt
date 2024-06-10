package io.github.aeckar.kanum

/**
 * The result of operations performed on instances of this class may be stored within the same instance.
 *
 * This class is useful for performing intermediate operations while keeping integer allocations to a minimum.
 * Instances of this class are cumulative for some operations,
 * which is to say that the result of the operation is stored in the same instance.
 *
 * There is no guarantee that the integrity of the state of a cumulative integer
 * will be maintained after it is passed to an operation.
 *
 * The following must always hold true:
 * - Cumulative integers must never be passed as more than one parameter to an operation
 * - Operations must be able to handle both singular and cumulative arguments
 * - The result of an operation may or may not be cumulative,
 * regardless of the type of the caller, unless otherwise specified
 * - If an operation is not [cumulative][Cumulative],the integrity of the state of the caller, if cumulative, is unknown
 *
 * To modify the properties of this class directly, call [valueOf].
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