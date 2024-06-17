package io.github.aeckar.kent.utils

internal class StringIndexIterator(private val source: String) {
    var cursor = 0
        private set

    /**
     * @throws StringIndexOutOfBoundsException the [cursor] is in a position outside the bounds of the underlying string
     */
    fun char() = source[cursor]

    operator fun inc() = this.also { ++cursor }
    operator fun dec() = this.also { --cursor }
    fun exists() = cursor >= 0 && cursor < source.length
}