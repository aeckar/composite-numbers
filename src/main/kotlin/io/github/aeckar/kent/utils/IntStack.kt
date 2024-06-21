package io.github.aeckar.kent.utils

// TODO remove from this repo. This file belongs in io.github.aeckar.karp (Kombinator)

/**
 * Returns an immutable [integer stack][IntStack] containing the single value.
 */
@Suppress("unused")
fun IntStack(single: Int): IntStack = SingletonIntStack(single)

/**
 * A stack of integers.
 */
sealed class IntStack {
    /**
     * The amount of integers that have been appended to this stack.
     */
    abstract val size: Int

    /**
     * Returns true if [size] is 0.
     */
    abstract fun isEmpty(): Boolean

    /**
     * Returns true if [size] is not 0.
     */
    abstract fun isNotEmpty(): Boolean

    /**
     * Returns the integer at the specified position.
     */
    abstract operator fun get(index: Int): Int

    final override fun toString() = buildString {
        append('[')
        repeat(size) {
            append(get(it))
            append(", ")
        }
        delete(length - 2, length)  // Remove trailing comma
        append(']')
    }
}

private class SingletonIntStack(val value: Int) : IntStack() {
    override val size get() = 1

    override fun isEmpty() = false
    override fun isNotEmpty() = true

    override fun get(index: Int): Int {
        if (index == 0) {
            return value
        }
        throw IndexOutOfBoundsException(index)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IntStack) {
            return false
        }
        return other.size == 1 && other[0] == value
    }

    override fun hashCode(): Int {
        return value
    }
}

/**
 * A mutable stack of integers.
 */
class MutableIntStack(initialSize: Int = DEFAULT_SIZE) : IntStack() {
    override var size = initialSize
    private var data = IntArray(size)

    /**
     * Appends the [code][Char.code] of this character to the stack.
     */
    operator fun plusAssign(c: Char) = plusAssign(c.code)

    /**
     * Appends the integer to the stack.
     */
    operator fun plusAssign(n: Int) {
        if (size == data.size) {
            val new = IntArray(size * 2)
            System.arraycopy(data, 0, new, 0, size)
            data = new
        }
        data[size] = n
        ++size
    }

    /**
     * Removes the last integer and returns its value.
     */
    @Suppress("unused")
    fun removeLast(): Int {
        return try {
            data[size - 1].also { --size }
        } catch (e: IndexOutOfBoundsException) {
            throw NoSuchElementException("Cannot remove last element from empty stack", e)
        }
    }

    override fun isEmpty() = data.isEmpty()
    override fun isNotEmpty() = data.isNotEmpty()

    override operator fun get(index: Int) = data[index]

    override fun equals(other: Any?): Boolean {
        if (other !is IntStack) {
            return false
        }
        return when (other.size) {
            0 -> size == 0
            1 -> data[0] == (if (other is SingletonIntStack) other.value else (other as MutableIntStack).data[0])
            else -> data.contentEquals((other as MutableIntStack).data)
        }
    }

    override fun hashCode(): Int {
        var result = 1
        data.forEach { result = 31 * result + it }
        return result
    }

    companion object {
        /**
         * The size of an instance when an initial size is not specified.
         */
        const val DEFAULT_SIZE = 8
    }
}