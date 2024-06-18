package io.github.aeckar.kent

import io.github.aeckar.kent.Rational.Companion.ZERO
import io.github.aeckar.kent.Rational.Companion.ONE
import io.github.aeckar.kent.utils.Table

/**
 * A 2-dimensional matrix.
 *
 * Instances of this class are immutable.
 *
 * Results of computationally expensive operations are not cached,
 * and should be stored in a variable if used more than once.
 * The exceptions to this are [ref], [toString], and partly [rref].
 */
class Matrix {
    private val table: Table<Rational>
    private var lazyString: String? = null
    private var lazyRowEchelonForm: Matrix? = null

    constructor(vararg rows: Array<Rational>) : this(*rows, isForeign = true)

    private constructor(entries: Table<Rational>) : this(*entries.array(), isForeign = false)

    @Suppress("UNCHECKED_CAST")
    private constructor(vararg rows: Array<Rational>, isForeign: Boolean) {
        fun raiseEmpty(): Nothing = raiseUndefined("Matrix cannot be empty")

        val tableRows = if (isForeign) {
            if (rows.isEmpty()) {
                raiseEmpty()
            }
            val initialRowSize = rows[0].size
            if (initialRowSize == 0) {
                raiseEmpty()
            }
            for (i in 1..rows.lastIndex) if (rows[i].size != initialRowSize) {
                raiseUndefined("Matrix must be rectangular")
            }
            (rows.clone() as Array<Array<Rational>>).apply { for (i in indices) this[i] = this[i].clone() }
        } else {
            rows
        }
        this.table = Table(tableRows as Array<Array<Rational?>>)
    }

    private fun ensureSquare(operation: String) {
        if (countRows() != countColumns()) {
            raiseUndefined("$operation is only defined for square matrices")
        }
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
     * Returns the entry at the current index.
     * @throws NoSuchElementException the specified index lies outside the bounds of the matrix
     */
    operator fun get(rowIndex: Int, columnIndex: Int) = table[rowIndex, columnIndex]

    // ------------------------------ transformations ------------------------------

    /**
     * Returns the transpose of this matrix.
     *
     * The transpose is the matrix where the rows and columns are swapped.
     */
    fun transpose(): Matrix {
        val table = Table(countColumns(), countRows()) { rowIndex, columnIndex -> table[columnIndex, rowIndex] }
        return Matrix(table)
    }

    /**
     * Returns the inverse of this matrix.
     *
     * The inverse is the matrix, when multiplied by this matrix, results in an identity matrix.
     * @throws ArithmeticException this matrix is not a square matrix
     */
    fun inverse(): Matrix {
        ensureSquare("Inverse")

        TODO("Not implemented yet")
    }

    /**
     * Returns this matrix as if it were in row echelon form (REF).
     *
     * The REF is the matrix, after applying elementary row operations, where:
     * - All entries below the main diagonal are zero
     * - All entries on the main diagonal are one
     * - All zero rows are on the bottom
     *
     * Furthermore, we can describe the elementary row operations as follows:
     * - Multiplication by a non-zero scalar
     * - Addition by another row
     * - Swap with another row
     * @see rref
     */
    fun ref(): Matrix {
        lazyRowEchelonForm?.let { return it }

        TODO("Not implemented yet")
    }

    /**
     * Returns this matrix as if it were in reduced row echelon form (RREF).
     *
     * The RREF is the matrix, after applying elementary row operations, where
     * the entries are in row echelon form and every entry above the main diagonal is zero.
     *
     * For augmented matrices in RREF, the rightmost column of the matrix will always contain
     * the solutions to the linear system which the matrix represents.
     *
     * For an explanation of elementary row operations, see [ref].
     */
    fun rref(): Matrix {
        val rowEchelonForm = ref()

        TODO("Not implemented yet")
    }

    // ------------------------------ arithmetic --------------------

    /**
     * Returns the determinant of this matrix.
     *
     * The determinant can be defined recursively as
     * @throws ArithmeticException this matrix is not square
     */
    fun determinant(): Rational {
        ensureSquare(operation = "Determinant")
        return determinant(0, 0, sideLength = countRows()).immutable()
    }

    /**
     * Assumes this matrix is square.
     */
    private fun determinant(rowIndex: Int, columnIndex: Int, sideLength: Int): Rational {
        if (sideLength == 2) {
            /*
                    | a b |
                A = | c d |
             */
            val ad = table[rowIndex, columnIndex] * table[rowIndex + 1, columnIndex + 1]
            val bc = table[rowIndex, columnIndex + 1] * table[rowIndex + 1, columnIndex]
            return ad - bc
        }
        var sign = 1
        val value = MutableRational(ZERO)
        repeat(sideLength) {
            val cofactorTarget = table[rowIndex, it % sideLength]
            value +/* = */ (cofactorTarget * determinant(rowIndex + 1, it + 1, sideLength - 1) * sign.toRational())
            sign = -sign
        }
        return value
    }

    /**
     * Returns the trace of this matrix.
     *
     * The trace is the sum of all entries on the main diagonal.
     * @throws ArithmeticException this matrix is not square
     */
    fun trace(): Rational {
        ensureSquare(operation = "Trace")
        var result = ZERO
        repeat(countRows()) { result += table[it, it] }
        return result
    }

    /**
     * Returns the rank of this matrix.
     *
     * The rank, informally, is the number of zero rows when a matrix is in [row echelon form][ref].
     */
    fun rank(): Int {
        val rowEchelon = ref().table
        var zeroRows = 0
        rowEchelon.byRow {
            byColumn { entry ->
                if (entry != ZERO) {
                    ++zeroRows
                    return@byColumn
                }
            }
        }
        return rowEchelon.countRows() - zeroRows
    }

    /**
     * Returns the minor, M, at the given entry.
     *
     * Since this implementation of the minor is by entry, the matrix must be square.
     * @throws ArithmeticException this matrix is not square
     */
    fun minor(rowIndex: Int, columnIndex: Int): Matrix {
        ensureSquare(operation = "Minor")
        return getMinor(rowIndex, columnIndex)
    }

    /**
     * Returns the cofactor, C, at the given entry.
     *
     * For the specific properties that are required in order to return a cofactor, see [minor].
     * @throws ArithmeticException this matrix is not square
     */
    fun cofactor(rowIndex: Int, columnIndex: Int): Matrix {
        ensureSquare(operation = "Cofactor")
        val minor = getMinor(rowIndex, columnIndex)
        return if ((rowIndex % 2 == 0) xor (columnIndex % 2 == 0)) -minor else minor
    }

    /**
     * Assumes this is a square matrix.
     */
    private fun getMinor(rowIndex: Int, columnIndex: Int): Matrix {
        val sideLength = countRows() - 1
        val table = Table<Rational>(sideLength, sideLength)
        var index = table.indexIterator()
        this.table.byEntryIndexed { row, column, entry ->
            if (row != rowIndex && column != columnIndex) {
                table[index] = entry
                ++index
            }
        }
        return Matrix(table)
    }

    /**
     * Returns the result of this matrix when multiplied by -1.
     */
    operator fun unaryMinus() = this * Rational.NEGATIVE_ONE

    /**
     * Returns the result of this matrix added to the other.
     *
     * The addition is done by-entry, with the resultant matrix having the same dimensions as the arguments.
     * @throws ArithmeticException the two matrices are of different sizes
     */
    operator fun plus(other: Matrix) = add(other, Rational::plus)

    /**
     * Returns the result of this matrix subtracted by the other.
     *
     * The subtraction is done by-entry, with the resultant matrix having the same dimensions as the arguments.
     * @throws ArithmeticException the two matrices are of different sizes
     */
    operator fun minus(other: Matrix) = add(other, Rational::minus)

    private inline fun add(other: Matrix, entryOperator: (Rational, Rational) -> Rational): Matrix {
        val rowCount = countRows()
        val columnCount = countColumns()
        if (rowCount != other.countRows() || columnCount != other.countColumns()) {
            raiseUndefined("Sum is undefined for matrices of different dimensions")
        }
        val table = Table(rowCount, columnCount) { rowIndex, columnIndex ->
            entryOperator(table[rowIndex, columnIndex], other.table[rowIndex, columnIndex])
        }
        return Matrix(table)
    }

    /**
     * Returns the result of this matrix multiplied by the other.
     *
     * The multiplication is done as the dot product of rows of this matrix by the columns of the other.
     * In other words, each entry becomes the sum of it multiplied by
     * each entry in each column of the other matrix, per column.
     * This is done for each row in this matrix.
     *
     * This operation is not commutative.
     */
    operator fun times(other: Matrix): Matrix {
        val columnCount = countColumns()
        val otherRowCount = other.countRows()
        if (columnCount != otherRowCount) {
            raiseUndefined(
                "Product is undefined when the # of columns of the left argument " +
                "is not equal to the # of rows of the right argument"
            )
        }
        val rowCount = countRows()
        val otherColumnCount = other.countColumns()
        val table = Table<Rational>(rowCount, otherColumnCount)
        var index = table.indexIterator()
        val sum = MutableRational(ZERO)
        this.table.byRow {
            repeat(otherColumnCount) { otherColumnIndex ->
                byColumnIndexed { termNumber, entry -> sum +/* = */ (entry * other[termNumber, otherColumnIndex]) }
                table[index] = sum.immutable()
                sum/* = */.valueOf(ZERO)
                ++index
            }
        }
        return Matrix(table)
    }

    /**
     * Returns the result of this matrix multiplied by a scalar.
     *
     * The multiplication is done by-entry, with each entry being multiplied by the scalar value.
     */
    operator fun times(scalar: Rational): Matrix {
        val table = Table(countRows(), countColumns()) { rowIndex, columnIndex ->
            table[rowIndex, columnIndex] * scalar
        }
        return Matrix(table)
    }

    // ------------------------------ eigenvalues ------------------------------

    // TODO implement eigenvalue, eigenvector functions

    // ------------------------------ string conversion ------------------------------

    override fun toString(): String {
        lazyString?.let { return it }
        val columnCount = countColumns()
        val entryStrings = Table<String>(countRows(), columnCount)
        val colMaxLengths = IntArray(columnCount)
        table.byEntryIndexed { rowIndex, columnIndex, entry ->
            val entryString = entry.toString()
            entryStrings[rowIndex, columnIndex] = entryString
            colMaxLengths[columnIndex].coerceAtLeast(entryString.length)
        }
        val string = buildString {
            entryStrings.byRow {
                append("| ")
                byColumnIndexed { columnIndex, entryString ->
                    repeat(colMaxLengths[columnIndex] - entryString.lastIndex) { append(' ') }  // Right-justify
                    append(entryString)
                }
                append(" |\n")
            }
            deleteCharAt(lastIndex)    // Remove trailing newline
        }
        this.lazyString = string
        return string
    }

    companion object {
        /**
         * The 2x2 identity matrix.
         *
         * All entries are zero except for those on the main diagonal, which are all one.
         */
        val I2 = identity(2)

        /**
         * The 3x3 identity matrix.
         *
         * All entries are zero except for those on the main diagonal, which are all one.
         */
        val I3 = identity(3)

        /**
         * Returns the identity matrix with the given number of rows and columns.
         * @see I2
         * @see I3
         */
        fun identity(sideLength: Int): Matrix {
            val table = Table(sideLength, sideLength, defaultEntry = ZERO)
            repeat(sideLength) { table[it, it] = ONE }
            return Matrix(table)
        }
    }
}