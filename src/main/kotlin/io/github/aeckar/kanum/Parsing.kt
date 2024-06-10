package io.github.aeckar.kanum

internal const val DIGITS = "0123456789"

internal inline fun String.countUntil(predicate: (Char) -> Boolean): Int {
    if (this.isEmpty()) {
        return 0
    }
    for ((index, char) in this.withIndex()) {
        if (predicate(char)) {
            return index + 1
        }
    }
    return length
}

internal inline fun String.countTrailingUntil(predicate: (Char) -> Boolean): Int {
    if (this.isEmpty()) {
        return 0
    }
    val index = lastIndex
    do {
        if (predicate(this[index])) {
            return lastIndex - index
        }
    } while (index != 0)
    return length
}