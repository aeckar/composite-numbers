package io.github.aeckar.kent

import io.github.aeckar.kent.Rational.Companion.ZERO
import io.github.aeckar.kent.Rational.Companion.ONE
import io.github.aeckar.kent.utils.Table
import java.util.*

/**
 * A 2-dimensional matrix.
 *
 * To create a new instance, use the following syntax:
 * ```kotlin
 * val myMatrix = Matrix[2,3](
 *     2, 6, 9
 *     4, 0, 1
 * )
 * ```
 *
 * Designed specifically for computation with minimal information loss.
 * Performance-sensitive applications should use a vector arithmetic library instead.
 *
 * Instances of this class are immutable.
 *
 * Results of computationally expensive operations are not cached,
 * and should be stored in a variable if used more than once.
 * The exceptions to this are [ref], [toString], and partly [rref].
 */
class Matrix private constructor(private val table: Table<Rational>) {
    private var lazyString: String? = null
    private var lazyRowEchelonForm: Matrix? = null

    /*
            val tableRows = if (isForeign) {
            if (rows.isEmpty()) {
                raiseUndefined("Matrix cannot be empty")
            }
            val initialRowSize = rows[0].size
            if (initialRowSize == 0) {
                raiseUndefined("Matrix cannot be empty")
            }
            if (convertEntries) {
                rows.forEachIndexed { rowIndex, row ->
                    if (row.size != initialRowSize) {
                        raiseUndefined("Matrix must be rectangular")
                    }
                    repeat(row.size) {
                        val entry = rows[rowIndex][it] as Number
                        rows[rowIndex][it] = when (entry) {
                            is Byte, is Short, is Int, is Long -> entry.toLong() over 1
                            is Int128 -> entry.toRational()
                            is Rational -> entry
                            else -> throw IllegalArgumentException("")
                        }
                    }
                }
            } else {
                if (rows.any { it.size != initialRowSize }) {
                    raiseUndefined("Matrix must be rectangular")
                }
            }
            (rows.clone() as Array<Array<Rational>>).apply { for (i in indices) this[i] = this[i].clone() }
        } else {
            rows
        }
        this.table = Table(tableRows as Array<Array<Rational?>>)
     */
    /**
     * Used to create a matrix using the dimensions this instance was given.
     */
    class Prototype(private val rowCount: Int, private val columnCount: Int) {
        /**
         * Returns a new matrix with the given entries.
         */
        operator fun invoke(vararg entries: Int): Matrix {
            ensureValidSize(entries.size)
            val entry = entries.iterator()
            val table = Table(rowCount, columnCount) { _, _ -> entry.nextInt().toRational() }
            return Matrix(table)
        }

        /**
         * Returns a new matrix with the given entries.
         */
        operator fun invoke(vararg entries: Rational): Matrix {
            ensureValidSize(entries.size)
            val entry = entries.iterator()
            val table = Table(rowCount, columnCount) { _, _ -> entry.next() }
            return Matrix(table)
        }

        private fun ensureValidSize(size: Int) {
            require (size != 0) { "Matrix cannot be empty" }
            require (size / rowCount == columnCount && size % columnCount == 0) {
                "Entries do not conform to matrix dimensions"
            }
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
        return determinant(0, -1).immutable()
    }

    /**
     * Assumes this matrix is square.
     */
    private fun determinant(rowPivot: Int, columnPivot: Int): Rational {
        val sideLength = countRows() - rowPivot
        if (sideLength == 2) {
            /*
                    | a b |
                A = | c d |
             */
            val column = if (columnPivot == 0) 1 else 0
            val nextColumn = column + if (columnPivot == column + 1) 2 else 1
            val ad = table[rowPivot, column] * table[rowPivot + 1, nextColumn]
            val bc = table[rowPivot, nextColumn] * table[rowPivot + 1, column]
            return ad - bc
        }
        var negateTerm = false
        var skippedColumn = false
        val result = MutableRational(ZERO)
        repeat(sideLength) {
            if (it == columnPivot) {
                skippedColumn = true
            }
            val column = if (skippedColumn) it + 1 else it
            val term = table[rowPivot, column] * determinant(rowPivot + 1, column)
            if (negateTerm) {
                result -/* = */ term
            } else {
                result +/* = */ term
            }
            negateTerm = !negateTerm
        }
        return result
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
        val entries = Table<String>(countRows(), columnCount)
        val maxLengths = IntArray(columnCount)
        table.byEntryIndexed { rowIndex, columnIndex, value ->
            val entry = value.toString()
            entries[rowIndex, columnIndex] = entry
            maxLengths[columnIndex] = maxLengths[columnIndex].coerceAtLeast(entry.length)
        }
        val string = buildString {
            entries.byRow {
                append("|")
                byColumnIndexed { columnIndex, entryString ->
                    repeat(maxLengths[columnIndex] - entryString.lastIndex) { append(' ') }  // Right-justify
                    append(entryString)
                }
                append(" |\n")
            }
            deleteCharAt(lastIndex)    // Remove trailing newline
        }
        this.lazyString = string
        return string
    }

    // ------------------------------ miscellaneous --------------------

    private fun ensureSquare(operation: String) {
        if (countRows() != countColumns()) {
            raiseUndefined("$operation is only defined for square matrices")
        }
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

        /**
         * Returns a new [matrix prototype][Prototype] with the given dimensions.
         */
        operator fun get(rowCount: Int, columnCount: Int) = Prototype(rowCount, columnCount)
    }
}