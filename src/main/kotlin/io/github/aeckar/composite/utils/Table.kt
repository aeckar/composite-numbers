package io.github.aeckar.composite.utils

/**
 * Thrown when a [table][Table] is initialized with invalid dimensions.
 */
internal class InvalidDimensionsException(
    rowCount: Int,
    columnCount: Int
) : Exception("${rowCount}x$columnCount table has 0 rows or 0 columns")

internal inline fun <E : Any> Table(
    rowCount: Int,
    columnCount: Int,
    defaultEntry: (Int, Int) -> E
): Table<E> {
    val table = Table<E>(rowCount, columnCount)
    repeat(rowCount) { rowIndex ->
        repeat(columnCount) { columnIndex -> table[rowIndex, columnIndex] = defaultEntry(rowIndex, columnIndex) }
    }
    return table
}

/**
 * A table of entries.
 *
 * This class is implemented as a 2-dimensional array of elements of type [E]`?`.
 * Tables cannot have 0 rows, 0 columns, nor have a non-rectangular shape.
 * If a table is initialized with an invalid number of rows or columns, an [InvalidDimensionsException] is thrown.
 *
 * Access to entries involves casting them to their non-nullable form.
 * If an entry is not initialized, that is to say it is null, a [NoSuchElementException] will be thrown upon access.
 *
 * Unlike other iterable classes, this class does not implement from [Iterable].
 * Additionally, iteration functions are prefixed with `by` instead of `for`.
 *
 * Instances are mutable and should be defensively copied when appropriate.
 */
@JvmInline
internal value class Table<E : Any>(private val backingArray: Array<Array<E?>>) {
    init {
        if (backingArray.isEmpty() || backingArray[0].isEmpty()) {
            val rowCount = backingArray.size
            val columnCount = if (backingArray.isNotEmpty()) backingArray[0].size else 0
            throw InvalidDimensionsException(rowCount, columnCount)
        }
    }

    @Suppress("UNCHECKED_CAST")
    constructor(
        rowCount: Int,
        columnCount: Int,
        defaultEntry: E? = null
    ) : this(Array<Array<Any?>>(rowCount) { Array<Any?>(columnCount) { defaultEntry } } as Array<Array<E?>>)

    /**
     * Will stop after the rightmost column before going down to the next row.
     * However, will not stop at last row.
     */
    class IndexIterator(table: Table<*>) {
        private val lastColumn: Int = table.countColumns() - 1
        var row = 0
            private set
        var column = 0
            private set
        
        operator fun inc() = this.also {
            if (column == lastColumn) {
                ++row
                column = 0
            } else {
                ++column
            }
        }
    }
    
    @JvmInline
    value class Row<E : Any>(private val entries: Array<E>) {
        inline fun byColumn(action: (E) -> Unit) = entries.forEach(action)

        inline fun byColumnIndexed(action: (Int, E) -> Unit) = entries.forEachIndexed(action)
    }

    // ------------------------------ access and modification ------------------------------

    operator fun get(rowIndex: Int, columnIndex: Int): E {
        return try {
            backingArray[rowIndex][columnIndex] as E
        } catch (e: NullPointerException) {
            throw NoSuchElementException("Attempted access of uninitialized table element", e)
        } catch (_: ArrayIndexOutOfBoundsException) {
            raiseInvalidIndex(rowIndex, columnIndex)
        }
    }

    operator fun set(index: IndexIterator, entry: E) {
        this[index.row, index.column] = entry
    }

    operator fun set(rowIndex: Int, columnIndex: Int, entry: E) {
        try {
            backingArray[rowIndex][columnIndex] = entry
        } catch (e: ArrayIndexOutOfBoundsException) {
            raiseInvalidIndex(rowIndex, columnIndex)
        }
    }

    /**
     * @throws NoSuchElementException always
     */
    private fun raiseInvalidIndex(rowIndex: Int, columnIndex: Int): Nothing {
        throw NoSuchElementException(
            "Index [$rowIndex, $columnIndex] lies outside the bounds of the table " +
            "(rows = ${countRows()}, columns = ${countColumns()})"
        )
    }

    // ------------------------------ iteration ------------------------------

    fun countRows() = backingArray.size
    fun countColumns() = backingArray[0].size

    @Suppress("UNCHECKED_CAST")
    inline fun byRow(action: Row<E>.() -> Unit) = backingArray.forEach { action(Row(it as Array<E>)) }

    inline fun byEntryIndexed(action: (Int, Int, E) -> Unit) {
        var index = indexIterator()
        while (index.row < backingArray.size) with (index) {
            action(row, column, backingArray[row][column] as E)
            ++index
        }
    }

    // ------------------------------ miscellaneous ------------------------------

    /**
     * Returns the 2-dimensional backing array of this table.
     */
    @Suppress("UNCHECKED_CAST")
    fun array() = backingArray as Array<Array<E>>

    fun indexIterator() = IndexIterator(this)
}