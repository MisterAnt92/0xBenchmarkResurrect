package com.sformica.benchmark.scimark2

import android.os.Bundle
import com.sformica.benchmark.main.tester.TesterScimark2
import com.sformica.benchmark.scimark2.kernel.measureFFT
import com.sformica.benchmark.scimark2.kernel.measureLU
import com.sformica.benchmark.scimark2.kernel.measureMonteCarlo
import com.sformica.benchmark.scimark2.kernel.measureSOR
import com.sformica.benchmark.scimark2.kernel.measureSparseMatmult
import com.sformica.benchmark.utils.Constant

/**
 * SciMark2: A Java numerical benchmark measuring performance
 * of computational kernels for FFTs, Monte Carlo simulation,
 * sparse matrix computations, Jacobi SOR, and dense LU matrix
 * factorizations.
 */
object CommandLine {
    /* Benchmark 5 kernels with individual Mflops.
     "results[0]" has the average Mflop rate.

  */
    @JvmStatic
    fun main(info: Bundle): String {
        // default to the (small) cache-contained version
        val min_time = Constant.RESOLUTION_DEFAULT
        val FFT_size = Constant.FFT_SIZE
        val SOR_size = Constant.SOR_SIZE
        val Sparse_size_M = Constant.SPARSE_SIZE_M
        val Sparse_size_nz = Constant.SPARSE_SIZE_nz
        val LU_size = Constant.LU_SIZE

        // run the benchmark
        val res = DoubleArray(6)
        val R = Random(Constant.RANDOM_SEED)
        res[1] = measureFFT(FFT_size, min_time, R)
        res[2] = measureSOR(SOR_size, min_time, R)
        res[3] = measureMonteCarlo(min_time, R)
        res[4] = measureSparseMatmult(
            Sparse_size_M,
            Sparse_size_nz, min_time, R
        )
        res[5] = measureLU(LU_size, min_time, R)
        res[0] = (res[1] + res[2] + res[3] + res[4] + res[5]) / 5


        // print out results
        println()
        println("SciMark 2.0a")
        println()
        println("Composite Score: " + res[0])
        info.putDouble(TesterScimark2.COMPOSITE, res[0])
        print("FFT ($FFT_size): ")
        if (res[1] == 0.0) println(" ERROR, INVALID NUMERICAL RESULT!") else println(res[1])
        info.putDouble(TesterScimark2.FFT, res[1])
        println(
            "SOR (" + SOR_size + "x" + SOR_size + "): "
                    + "  " + res[2]
        )
        info.putDouble(TesterScimark2.SOR, res[2])
        println("Monte Carlo : " + res[3])
        info.putDouble(TesterScimark2.MONTECARLO, res[3])
        println(
            "Sparse matmult (N=" + Sparse_size_M +
                    ", nz=" + Sparse_size_nz + "): " + res[4]
        )
        info.putDouble(TesterScimark2.SPARSEMATMULT, res[4])
        print("LU (" + LU_size + "x" + LU_size + "): ")
        if (res[5] == 0.0) println(" ERROR, INVALID NUMERICAL RESULT!") else println(res[5])
        info.putDouble(TesterScimark2.LU, res[5])
        return ""
    }
}