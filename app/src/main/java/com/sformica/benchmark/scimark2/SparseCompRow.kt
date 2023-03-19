package com.sformica.benchmark.scimark2

object SparseCompRow {
    /* multiple iterations used to make kernel have roughly
        same granulairty as other Scimark kernels. */
    @JvmStatic
    fun num_flops(N: Int, nz: Int, num_iterations: Int): Double {
        /* Note that if nz does not divide N evenly, then the
           actual number of nonzeros used is adjusted slightly.
        */
        val actual_nz = nz / N * N
        return actual_nz.toDouble() * 2.0 * num_iterations.toDouble()
    }

    /* computes  a matrix-vector multiply with a sparse matrix
        held in compress-row format.  If the size of the matrix
        in MxN with nz nonzeros, then the val[] is the nz nonzeros,
        with its ith entry in column col[i].  The integer vector row[]
        is of size M+1 and row[i] points to the begining of the
        ith row in col[].  
    */
    @JvmStatic
    fun matmult(
        y: DoubleArray, `val`: DoubleArray, row: IntArray,
        col: IntArray, x: DoubleArray, NUM_ITERATIONS: Int
    ) {
        val M = row.size - 1
        for (reps in 0 until NUM_ITERATIONS) {
            for (r in 0 until M) {
                var sum = 0.0
                val rowR = row[r]
                val rowRp1 = row[r + 1]
                for (i in rowR until rowRp1) sum += x[col[i]] * `val`[i]
                y[r] = sum
            }
        }
    }
}