package io.github.aeckar.kanum

infix fun Char.from(acceptable: String): Char {
    if (this !in acceptable) {
        throw IllegalCharException(this, acceptable)
    }
    return this
}

// ------------------------------ CharIterator extensions ------------------------------

/**
 * @return next non-[whitespace][isWhitespace] character.
 * @throws NoSuchElementException the iterator is exhausted before a visible character is found
 */
fun CharIterator.nextVisible(): Char {
    var next: Char
    do {
        if (!hasNext()) {
            throw NoSuchElementException("Visible character expected, but not found")
        }
        next = nextChar()
    } while (next.isWhitespace())
    return next
}

data class Token(val substring: String, val sentinel: Int = -1) {
    fun isEmpty() = substring.isEmpty()

    override fun toString() = substring // == reduceToString {}
}

/**
 * Thrown when an illegal token is encountered when [CharIterator.nextToken] is called.
 */
class IllegalCharException internal constructor(
    c: Char,
    acceptable: String
) : Exception("Character '$c' does not conform to [$acceptable]")

const val WHITESPACE = " \t\n\r"

// In io.github.aeckar.kombinator, this extension will use RangeVector parameters
/**
 * Parses a token at the current position.
 *
 * Whitespace is **not** ignored.
 *
 * The sentinel is consumed and not present in the returned string.
 * If the sentinel is at the current position, an empty string is returned.
 * Parsing stops upon consumption of a [whitespace][isWhitespace] character.
 * If the end of the iterator is reached, parsing stops with sentinel code [EOF].
 * @return the token whose characters are [acceptable]. Parsing stops once a [sentinel][sentinels] is reached
 * @throws IllegalCharException an unacceptable character is encountered
 * @throws NoSuchElementException the supplied iterator is exhausted
 */
fun CharIterator.nextToken(acceptable: String, sentinels: String, optional: Boolean = false): Token {
    assert(acceptable.none { it.isWhitespace() })
    var sentinel = EOF
    val substring = buildString {
        while (hasNext()) {
           val c = nextChar()
            if (c in sentinels) {
                sentinel = c.code
                return@buildString
            }
            if (c !in acceptable) {
                throw IllegalCharException(c, acceptable)
            }
            append(c)
        }
    }
    if (!optional && substring.isEmpty()) {
        throw NoSuchElementException("Token expected, but not found")
    }
    return Token(substring, sentinel)
}

const val EOF = -1

fun main() {
    println(Rational(Int.MAX_VALUE, 3) + Rational(3,Int.MAX_VALUE))
}