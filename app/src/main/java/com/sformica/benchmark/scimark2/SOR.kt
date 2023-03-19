package com.sformica.benchmark.scimark2

object SOR {

    @JvmStatic
    fun num_flops(M: Int, N: Int, num_iterations: Int): Double {
        return (M.toDouble() - 1) * (N.toDouble() - 1) * num_iterations.toDouble() * 6.0
    }

    @JvmStatic
    fun execute(omega: Double, G: Array<DoubleArray>, num_iterations: Int) {
        val M = G.size
        val N: Int = G[0].size
        val omega_over_four = omega * 0.25
        val one_minus_omega = 1.0 - omega

        // update interior points
        //
        val Mm1 = M - 1
        val Nm1 = N - 1
        for (p in 0 until num_iterations) {
            for (i in 1 until Mm1) {
                val Gi = G[i]
                val Gim1 = G[i - 1]
                val Gip1 = G[i + 1]
                for (j in 1 until Nm1) Gi[j] = omega_over_four * (Gim1[j] + Gip1[j] + Gi[j - 1]
                        + Gi[j + 1]) + one_minus_omega * Gi[j]
            }
        }
    }
}