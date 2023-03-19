package com.sformica.benchmark.scimark2

/**
 * Estimate Pi by approximating the area of a circle.
 *
 *
 * How: generate N random numbers in the unit square, (0,0) to (1,1)
 * and see how are within a radius of 1 or less, i.e.
 * <pre>
 *
 * sqrt(x^2 + y^2) < r
 *
</pre> *
 * since the radius is 1.0, we can square both sides
 * and avoid a sqrt() computation:
 * <pre>
 *
 * x^2 + y^2 <= 1.0
 *
</pre> *
 * this area under the curve is (Pi * r^2)/ 4.0,
 * and the area of the unit of square is 1.0,
 * so Pi can be approximated by
 * <pre>
 * # points with x^2+y^2 < 1
 * Pi =~         --------------------------  * 4.0
 * total # points
 *
</pre> *
 */
object MonteCarlo {

    private const val SEED = 113

    @JvmStatic
    fun num_flops(Num_samples: Int): Double {
        // 3 flops in x^2+y^2 and 1 flop in random routine
        return Num_samples.toDouble() * 4.0
    }

    @JvmStatic
    fun integrate(Num_samples: Int): Double {
        val R = Random(SEED)
        var under_curve = 0
        for (count in 0 until Num_samples) {
            val x = R.nextDouble()
            val y = R.nextDouble()
            if (x * x + y * y <= 1.0) under_curve++
        }
        return under_curve.toDouble() / Num_samples * 4.0
    }
}