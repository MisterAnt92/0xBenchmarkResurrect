package com.sformica.benchmark.scimark2

import kotlin.math.abs
import kotlin.math.min

/* Random.java based on Java Numerical Toolkit (JNT) Random.UniformSequence
      class.  We do not use Java's own java.util.Random so that we can compare
      results with equivalent C and Fortran coces.
*/
class Random {
    /* ------------------------------------------------------------------------------
                                 CLASS VARIABLES
     ------------------------------------------------------------------------------ */
    private var seed = 0
    private var m: IntArray = IntArray(0)
    private var i = 4
    private var j = 16
    private val mdig = 32
    private val one = 1
    private val m1 = (one shl mdig - 2) + ((one shl mdig - 2) - one)

    /* For mdig = 32 : m1 =          2147483647, m2 =      65536
       For mdig = 64 : m1 = 9223372036854775807, m2 = 4294967296 
    */
    private val dm1 = 1.0 / m1.toDouble()
    private var haveRange = false
    private var left = 0.0
    private var right = 1.0
    private var width = 1.0
    /* ------------------------------------------------------------------------------
                                  CONSTRUCTORS
     ------------------------------------------------------------------------------ */
    /**
     * Initializes a sequence of uniformly distributed quasi random numbers with a
     * seed based on the system clock.
     */
    constructor() {
        initialize(System.currentTimeMillis().toInt())
    }

    /**
     * Initializes a sequence of uniformly distributed quasi random numbers on a
     * given half-open interval [left,right) with a seed based on the system
     * clock.
     */
    constructor(left: Double, right: Double) {
        initialize(System.currentTimeMillis().toInt())
        this.left = left
        this.right = right
        width = right - left
        haveRange = true
    }

    /**
     * Initializes a sequence of uniformly distributed quasi random numbers with a
     * given seed.
     */
    constructor(seed: Int) {
        initialize(seed)
    }

    /**
     * Initializes a sequence of uniformly distributed quasi random numbers
     * with a given seed on a given half-open interval [left,right).
     */
    constructor(seed: Int, left: Double, right: Double) {
        initialize(seed)
        this.left = left
        this.right = right
        width = right - left
        haveRange = true
    }
    /* ------------------------------------------------------------------------------
                               PUBLIC METHODS
     ------------------------------------------------------------------------------ */
    /**
     * Returns the next random number in the sequence.
     */
    @Synchronized
    fun nextDouble(): Double {
        var nextValue: Double
        var k: Int = m[i] - m[j]
        if (k < 0) k += m1
        m[j] = k
        if (i == 0) i = 16 else i--
        if (j == 0) j = 16 else j--
        return if (haveRange) left + dm1 * k.toDouble() * width else dm1 * k.toDouble()
    }

    /**
     * Returns the next N random numbers in the sequence, as
     * a vector.
     */
    @Synchronized
    fun nextDoubles(x: DoubleArray) {
        val N = x.size
        val remainder = N and 3 // N mod 4
        if (haveRange) {
            for (count in 0 until N) {
                var k = m[i] - m[j]
                if (i == 0) i = 16 else i--
                if (k < 0) k += m1
                m[j] = k
                if (j == 0) j = 16 else j--
                x[count] = left + dm1 * k.toDouble() * width
            }
        } else {
            for (count in 0 until remainder) {
                var k = m[i] - m[j]
                if (i == 0) i = 16 else i--
                if (k < 0) k += m1
                m[j] = k
                if (j == 0) j = 16 else j--
                x[count] = dm1 * k.toDouble()
            }
            var count = remainder
            while (count < N) {
                var k = m[i] - m[j]
                if (i == 0) i = 16 else i--
                if (k < 0) k += m1
                m[j] = k
                if (j == 0) j = 16 else j--
                x[count] = dm1 * k.toDouble()
                k = m[i] - m[j]
                if (i == 0) i = 16 else i--
                if (k < 0) k += m1
                m[j] = k
                if (j == 0) j = 16 else j--
                x[count + 1] = dm1 * k.toDouble()
                k = m[i] - m[j]
                if (i == 0) i = 16 else i--
                if (k < 0) k += m1
                m[j] = k
                if (j == 0) j = 16 else j--
                x[count + 2] = dm1 * k.toDouble()
                k = m[i] - m[j]
                if (i == 0) i = 16 else i--
                if (k < 0) k += m1
                m[j] = k
                if (j == 0) j = 16 else j--
                x[count + 3] = dm1 * k.toDouble()
                count += 4
            }
        }
    }

    /*----------------------------------------------------------------------------
                             PRIVATE METHODS
    ------------------------------------------------------------------------ */
    private fun initialize(seed: Int) {
        var jseed: Int
        val k0: Int
        val k1: Int
        var j0: Int
        var j1: Int
        this.seed = seed
        m = IntArray(17)
        jseed = min(abs(seed), m1)
        if (jseed % 2 == 0) --jseed
        val m2 = one shl mdig / 2
        k0 = 9069 % m2
        k1 = 9069 / m2
        j0 = jseed % m2
        j1 = jseed / m2
        var iloop = 0
        while (iloop < 17) {
            jseed = j0 * k0
            j1 = (jseed / m2 + j0 * k1 + j1 * k0) % (m2 / 2)
            j0 = jseed % m2
            m[iloop] = j0 + m2 * j1
            ++iloop
        }
        i = 4
        j = 16
    }
}