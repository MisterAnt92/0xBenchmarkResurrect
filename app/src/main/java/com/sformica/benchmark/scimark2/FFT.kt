package com.sformica.benchmark.scimark2

import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Computes FFT's of complex, double precision data where n is an integer power of 2.
 * This appears to be slower than the Radix2 method,
 * but the code is smaller and simpler, and it requires no extra storage.
 *
 *
 *
 * @author Bruce R. Miller bruce.miller@nist.gov,
 * @author Derived from GSL (Gnu Scientific Library),
 * @author GSL's FFT Code by Brian Gough bjg@vvv.lanl.gov
 */
/* See {@link ComplexDoubleFFT ComplexDoubleFFT} for details of data layout.
 */
object FFT {

    fun num_flops(N: Int): Double {
        val logN = log2(N).toDouble()
        return (5.0 * N.toDouble() - 2) * logN + 2 * (N.toDouble() + 1)
    }

    /**
     * Compute Fast Fourier Transform of (complex) data, in place.
     */
    fun transform(data: DoubleArray) {
        transform_internal(data, -1)
    }

    /**
     * Compute Inverse Fast Fourier Transform of (complex) data, in place.
     */
    fun inverse(data: DoubleArray) {
        transform_internal(data, +1)
        // Normalize
        val nd = data.size
        val n = nd / 2
        val norm = 1 / n.toDouble()
        for (i in 0 until nd) data[i] *= norm
    }

    /**
     * Accuracy check on FFT of data. Make a copy of data, Compute the FFT, then
     * the inverse and compare to the original.  Returns the rms difference.
     */
    fun test(data: DoubleArray): Double {
        val nd = data.size
        // Make duplicate for comparison
        val copy = DoubleArray(nd)
        System.arraycopy(data, 0, copy, 0, nd)
        // Transform & invert
        transform(data)
        inverse(data)
        // Compute RMS difference.
        var diff = 0.0
        for (i in 0 until nd) {
            val d = data[i] - copy[i]
            diff += d * d
        }
        return sqrt(diff / nd)
    }

    /**
     * Make a random array of n (complex) elements.
     */
    private fun makeRandom(n: Int): DoubleArray {
        val nd = 2 * n
        val data = DoubleArray(nd)
        for (i in 0 until nd) data[i] = Math.random()
        return data
    }

    /**
     * Simple Test routine.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            val n = 1024
            println("n=" + n + " => RMS Error=" + test(makeRandom(n)))
        }
        for (arg in args) {
            val n = arg.toInt()
            println("n=" + n + " => RMS Error=" + test(makeRandom(n)))
        }
    }

    /* ______________________________________________________________________ */
    internal fun log2(n: Int): Int {
        var log = 0
        var k = 1
        while (k < n) {
            k *= 2
            log++
        }
        if (n != 1 shl log) throw Error("FFT: Data length is not a power of 2!: $n")
        return log
    }

    private fun transform_internal(data: DoubleArray, direction: Int) {
        if (data.isEmpty()) return
        val n = data.size / 2
        if (n == 1) return  // Identity operation!
        val logn = log2(n)

        /* bit reverse the input data for decimation in time algorithm */bitreverse(data)

        /* apply fft recursion */
        /* this loop executed log2(N) times */
        var bit = 0
        var dual = 1
        while (bit < logn) {
            var w_real = 1.0
            var w_imag = 0.0
            val theta = 2.0 * direction * Math.PI / (2.0 * dual.toDouble())
            val s = sin(theta)
            val t = sin(theta / 2.0)
            val s2 = 2.0 * t * t

            /* a = 0 */
            var b = 0
            while (b < n) {
                val i = 2 * b
                val j = 2 * (b + dual)
                val wd_real = data[j]
                val wd_imag = data[j + 1]
                data[j] = data[i] - wd_real
                data[j + 1] = data[i + 1] - wd_imag
                data[i] += wd_real
                data[i + 1] += wd_imag
                b += 2 * dual
            }

            /* a = 1 .. (dual-1) */for (a in 1 until dual) {
                /* trignometric recurrence for w-> exp(i theta) w */
                run {
                    val tmp_real = w_real - s * w_imag - s2 * w_real
                    val tmp_imag = w_imag + s * w_real - s2 * w_imag
                    w_real = tmp_real
                    w_imag = tmp_imag
                }
                var b = 0
                while (b < n) {
                    val i = 2 * (b + a)
                    val j = 2 * (b + a + dual)
                    val z1_real = data[j]
                    val z1_imag = data[j + 1]
                    val wd_real = w_real * z1_real - w_imag * z1_imag
                    val wd_imag = w_real * z1_imag + w_imag * z1_real
                    data[j] = data[i] - wd_real
                    data[j + 1] = data[i + 1] - wd_imag
                    data[i] += wd_real
                    data[i + 1] += wd_imag
                    b += 2 * dual
                }
            }
            bit++
            dual *= 2
        }
    }

    private fun bitreverse(data: DoubleArray) {
        /* This is the Goldrader bit-reversal algorithm */
        val n = data.size / 2
        val nm1 = n - 1
        var i = 0
        var j = 0
        while (i < nm1) {


            //int ii = 2*i;
            val ii = i shl 1

            //int jj = 2*j;
            val jj = j shl 1

            //int k = n / 2 ;
            var k = n shr 1
            if (i < j) {
                val tmp_real = data[ii]
                val tmp_imag = data[ii + 1]
                data[ii] = data[jj]
                data[ii + 1] = data[jj + 1]
                data[jj] = tmp_real
                data[jj + 1] = tmp_imag
            }
            while (k <= j) {
                //j = j - k ;
                j -= k

                //k = k / 2 ; 
                k = k shr 1
            }
            j += k
            i++
        }
    }
}