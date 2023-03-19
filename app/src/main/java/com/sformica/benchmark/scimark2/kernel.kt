package com.sformica.benchmark.scimark2

import com.sformica.benchmark.scimark2.LU.Companion.factor
import com.sformica.benchmark.scimark2.LU.Companion.solve
import com.sformica.benchmark.scimark2.MonteCarlo.integrate
import com.sformica.benchmark.scimark2.SOR.execute
import com.sformica.benchmark.scimark2.SparseCompRow.matmult
import kotlin.math.abs

object kernel {
    // each measurement returns approx Mflops
    @JvmStatic
    fun measureFFT(N: Int, mintime: Double, R: Random): Double {
        // initialize FFT data as complex (N real/img pairs)
        val x = RandomVector(2 * N, R)
        val oldx = NewVectorCopy(x)
        var cycles: Long = 1
        val Q = Stopwatch()
        while (true) {
            Q.start()
            for (i in 0 until cycles) {
                FFT.transform(x) // forward transform
                FFT.inverse(x) // backward transform
            }
            Q.stop()
            if (Q.read() >= mintime) break
            cycles *= 2
        }
        // approx Mflops
        val EPS = 1.0e-10
        return if (FFT.test(x) / N > EPS) 0.0 else FFT.num_flops(N) * cycles / Q.read() * 1.0e-6
    }

    @JvmStatic
    fun measureSOR(N: Int, min_time: Double, R: Random): Double {
        val G = RandomMatrix(N, N, R)
        val Q = Stopwatch()
        var cycles = 1
        while (true) {
            Q.start()
            execute(1.25, G, cycles)
            Q.stop()
            if (Q.read() >= min_time) break
            cycles *= 2
        }
        // approx Mflops
        return SOR.num_flops(N, N, cycles) / Q.read() * 1.0e-6
    }

    @JvmStatic
    fun measureMonteCarlo(min_time: Double, R: Random?): Double {
        val Q = Stopwatch()
        var cycles = 1
        while (true) {
            Q.start()
            integrate(cycles)
            Q.stop()
            if (Q.read() >= min_time) break
            cycles *= 2
        }
        // approx Mflops
        return MonteCarlo.num_flops(cycles) / Q.read() * 1.0e-6
    }

    @JvmStatic
    fun measureSparseMatmult(
        N: Int, nz: Int,
        min_time: Double, R: Random
    ): Double {
        // initialize vector multipliers and storage for result
        // y = A*y;
        val x = RandomVector(N, R)
        val y = DoubleArray(N)

        // initialize square sparse matrix
        //
        // for this test, we create a sparse matrix wit M/nz nonzeros
        // per row, with spaced-out evenly between the begining of the
        // row to the main diagonal.  Thus, the resulting pattern looks
        // like
        //             +-----------------+
        //             +*                +
        //             +***              +
        //             +* * *            +
        //             +** *  *          +
        //             +**  *   *        +
        //             +* *   *   *      +
        //             +*  *   *    *    +
        //             +*   *    *    *  + 
        //             +-----------------+
        //
        // (as best reproducible with integer artihmetic)
        // Note that the first nr rows will have elements past
        // the diagonal.
        val nr = nz / N // average number of nonzeros per row
        val anz = nr * N // _actual_ number of nonzeros
        val `val` = RandomVector(anz, R)
        val col = IntArray(anz)
        val row = IntArray(N + 1)
        row[0] = 0
        for (r in 0 until N) {
            // initialize elements for row r
            val rowr = row[r]
            row[r + 1] = rowr + nr
            var step = r / nr
            if (step < 1) step = 1 // take at least unit steps
            for (i in 0 until nr) col[rowr + i] = i * step
        }
        val Q = Stopwatch()
        var cycles = 1
        while (true) {
            Q.start()
            matmult(y, `val`, row, col, x, cycles)
            Q.stop()
            if (Q.read() >= min_time) break
            cycles *= 2
        }
        // approx Mflops
        return SparseCompRow.num_flops(N, nz, cycles) / Q.read() * 1.0e-6
    }

    @JvmStatic
    fun measureLU(N: Int, min_time: Double, R: Random): Double {
        // compute approx Mlfops, or O if LU yields large errors
        val A = RandomMatrix(N, N, R)
        val lu = Array(N) { DoubleArray(N) }
        val pivot = IntArray(N)
        val Q = Stopwatch()
        var cycles = 1
        while (true) {
            Q.start()
            for (i in 0 until cycles) {
                CopyMatrix(lu, A)
                factor(lu, pivot)
            }
            Q.stop()
            if (Q.read() >= min_time) break
            cycles *= 2
        }


        // verify that LU is correct
        val b = RandomVector(N, R)
        val x = NewVectorCopy(b)
        solve(lu, pivot, x)
        val EPS = 1.0e-12
        return if (normabs(
                b,
                matvec(A, x)
            ) / N > EPS
        ) 0.0 else LU.num_flops(N) * cycles / Q.read() * 1.0e-6


        // else return approx Mflops
        //
    }

    private fun NewVectorCopy(x: DoubleArray): DoubleArray {
        val N = x.size
        val y = DoubleArray(N)
        System.arraycopy(x, 0, y, 0, N)
        return y
    }

    private fun CopyVector(B: DoubleArray, A: DoubleArray) {
        val N = A.size
        System.arraycopy(A, 0, B, 0, N)
    }

    private fun normabs(x: DoubleArray, y: DoubleArray): Double {
        val N = x.size
        var sum = 0.0
        for (i in 0 until N) sum += abs(x[i] - y[i])
        return sum
    }

    private fun CopyMatrix(B: Array<DoubleArray>, A: Array<DoubleArray>) {
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

    private fun RandomMatrix(M: Int, N: Int, R: Random): Array<DoubleArray> {
        val A = Array(M) { DoubleArray(N) }
        for (i in 0 until N) for (j in 0 until N) A[i][j] = R.nextDouble()
        return A
    }

    private fun RandomVector(N: Int, R: Random): DoubleArray {
        val A = DoubleArray(N)
        for (i in 0 until N) A[i] = R.nextDouble()
        return A
    }

    private fun matvec(A: Array<DoubleArray>, x: DoubleArray): DoubleArray {
        val N = x.size
        val y = DoubleArray(N)
        matvec(A, x, y)
        return y
    }

    private fun matvec(A: Array<DoubleArray>, x: DoubleArray, y: DoubleArray) {
        val M = A.size
        val N: Int = A[0].size
        for (i in 0 until M) {
            var sum = 0.0
            val Ai = A[i]
            for (j in 0 until N) sum += Ai[j] * x[j]
            y[i] = sum
        }
    }
}