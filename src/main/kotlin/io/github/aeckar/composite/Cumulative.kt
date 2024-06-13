package io.github.aeckar.composite

/**
 * When applied to a function, indicates that if the caller is mutable, the result will be stored in the same instance.
 *
 * Applies to operations that return only their caller or a value returned
 * by [valueOf][CompositeNumber.valueOf], or some variant of it.
 * Any operation that breaks this contract is not considered cumulative.
 *
 * Mutability of composite numbers is necessary to keep allocations
 * to a minimum when performing intermediate operations.
 *
 * Instead of supplying separate functions for both, the business logic of operations
 * returning a unique value and those performed in-place are shared within the same function.
 * To determine what kind of result should be returned, the returned instance is determined by
 * `valueOf`, which:
 * - For immutable instances, returns a new immutable instance with the desired state
 * - For mutable instances, the same instance with a modified state
 *
 * There is no guarantee that the integrity of the state of a cumulative integer
 * will be maintained if it is passed as an argument to an operation.
 *
 * The following always hold true:
 * - If the caller is `immutable`, the result is immutable
 * - If the caller is `mutable`, the result may be immutable or refer to the same instance
 * - If a mutable caller invokes a [cumulative][Cumulative] operation, the result is the same instance
 * - Mutable integers must never be passed as more than one parameter to an operation
 *
 * @see MutableInt128
 * @see MutableRational
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
internal annotation class Cumulative