package io.github.aeckar.kent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class MatrixTest {
    @Nested
    inner class Transformation {
        // TODO
    }

    @Nested
    inner class ScalarReturningOperations {
        @Test
        fun determinant() {
            val matrixA = Matrix[3,3](
                1, 5,  7,
                3, 1,  0,
                8, 9, 10
            )
            assertEquals(Rational(-7), matrixA.determinant())
        }
    }

    @Nested
    inner class MatrixReturningOperations {
        // TODO
    }
}