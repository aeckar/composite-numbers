package io.github.aeckar.kent.utils

internal class StringIndexIterator(val string: String) {
    var position = 0
        private set

    /**
     * To ensure that this function does not throw an exception, ensure [exists] is true.
     *
     * Should be preferred when a check has already been made that proves
     * that the character at the current position exists.
     * @throws StringIndexOutOfBoundsException the [position] is in a position outside the bounds of the underlying string
     */
    fun char() = string[position]

    /**
     * Returns true if the character at the current position exists and satisfies the given predicate.
     *
     * Should be preferred when it is unknown whether the next character in the underlying string exists or not.
     */
    inline fun char(predicate: (Char) -> Boolean) = this.exists() && char().let(predicate)

    fun exists() = position >= 0 && position < string.length
    fun doesNotExist() = position < 0 || position >= string.length

    operator fun inc() = this.also { ++position }
    operator fun dec() = this.also { --position }
}