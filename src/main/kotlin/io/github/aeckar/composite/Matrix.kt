package io.github.aeckar.composite

import io.github.aeckar.composite.Rational.Companion.ZERO
import io.github.aeckar.composite.Rational.Companion.ONE
import io.github.aeckar.composite.utils.Table
import io.github.aeckar.composite.utils.raiseUndefined

/**
 * A 2-dimensional matrix.
 *
 * Instances of this class are immutable.
 *
 * @param rows the entries in this matrix, sorted by row
 */
class Matrix {
    private val table: Table<Rational>

    /**
     * Lazily evaluated using [toString].
     */
    private var string: String? = null

    constructor(vararg rows: Array<Rational>) : this(defensiveCopy = true, *rows)

    private constructor(defensiveCopy: Boolean, vararg rows: Array<Rational>) {
        rows.forEach {

        }
        val columnSize = rows[0].size
        this.table = Array(rows.size) { Array(columnSize) { ZERO } }
    }

    // ------------------------------ entry access --------------------

    /**
     * Returns the total number of rows.
     */
    fun countRows() = table.countRows()

    /**
     * Returns the total number of columns.
     */
    fun countColumns() = table.countColumns()

    /**
     * Returns the row at the current index.
     *
     * Should be preferred over [table]`[rowIndex][columnIndex]`.
     */
    operator fun get(rowIndex: Int, columnIndex: Int) = table[rowIndex][columnIndex]

    // ------------------------------ transformations ------------------------------

    // TODO write more detailed kdoc comments explaining what the operations do

    /**
     * Returns the transpose of this matrix.
     *
     *
     */
    fun transpose(): Matrix {

    }

    /**
     * Returns the inverse of this matrix.
     */
    fun inverse(): Matrix {

    }

    /**
     * Returns this matrix as if it were in row echelon form.
     */
    fun rowEchelonForm(): Matrix {

    }

    // ------------------------------ scalar-returning operations --------------------

    /**
     * Returns the determinant of this matrix.
     */
    fun determinant(): Rational {

    }

    /**
     * Returns the trace of this matrix.
     *
     * @throws ArithmeticException this matrix is not a square matrix
     */
    fun trace(): Rational {
        val rowCount = countRows()
        if (countRows() != countColumns()) {
            raiseUndefined("Trace is only defined for square matrices")
        }
        var result = ZERO
        repeat(rowCount) { result += table[it][it] }
        return result
    }

    /**
     * Returns the rank of this matrix.
     */
    fun rank(): Int {
        val rowEchelon = rowEchelonForm().table
        var zeroRows = 0
        for (i in rowEchelon.lastIndex downTo 0) {
            if (rowEchelon[i].any { entry -> entry != ZERO }) {
                break
            }
            ++zeroRows
        }
        return rowEchelon.size - zeroRows
    }

    // ------------------------------ matrix-returning operations ------------------------------

    fun minor(): Matrix {

    }

    fun cofactor(): Matrix {

    }

    operator fun plus(other: Matrix): Matrix {
        if (countRows() != other.countRows() || countColumns() != other.countColumns()) {
            raiseUndefined("Sum is undefined for matrices of different dimensions")
        }

    }

    /**
     * Returns the result of this matrix when multiplied by -1.
     */
    operator fun unaryMinus(): Matrix {
        val table = Table<Rational>(countRows(), countColumns())
        this.table.byEntryIndexed { (rowIndex, columnIndex), row ->
            table[rowIndex, columnIndex] = -this.table[rowIndex, columnIndex]
        }
        return Matrix(table, defensiveCopy = false)
    }

    /**
     * Returns the result
     */
    operator fun minus(other: Matrix): Matrix {

    }

    operator fun times(other: Matrix): Matrix {

    }

    operator fun times(scalar: Rational): Matrix {

    }

    // ------------------------------ string conversion ------------------------------

    override fun toString(): String {
        string?.let { return it }
        val columnCount = countColumns()
        val entryStrings = Table<String>(countRows(), columnCount)
        val colMaxLengths = IntArray(columnCount)
        table.byEntryIndexed { (rowIndex, columnIndex), entry ->
            val entryString = entry.toString()
            entryStrings[rowIndex, columnIndex] = entryString
            colMaxLengths[columnIndex].coerceAtLeast(entryString.length)
        }
        val string = buildString {
            entryStrings.byRow { row ->
                append("| ")
                row.byColumnIndexed { columnIndex, entryString ->
                    repeat(colMaxLengths[columnIndex] - entryString.lastIndex) { append(' ') }  // Right-justify
                    append(entryString)
                }
                append(" |\n")
            }
            deleteCharAt(lastIndex)    // Remove trailing newline
        }
        this.string = string
        return string
    }

    companion object {
        /**
         * The 2x2 identity matrix.
         *
         * All entries are zero except for those on the main diagonal, which are all one.
         */
        val IDENTITY_2BY2 = Matrix(defensiveCopy = false,
            arrayOf(ONE,  ZERO),
            arrayOf(ZERO,  ONE),
        )

        /**
         * The 3x3 identity matrix.
         *
         * All entries are zero except for those on the main diagonal, which are all one.
         */
        val IDENTITY_3BY3 = Matrix(defensiveCopy = false,
            arrayOf(ONE,  ZERO, ZERO),
            arrayOf(ZERO,  ONE, ZERO),
            arrayOf(ZERO, ZERO,  ONE)
        )

        /**
         * Returns the identity matrix with the given number of rows and columns.
         */
        fun identity(sideLength: Int): Matrix {
            val table = Table<Rational> {}
            return Matrix(defensiveCopy = false, table)
        }

        fun entryArray(rowCount: Int, columnCount: Int) = Array(rowCount) { Array(columnCount) { ZERO } }
    }
}