package com.sformica.benchmark.utils

import android.content.Context

object Constant {

    const val SUNSPIDER_RESULT = "SUNSPIDER_RESULT"
    const val SUNSPIDER_FORMATTED_RESULT = "SUNSPIDER_FORMATTED_RESULT"
    const val SUNSPIDER_TOTAL = "SUNSPIDER_TOTAL"

    const val LIN_RESULT = "LIN_RESULT"

    const val PREF_FILENAME = "ZeroxBench_Preference"
    const val KEY_RESULT_CUSTOM_DIR = "KEY_RESULT_CUSTOM_DIR"
    const val KEY_RESULT_SELECTION = "KEY_RESULT_SELECTION"


    const val PREFS_TEST_IN_PROGRESS_KEY = "PREFS_TEST_IN_PROGRESS_KEY"
    const val PREFS_TEST_IN_PROGRESS_NAME = "PREFS_TEST_IN_PROGRESS_NAME"

    const val PREF_MODE = Context.MODE_PRIVATE

    const val RESOLUTION_DEFAULT = 2.0 /*secs*/
    const val RANDOM_SEED = 101010

    // default: small (cache-contained) problem sizes
    //
    const val FFT_SIZE = 1024 // must be a power of two

    const val SOR_SIZE = 100 // NxN grid

    const val SPARSE_SIZE_M = 1000
    const val SPARSE_SIZE_nz = 5000
    const val LU_SIZE = 100

    // large (out-of-cache) problem sizes
    //
    const val LG_FFT_SIZE = 1048576 // must be a power of two

    const val LG_SOR_SIZE = 1000 // NxN grid

    const val LG_SPARSE_SIZE_M = 100000
    const val LG_SPARSE_SIZE_nz = 1000000
    const val LG_LU_SIZE = 1000

    // tiny problem sizes (used to mainly to preload network classes
    //                     for applet, so that network download times
    //                     are factored out of benchmark.)
    //
    const val TINY_FFT_SIZE = 16 // must be a power of two

    const val TINY_SOR_SIZE = 10 // NxN grid

    const val TINY_SPARSE_SIZE_M = 10
    const val TINY_SPARSE_SIZE_N = 10
    const val TINY_SPARSE_SIZE_nz = 50
    const val TINY_LU_SIZE = 10
}