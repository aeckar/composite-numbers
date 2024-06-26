package io.github.aeckar.kent

/**
 * Indicates that no constructors of this class may be public.
 *
 * Enables open classes to disallow inheritance from outside the API.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
internal annotation class PrivateInheritance
