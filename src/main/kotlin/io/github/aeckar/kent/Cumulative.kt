package io.github.aeckar.kent

/**
 * When applied to a function, indicates that if the caller is mutable, the result will be stored in the same instance.
 *
 * Applies to operations that return only their caller or a value returned
 * by [valueOf][CompositeNumber.valueOf] (or some variant of it) from within the same scope.
 * Any operation that breaks this contract cannot be considered cumulative.
 * Overrides of cumulative functions must abide by the same contract.
 *
 * Mutability of composite numbers is necessary to keep allocations
 * to a minimum when performing intermediate operations.
 *
 * Instead of supplying separate functions for returning a unique value and modifying an existing value,
 * the business logic for both is shared within the same function.
 * To determine which will be returned, this responsibility is delegated to `valueOf`.
 *
 * There is no guarantee that the integrity of the state of a cumulative integer
 * will be maintained if it is passed as an argument to an operation.
 * It is for this reason that users are not allowed to interact with mutable composite numbers at all.
 * @see MutableInt128
 * @see MutableRational
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
internal annotation class Cumulative