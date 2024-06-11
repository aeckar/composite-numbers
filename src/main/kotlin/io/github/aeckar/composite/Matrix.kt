package io.github.aeckar.composite

import io.github.aeckar.composite.utils.raiseUndefined

/**
 * A 2-dimensional matrix.
 *
 * Instances of this class are immutable.
 */
class Matrix {
    /**
     * The entries in this matrix, sorted by row.
     *
     * To get the total number of rows, use [countRows].
     */
    val rows: List<List<Rational>>

    /**
     * The entries in this matrix, sorted by column.
     *
     * The value of this property is lazily evaluated.
     * To get the total number of columns without evaluating this property, use [countColumns].
     */
    val columns get() = (transpose ?: transpose()).rows

    /**
     * Lazily evaluated using [determinant].
     */
    private var determinant: Matrix? = null

    /**
     * Lazily evaluated using [transpose].
     */
    private var transpose: Matrix? = null

    /**
     * Lazily evaluated using [toString].
     */
    private var string: String? = null

    // ------------------------------ entry access --------------------

    /**
     * Returns the total number of [rows].
     *
     * Should be preferred over using `rows.size`.
     */
    fun countRows() = rows.size

    /**
     * Returns the total number of [columns].
     *
     * Should be preferred over using `columns.size`.
     */
    fun countColumns() = rows[0].size

    /**
     * Returns the row at the current index.
     *
     * Should be preferred over [rows]`[rowIndex]`.
     */
    operator fun get(rowIndex: Int) = rows[rowIndex]

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
        var result = Rational.ZERO
        repeat(rowCount) { result += rows[it][it] }
        return result
    }

    /**
     * Returns the rank of this matrix.
     */
    fun rank(): Int {
        val rowEchelon = rowEchelonForm()
        var zeroRows = 0
        rowEchelon.rows.asReversed().forEach {
            if (it.any { entry -> entry != Rational.ZERO }) {
                return@forEach
            }
            ++zeroRows
        }
        return rowEchelon.rows.size - zeroRows
    }

    // ------------------------------ matrix-returning operations ------------------------------

    fun minor(): Matrix {

    }

    fun cofactor(): Matrix {

    }

    operator fun plus(other: Matrix): Matrix {

    }

    operator fun minus(other: Matrix): Matrix {

    }

    operator fun times(other: Matrix): Matrix {

    }

    operator fun times(scalar: Rational): Matrix {

    }

    // ------------------------------ string conversion ------------------------------

    override fun toString(): String {
        string?.let { return it }
        val entryStrings: Array<Array<String>> = Array(countRows()) { Array(countColumns()) { "" } }
        val string = buildString {

        }
        this.string = string
        return string
    }
}