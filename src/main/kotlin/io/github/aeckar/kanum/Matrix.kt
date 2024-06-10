package io.github.aeckar.kanum

// TODO
///**
// * A 2-dimensional matrix.
// */
//class Matrix {
//    /**
//     * The entries in this matrix, by rows then by column.
//     */
//    val entries: Array<Array<Rational>>
//
//    /**
//     * The total number of rows.
//     */
//    val rows inline get() = entries.size
//
//    /**
//     * The total number of columns.
//     */
//    val columns inline get() = entries[0].size
//
//    private val determinant: Matrix? = null
//
//    fun trace(): Rational {
//        if (rows != columns) {
//            throw ArithmeticException("Trace is only defined for square matrices")
//        }
//        var result = Rational.ZERO
//        repeat(rows) { result += entries[it][it] }
//        return result
//    }
//
//    fun transpose(): Matrix {
//        val totalRows =
//    }
//
//    fun determinant(): Matrix {
//
//    }
//
//    fun inverse(): Matrix {
//
//    }
//
//    fun rank(): Int {
//
//    }
//
//    fun toRowEchelon(): Matrix {
//
//    }
//
//    operator fun plus(other: Matrix): Matrix {
//
//    }
//
//    operator fun minus(other: Matrix): Matrix {
//
//    }
//
//    operator fun times(other: Matrix): Matrix {
//
//    }
//
//    operator fun times(scalar: Rational): Matrix {
//
//    }
//}