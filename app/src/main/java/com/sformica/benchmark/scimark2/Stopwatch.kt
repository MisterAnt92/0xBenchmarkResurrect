package com.sformica.benchmark.scimark2

/**
 * Provides a stopwatch to measure elapsed time.
 *
 *
 *
 * <DL>
 * <DT><B>Example of use:</B></DT>
 * <DD>
</DD></DL> *
 *
 * <pre>
 * Stopwatch Q = new Stopwatch;
</pre> *
 *
 * Q.start();
 * //
 * // code to be timed here ...
 * //
 * Q.stop();
 * System.out.println("elapsed time was: " + Q.read() + " seconds.");
 *
 *
 * @author Roldan Pozo
 * @version 14 October 1997, revised 1999-04-24
 */
class Stopwatch {

    companion object {
        /**
         * Return system time (in seconds)
         */
        fun seconds(): Double {
            return System.currentTimeMillis() * 0.001
        }
    }

    init {
        reset()
    }

    private var running = false
    private var last_time = 0.0
    private var total = 0.0

    /**
     * Return system time (in seconds)
     */
    fun reset() {
        running = false
        last_time = 0.0
        total = 0.0
    }

    /**
     * Start (and reset) timer
     */
    fun start() {
        if (!running) {
            running = true
            total = 0.0
            last_time = seconds()
        }
    }

    /**
     * Resume timing, after stopping.  (Does not wipe out
     * accumulated times.)
     */
    fun resume() {
        if (!running) {
            last_time = seconds()
            running = true
        }
    }

    /**
     * Stop timer
     */
    fun stop(): Double {
        if (running) {
            total += seconds() - last_time
            running = false
        }
        return total
    }

    /**
     * Display the elapsed time (in seconds)
     */
    fun read(): Double {
        if (running) {
            total += seconds() - last_time
            last_time = seconds()
        }
        return total
    }
}