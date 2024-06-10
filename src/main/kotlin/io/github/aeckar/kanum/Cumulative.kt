package io.github.aeckar.kanum

/**
 * If applied to a function, indicates that if the caller is mutable, the result will be stored in the same instance.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
internal annotation class Cumulative