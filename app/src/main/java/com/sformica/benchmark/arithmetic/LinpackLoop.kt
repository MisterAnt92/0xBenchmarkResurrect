package com.sformica.benchmark.arithmetic

import android.os.Bundle
import android.util.Log
import com.sformica.benchmark.main.tester.TesterArithmetic
import kotlin.math.abs
import kotlin.math.max

/*

Reformatted by Jonathan Hardwick (jch@cs.cmu.edu), 3/28/96
Run benchmark 10 times to decrease effects of timer granularity, 10/20/96
Compare to LinpackLoopOpt.java.
See http://www.cs.cmu.edu/~jch/java/linpack.html for details


Ported to Java by Reed Wade    (wade@cs.utk.edu) 2/96
built using JDK 1.0 on solaris
using "javac -O Linpack.java"


Translated to C by Bonnie Toy 5/88
   (modified on 2/25/94    to fix a problem with daxpy    for
    unequal increments or equal increments not equal to 1.
        Jack Dongarra)

*/
class LinpackLoop {

    private var second_orig = -1.0

    fun second(): Double {
        if (second_orig == -1.0)
            second_orig = System.currentTimeMillis().toDouble()
        return (System.currentTimeMillis() - second_orig) / 1000
    }

    fun runBenchmark(): String {
        val a = Array(200) { DoubleArray(201) }
        val b = DoubleArray(200)
        val x = DoubleArray(200)
        val ops: Double
        val total: Double
        val norma: Double
        var normx: Double
        var resid: Double

        val ipvt = IntArray(200)
        val residnResult: Double
        val lda = 201
        val n = 100
        ops = 2.0e0 * (n * n * n) / 3.0 + 2.0 * (n * n)
        matgen(a, lda, n, b)
        val time: Double = second()
        var i = 0
        while (i < 10) {
            dgefa(a, lda, n, ipvt)
            dgesl(a, lda, n, ipvt, b, 0)
            i++
        }
        total = (second() - time) / 10.0
        i = 0
        while (i < n) {
            x[i] = b[i]
            i++
        }
        norma = matgen(a, lda, n, b)
        i = 0
        while (i < n) {
            b[i] = -b[i]
            i++
        }
        dmxpy(n, b, n, lda, x, a)
        resid = 0.0
        normx = 0.0
        i = 0
        while (i < n) {
            resid = max(resid, abs(b[i]))
            normx = max(normx, abs(x[i]))
            i++
        }
        val epsResult: Double = epslon(1.0)
        residnResult = resid / (n * norma * normx * epsResult)
        val timeResult: Double = total
        val mflops_result: Double = ops / (1.0e6 * total)

        mInfo?.putDouble(
            TesterArithmetic.MFLOPS, mflops_result
        )
        mInfo?.putDouble(
            TesterArithmetic.RESIDN, residnResult
        )
        mInfo?.putDouble(
            TesterArithmetic.TIME, timeResult
        )
        mInfo?.putDouble(
            TesterArithmetic.EPS, epsResult
        )

        Log.e(
            "Benchmark", "Mflops/s: " + mflops_result +
                    "    Time: " + timeResult + " secs" +
                    "    Norm Res: " + residnResult +
                    "    Precision: " + epsResult
        )
        return "Mflops/s: " + mflops_result +
                "    Time: " + timeResult + " secs" +
                "    Norm Res: " + residnResult +
                "    Precision: " + epsResult
    }

    private fun matgen(a: Array<DoubleArray>, lda: Int, n: Int, b: DoubleArray): Double {
        var norma: Double
        var init: Int
        var i: Int
        var j: Int
        init = 1325
        norma = 0.0
        /*    Next two for() statements switched.    Solver wants
        matrix in column order. --dmd 3/3/97 --added to this source jch 3/7/97
        */i = 0
        while (i < n) {
            j = 0
            while (j < n) {
                init = 3125 * init % 65536
                a[j][i] = (init - 32768.0) / 16384.0
                norma = max(a[j][i], norma)
                j++
            }
            i++
        }
        i = 0
        while (i < n) {
            b[i] = 0.0
            i++
        }
        j = 0
        while (j < n) {
            i = 0
            while (i < n) {
                b[i] = b[i] + a[j][i]
                i++
            }
            j++
        }
        return norma
    }

    /*
        dgefa factors a double precision matrix by gaussian elimination.

        dgefa is usually called by dgeco, but it can be called
        directly with a saving in time if    rcond    is not needed.
        (time for dgeco) = (1 + 9/n)*(time for dgefa) .

        on entry

        a             double precision[n][lda]
        the matrix to be factored.

        lda         integer
        the leading dimension of the array    a .

        n             integer
        the order of the matrix    a .

        on return

        a             an upper triangular matrix and the multipliers
        which were used to obtain it.
        the factorization can be written    a = l*u    where
        l    is a product of permutation and unit lower
        triangular matrices and    u    is upper triangular.

        ipvt        integer[n]
        an integer vector of pivot indices.

        info        integer
        = 0    normal value.
        = k    if    u[k][k] .eq. 0.0 .    this is not an error
        condition for this subroutine, but it does
        indicate that dgesl or dgedi will divide by zero
        if called.    use    rcond    in dgeco for a reliable
        indication of singularity.

        linpack. this version dated 08/14/78.
        cleve moler, university of new mexico, argonne national lab.

        functions

        blas daxpy,dscal,idamax
    */
    private fun dgefa(a: Array<DoubleArray>, lda: Int, n: Int, ipvt: IntArray): Int {
        var t: Double
        var j: Int
        var k: Int
        var kp1: Int
        var l: Int
        var info: Int

        // gaussian elimination with partial pivoting
        info = 0
        val nm1: Int = n - 1
        if (nm1 >= 0) {
            k = 0
            while (k < nm1) {
                kp1 = k + 1

                // find l = pivot index
                l = idamax(n - k, a[k], k, 1) + k
                ipvt[k] = l

                // zero pivot implies this column already triangularized
                if (a[k][l] != 0.0) {

                    // interchange if necessary
                    if (l != k) {
                        t = a[k][l]
                        a[k][l] = a[k][k]
                        a[k][k] = t
                    }

                    // compute multipliers
                    t = -1.0 / a[k][k]
                    dscal(n - (k + 1), t, a[k], k + 1, 1)

                    // row elimination with column indexing
                    j = kp1
                    while (j < n) {
                        t = a[j][l]
                        if (l != k) {
                            a[j][l] = a[j][k]
                            a[j][k] = t
                        }
                        daxpy(
                            n - (k + 1), t, a[k], k + 1, 1,
                            a[j], k + 1, 1
                        )
                        j++
                    }
                } else {
                    info = k
                }
                k++
            }
        }
        ipvt[n - 1] = n - 1

        if (a[n - 1][n - 1] == 0.0
        ) {
            info = n - 1
        }
        return info
    }

    /*
        dgesl solves the double precision system
        a * x = b    or    trans(a) * x = b
        using the factors computed by dgeco or dgefa.

        on entry

        a             double precision[n][lda]
        the output from dgeco or dgefa.

        lda         integer
        the leading dimension of the array    a .

        n             integer
        the order of the matrix    a .

        ipvt        integer[n]
        the pivot vector from dgeco or dgefa.

        b             double precision[n]
        the right hand side vector.

        job         integer
        = 0                 to solve    a*x = b ,
        = nonzero     to solve    trans(a)*x = b    where
        trans(a)    is the transpose.

        on return

        b             the solution vector    x .

        error condition

        a division by zero will occur if the input factor contains a
        zero on the diagonal.    technically this indicates singularity
        but it is often caused by improper arguments or improper
        setting of lda .    it will not occur if the subroutines are
        called correctly and if dgeco has set rcond .gt. 0.0
        or dgefa has set info .eq. 0 .

        to compute    inverse(a) * c    where    c    is a matrix
        with    p    columns
        dgeco(a,lda,n,ipvt,rcond,z)
        if (!rcond is too small){
        for (j=0,j<p,j++)
        dgesl(a,lda,n,ipvt,c[j][0],0);
        }

        linpack. this version dated 08/14/78 .
        cleve moler, university of new mexico, argonne national lab.

        functions

        blas daxpy,ddot
    */
    private fun dgesl(
        a: Array<DoubleArray>,
        lda: Int,
        n: Int,
        ipvt: IntArray,
        b: DoubleArray,
        job: Int
    ) {
        var t: Double
        var k: Int
        var kb: Int
        var l: Int
        val nm1: Int = n - 1
        if (job == 0) {

            // job = 0 , solve    a * x = b.    first solve    l*y = b
            if (nm1 >= 1) {
                k = 0
                while (k < nm1) {
                    l = ipvt[k]
                    t = b[l]
                    if (l != k) {
                        b[l] = b[k]
                        b[k] = t
                    }
                    daxpy(n - (k + 1), t, a[k], k + 1, 1, b, k + 1, 1)
                    k++
                }
            }

            // now solve    u*x = y
            kb = 0
            while (kb < n) {
                k = n - (kb + 1)
                b[k] = b[k] / a[k][k]
                t = -b[k]
                daxpy(k, t, a[k], 0, 1, b, 0, 1)
                kb++
            }
        } else {

            // job = nonzero, solve    trans(a) * x = b.    first solve    trans(u)*y = b
            k = 0
            while (k < n) {
                t = ddot(k, a[k], 0, 1, b, 0, 1)
                b[k] = (b[k] - t) / a[k][k]
                k++
            }

            // now solve trans(l)*x = y
            if (nm1 >= 1) {
                kb = 1
                while (kb < nm1) {
                    k = n - (kb + 1)
                    b[k] = b[k] + ddot(n - (k + 1), a[k], k + 1, 1, b, k + 1, 1)
                    l = ipvt[k]
                    if (l != k) {
                        t = b[l]
                        b[l] = b[k]
                        b[k] = t
                    }
                    kb++
                }
            }
        }
    }

    /*
        constant times a vector plus a vector.
        jack dongarra, linpack, 3/11/78.
    */
    private fun daxpy(
        n: Int, da: Double, dx: DoubleArray, dx_off: Int, incx: Int,
        dy: DoubleArray, dy_off: Int, incy: Int
    ) {
        var i: Int
        var ix: Int
        var iy: Int
        var m: Int
        var mp1: Int
        if (n > 0 && da != 0.0) {
            if (incx != 1 || incy != 1) {

                // code for unequal increments or equal increments not equal to 1
                ix = 0
                iy = 0
                if (incx < 0) ix = (-n + 1) * incx
                if (incy < 0) iy = (-n + 1) * incy
                i = 0
                while (i < n) {
                    dy[iy + dy_off] = dy[iy + dy_off] + da * dx[ix + dx_off]
                    ix += incx
                    iy += incy
                    i++
                }
            } else {

                // code for both increments equal to 1
                val _r = n % 4
                val _n = n - _r
                i = 0
                while (i < _n) {
                    dy[i + dy_off] = dy[i + dy_off] + da * dx[i + dx_off]
                    dy[i + 1 + dy_off] = dy[i + 1 + dy_off] + da * dx[i + 1 + dx_off]
                    dy[i + 2 + dy_off] = dy[i + 2 + dy_off] + da * dx[i + 2 + dx_off]
                    dy[i + 3 + dy_off] = dy[i + 3 + dy_off] + da * dx[i + 3 + dx_off]
                    i += 4
                }
                i = _n
                while (i < n) {
                    dy[i + dy_off] = dy[i + dy_off] + da * dx[i + dx_off]
                    i++
                }
            }
        }
    }

    /*
        forms the dot product of two vectors.
        jack dongarra, linpack, 3/11/78.
    */
    private fun ddot(
        n: Int, dx: DoubleArray, dx_off: Int, incx: Int, dy: DoubleArray,
        dy_off: Int, incy: Int
    ): Double {
        var i: Int
        var ix: Int
        var iy: Int
        var m: Int
        var mp1: Int
        var dtemp = 0.0
        if (n > 0) {
            if (incx != 1 || incy != 1) {

                // code for unequal increments or equal increments not equal to 1
                ix = 0
                iy = 0
                if (incx < 0) ix = (-n + 1) * incx
                if (incy < 0) iy = (-n + 1) * incy
                i = 0
                while (i < n) {
                    dtemp += dx[ix + dx_off] * dy[iy + dy_off]
                    ix += incx
                    iy += incy
                    i++
                }
            } else {

                // code for both increments equal to 1
                i = 0
                while (i < n) {
                    dtemp += dx[i + dx_off] * dy[i + dy_off]
                    i++
                }
            }
        }
        return dtemp
    }

    /*
        scales a vector by a constant.
        jack dongarra, linpack, 3/11/78.
    */
    private fun dscal(n: Int, da: Double, dx: DoubleArray, dx_off: Int, incx: Int) {
        var i: Int
        var m: Int
        var mp1: Int
        val nincx: Int
        if (n > 0) {
            if (incx != 1) {

                // code for increment not equal to 1
                nincx = n * incx
                i = 0
                while (i < nincx) {
                    dx[i + dx_off] = da * dx[i + dx_off]
                    i += incx
                }
            } else {

                // code for increment equal to 1
                i = 0
                while (i < n) {
                    dx[i + dx_off] = da * dx[i + dx_off]
                    i++
                }
            }
        }
    }

    /*
        finds the index of element having max. absolute value.
        jack dongarra, linpack, 3/11/78.
    */
    private fun idamax(n: Int, dx: DoubleArray, dx_off: Int, incx: Int): Int {
        var dmax: Double
        var i: Int
        var ix: Int
        var itemp = 0
        when {
            n < 1 -> {
                itemp = -1
            }
            n == 1 -> {
                itemp = 0
            }
            incx != 1 -> {

                // code for increment not equal to 1
                ix = 1
                dmax = abs(dx[dx_off])
                ix += incx
                i = 1
                while (i < n) {
                    if (abs(dx[ix + dx_off]) > dmax) {
                        itemp = i
                        dmax = abs(dx[ix + dx_off])
                    }
                    ix += incx
                    i++
                }
            }
            else -> {

                // code for increment equal to 1
                itemp = 0
                dmax = abs(dx[dx_off])
                i = 1
                while (i < n) {
                    if (abs(dx[i + dx_off]) > dmax) {
                        itemp = i
                        dmax = abs(dx[i + dx_off])
                    }
                    i++
                }
            }
        }
        return itemp
    }

    /*
        estimate unit roundoff in quantities of size x.

        this program should function properly on all systems
        satisfying the following two assumptions,
        1.    the base used in representing dfloating point
        numbers is not a power of three.
        2.    the quantity    a    in statement 10 is represented to
        the accuracy used in dfloating point variables
        that are stored in memory.
        the statement number 10 and the go to 10 are intended to
        force optimizing compilers to generate code satisfying
        assumption 2.
        under these assumptions, it should be true that,
        a    is not exactly equal to four-thirds,
        b    has a zero for its last bit or digit,
        c    is not exactly equal to one,
        eps    measures the separation of 1.0 from
        the next larger dfloating point number.
        the developers of eispack would appreciate being informed
        about any systems where these assumptions do not hold.

        *****************************************************************
        this routine is one of the auxiliary routines used by eispack iii
        to avoid machine dependencies.
        *****************************************************************

        this version dated 4/6/83.
    */
    private fun epslon(x: Double): Double {
        var b: Double
        var c: Double
        var eps: Double
        val a: Double = 4.0e0 / 3.0e0
        eps = 0.0
        while (eps == 0.0) {
            b = a - 1.0
            c = b + b + b
            eps = abs(c - 1.0)
        }
        return eps * abs(x)
    }

    /*
        purpose:
        multiply matrix m times vector x and add the result to vector y.

        parameters:

        n1 integer, number of elements in vector y, and number of rows in
        matrix m

        y double [n1], vector of length n1 to which is added
        the product m*x

        n2 integer, number of elements in vector x, and number of columns
        in matrix m

        ldm integer, leading dimension of array m

        x double [n2], vector of length n2

        m double [ldm][n2], matrix of n1 rows and n2 columns
    */
    private fun dmxpy(
        n1: Int,
        y: DoubleArray,
        n2: Int,
        ldm: Int,
        x: DoubleArray,
        m: Array<DoubleArray>
    ) {
        var i: Int
        var jmin: Int

        // cleanup odd vector
        var j: Int = 0
        while (j < n2) {
            i = 0
            while (i < n1) {
                y[i] = y[i] + x[j] * m[j][i]
                i++
            }
            j++
        }
    }

    companion object {

        var mInfo: Bundle? = null

        @JvmStatic
        fun main(info: Bundle?): String {
            val l = LinpackLoop()
            mInfo = info
            return l.runBenchmark()
        }
    }
}