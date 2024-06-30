@file:JvmName("Random")
package io.github.aeckar.kent

import kotlin.random.Random

/**
 * Returns a random 128-bit integer.
 */
public fun Random.nextInt128(): Int128 {
    val mag = nextInt(1, 5)
    return Int128(
        nextInt(),
        if (mag > 1) 0 else nextInt(),
        if (mag > 2) 0 else nextInt(),
        if (mag == 4) 0 else nextInt()
    )
}

/**
 * Returns a random rational number.
 */
public fun Random.nextRational(): Rational {
    TODO("Not yet implemented")
}