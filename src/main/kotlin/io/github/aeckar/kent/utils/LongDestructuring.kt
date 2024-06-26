package io.github.aeckar.kent.utils

/**
 * The most significant 32 bits of this value.
 */
internal val Long.high inline get() = (this ushr 32).toInt()

/**
 * The least significant 32 bits of this value.
 */
internal val Long.low inline get() = this.toInt()

/**
 * The [upper half][high] of this value.
 */
internal operator fun Long.component1() = high

/**
 * The [lower half][low] of this value.
 */
internal operator fun Long.component2() = low