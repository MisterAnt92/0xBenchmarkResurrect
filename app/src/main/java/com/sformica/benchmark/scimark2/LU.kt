package com.sformica.benchmark.scimark2

import kotlin.math.abs
import kotlin.math.min

/**
 * LU matrix factorization. (Based on TNT implementation.)
 * Decomposes a matrix A  into a triangular lower triangular
 * factor (L) and an upper triangular factor (U) such that
 * A = L*U.  By convnetion, the main diagonal of L consists
 * of 1's so that L and U can be stored compactly in
 * a NxN matrix.
 */
open class LU(A: Array<DoubleArray>) {

    private val LU_: Array<DoubleArray>
    private val pivot_: IntArray

    val lU: Array<DoubleArray>
        get() = new_copy(LU_)

    /**
     * Returns a *copy* of the pivot vector.
     *
     * @return the pivot vector used in obtaining the
     * LU factorization.  Subsequent solutions must
     * permute the right-hand side by this vector.
     */
    val pivot: IntArray
        get() = new_copy(pivot_)

    /**
     * Initalize LU factorization from matrix.
     *
     * A (in) the matrix to associate with this
     * factorization.
     */
    init {
        val M = A.size
        val N: Int = A[0].size

        //if ( LU_ == null || LU_.length != M || LU_[0].length != N)
        LU_ = Array(M) { DoubleArray(N) }
        insert_copy(LU_, A)

        //if (pivot_.length != M)
        pivot_ = IntArray(M)
        factor(LU_, pivot_)
    }

    /**
     * Solve a linear system, with pre-computed factorization.
     *
     * @param b (in) the right-hand side.
     * @return solution vector.
     */
    fun solve(b: DoubleArray): DoubleArray {
        val x = new_copy(b)
        solve(LU_, pivot_, x)
        return x
    }

    companion object {
        /**
         * Returns a *copy* of the compact LU factorization.
         * (useful mainly for debugging.)
         *
         * @return the compact LU factorization.  The U factor
         * is stored in the upper triangular portion, and the L
         * factor is stored in the lower triangular portion.
         * The main diagonal of L consists (by convention) of
         * ones, and is not explicitly stored.
         */

        @JvmStatic
        fun num_flops(N: Int): Double {
            // rougly 2/3*N^3
            return 2.0 * N.toDouble() * N.toDouble() * N.toDouble() / 3.0
        }

        protected fun new_copy(x: DoubleArray): DoubleArray {
            val N = x.size
            val T = DoubleArray(N)
            System.arraycopy(x, 0, T, 0, N)
            return T
        }

        protected fun new_copy(A: Array<DoubleArray>): Array<DoubleArray> {
            val M = A.size
            val N: Int = A[0].size
            val T = Array(M) { DoubleArray(N) }
            for (i in 0 until M) {
                val Ti = T[i]
                val Ai = A[i]
                System.arraycopy(Ai, 0, Ti, 0, N)
            }
            return T
        }

        fun new_copy(x: IntArray): IntArray {
            val N = x.size
            val T = IntArray(N)
            System.arraycopy(x, 0, T, 0, N)
            return T
        }

        protected fun insert_copy(B: Array<DoubleArray>, A: Array<DoubleArray>) {
            val M = A.size
            val N: Int = A[0].size
            val remainder = N and 3 // N mod 4;
            for (i in 0 until M) {
                val Bi = B[i]
                val Ai = A[i]
                System.arraycopy(Ai, 0, Bi, 0, remainder)
                var j = remainder
                while (j < N) {
                    Bi[j] = Ai[j]
                    Bi[j + 1] = Ai[j + 1]
                    Bi[j + 2] = Ai[j + 2]
                    Bi[j + 3] = Ai[j + 3]
                    j += 4
                }
            }
        }

        /**
         * LU factorization (in place).
         *
         * @param A (in/out) On input, the matrix to be factored.
         * On output, the compact LU factorization.
         * @return 0, if OK, nozero value, othewise.
         */
        @JvmStatic
        fun factor(A: Array<DoubleArray>, pivot: IntArray): Int {
            val N = A.size
            val M: Int = A[0].size
            val minMN = min(M, N)
            for (j in 0 until minMN) {
                // find pivot in column j and  test for singularity.
                var jp = j
                var t = abs(A[j][j])
                for (i in j + 1 until M) {
                    val ab = abs(A[i][j])
                    if (ab > t) {
                        jp = i
                        t = ab
                    }
                }
                pivot[j] = jp

                // jp now has the index of maximum element
                // of column j, below the diagonal
                if (A[jp][j] == 0.0) return 1 // factorization failed because of zero pivot
                if (jp != j) {
                    // swap rows j and jp
                    val tA = A[j]
                    A[j] = A[jp]
                    A[jp] = tA
                }
                if (j < M - 1) // compute elements j+1:M of jth column
                {
                    // note A(j,j), was A(jp,p) previously which was
                    // guarranteed not to be zero (Label #1)
                    //
                    val recp = 1.0 / A[j][j]
                    for (k in j + 1 until M) A[k][j] *= recp
                }
                if (j < minMN - 1) {
                    // rank-1 update to trailing submatrix:   E = E - x*y;
                    //
                    // E is the region A(j+1:M, j+1:N)
                    // x is the column vector A(j+1:M,j)
                    // y is row vector A(j,j+1:N)
                    for (ii in j + 1 until M) {
                        val Aii = A[ii]
                        val Aj = A[j]
                        val AiiJ = Aii[j]
                        for (jj in j + 1 until N) Aii[jj] -= AiiJ * Aj[jj]
                    }
                }
            }
            return 0
        }

        /**
         * Solve a linear system, using a prefactored matrix
         * in LU form.
         *
         * @param LU (in) the factored matrix in LU form.
         * @param b  (in/out) On input, the right-hand side.
         * On output, the solution vector.
         */
        @JvmStatic
        fun solve(LU: Array<DoubleArray>, pvt: IntArray, b: DoubleArray) {
            val M = LU.size
            val N: Int = LU[0].size
            var ii = 0
            for (i in 0 until M) {
                val ip = pvt[i]
                var sum = b[ip]
                b[ip] = b[i]
                for (j in ii until i) sum -= LU[i][j] * b[j]
                b[i] = sum
            }
            for (i in N - 1 downTo 0) {
                var sum = b[i]
                for (j in i + 1 until N) sum -= LU[i][j] * b[j]
                b[i] = sum / LU[i][i]
            }
        }
    }
}