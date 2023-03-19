/*
 * This is adapted from a benchmark written by John Ellis and Pete Kovac
 * of Post Communications.
 * It was modified by Hans Boehm of Silicon Graphics.
 *
 * This is no substitute for real applications.  No actual application
 * is likely to behave in exactly this way.  However, this benchmark was
 * designed to be more representative of real applications than other
 * Java GC benchmarks of which we are aware.
 * It attempts to model those properties of allocation requests that
 * are important to current GC techniques.
 * It is designed to be used either to obtain a single overall performance
 * number, or to give a more detailed estimate of how collector
 * performance varies with object lifetimes.  It prints the time
 * required to allocate and collect balanced binary trees of various
 * sizes.  Smaller trees result in shorter object lifetimes.  Each cycle
 * allocates roughly the same amount of memory.
 * Two data structures are kept around during the entire process, so
 * that the measured performance is representative of applications
 * that maintain some live in-memory data.  One of these is a tree
 * containing many pointers.  The other is a large array containing
 * double precision floating point numbers.  Both should be of comparable
 * size.
 *
 * The results are only really meaningful together with a specification
 * of how much memory was used.  It is possible to trade memory for
 * better time performance.  This benchmark should be run in a 32 MB
 * heap, though we don't currently know how to enforce that uniformly.
 * Unlike the original Ellis and Kovac benchmark, we do not attempt
 * measure pause times.  This facility should eventually be added back
 * in.  There are several reasons for omitting it for now.  The original
 * implementation depended on assumptions about the thread scheduler
 * that don't hold uniformly.  The results really measure both the
 * scheduler and GC.  Pause time measurements tend to not fit well with
 * current benchmark suites.  As far as we know, none of the current
 * commercial Java implementations seriously attempt to minimize GC pause
 * times.
 *
 *	Known deficiencies:
 *		- No way to check on memory use
 *		- No cyclic data structures
 *		- No attempt to measure variation with object size
 *		- Results are sensitive to locking cost, but we dont
 *		  check for proper locking
 */
package com.sformica.benchmark.gc

import android.os.Message
import com.sformica.benchmark.main.tester.TesterGC

class Node {
    var left: Node? = null
    var right: Node? = null

    var i = 0
    var j = 0

    constructor(l: Node?, r: Node?) {
        left = l
        right = r
    }

    constructor()
}

object GCBenchmark {

    private const val kStretchTreeDepth = 16 // about 8Mb
    private const val kLongLivedTreeDepth = 14 // about 2Mb
    private const val kArraySize = 125000 // about 2Mb
    private const val kMinTreeDepth = 2
    private const val kMaxTreeDepth = 8

    @JvmField
    var out = StringBuffer()

    fun update(s: String?) {
        out.append(s).append("\n")
        val m = Message()
        m.what = TesterGC.GUI_NOTIFIER
        TesterGC.mHandler?.sendMessage(m)
    }

    // Nodes used by a tree of a given size
    private fun TreeSize(i: Int): Int = (1 shl i + 1) - 1

    // Number of iterations to use for a given tree depth
    private fun NumIters(i: Int): Int = 2 * TreeSize(kStretchTreeDepth) / TreeSize(i)

    // Build tree top down, assigning to older objects.
    private fun Populate(iDepth: Int, thisNode: Node?) {
        var iDepth = iDepth
        if (iDepth > 0) {
            iDepth--
            thisNode!!.left = Node()
            thisNode.right = Node()
            Populate(iDepth, thisNode.left)
            Populate(iDepth, thisNode.right)
        }
    }

    // Build tree bottom-up
    private fun MakeTree(iDepth: Int): Node {
        return if (iDepth <= 0) {
            Node()
        } else {
            Node(
                MakeTree(iDepth - 1),
                MakeTree(iDepth - 1)
            )
        }
    }

    private fun PrintDiagnostics() {
        val lFreeMemory = Runtime.getRuntime().freeMemory()
        val lTotalMemory = Runtime.getRuntime().totalMemory()
        update("*Total memory:$lTotalMemory bytes")
        update("*Free  memory:$lFreeMemory bytes\n")
    }

    private fun TimeConstruction(depth: Int) {
        var root: Node
        val iNumIters = NumIters(depth)
        var tempTree: Node?
        update(
            "Create " + iNumIters +
                    " trees of depth " + depth
        )
        var tStart: Long = System.currentTimeMillis()
        for (i in 0 until iNumIters) {
            tempTree = Node()
            Populate(depth, tempTree)
            tempTree = null
        }
        var tFinish: Long = System.currentTimeMillis()
        update(
            "- Top down: "
                    + (tFinish - tStart) + "msecs"
        )
        tStart = System.currentTimeMillis()
        for (i in 0 until iNumIters) {
            tempTree = MakeTree(depth)
            tempTree = null
        }
        tFinish = System.currentTimeMillis()
        update(
            "- Bottom up: "
                    + (tFinish - tStart) + "msecs"
        )
    }

    @JvmStatic
    fun benchmark() {
        out = StringBuffer()
        var root: Node
        var tempTree: Node?

        //    Debug.startMethodTracing("gcbench");
        // update("Garbage Collector Test");
        update(
            """Stretching memory:
    binary tree of depth $kStretchTreeDepth"""
        )
        PrintDiagnostics()
        val tStart: Long = System.currentTimeMillis()

        // Stretch the memory space quickly
        tempTree = MakeTree(kStretchTreeDepth)
        tempTree = null

        // Create a long lived object
        update(
            """Creating:
    long-lived binary tree of depth $kLongLivedTreeDepth"""
        )
        val longLivedTree = Node()
        Populate(kLongLivedTreeDepth, longLivedTree)

        // Create long-lived array, filling half of it
        update(
            "    long-lived array of "
                    + kArraySize + " doubles"
        )
        val array = DoubleArray(kArraySize)
        for (i in 0 until kArraySize / 2) {
            array[i] = 1.0 / i
        }
        PrintDiagnostics()
        var d = kMinTreeDepth
        while (d <= kMaxTreeDepth) {
            TimeConstruction(d)
            d += 2
        }
        if (array[1000] != 1.0 / 1000) update("Failed")
        // fake reference to LongLivedTree
        // and array
        // to keep them from being optimized away
        val tFinish: Long = System.currentTimeMillis()
        val tElapsed: Long = tFinish - tStart
        PrintDiagnostics()
        update("Completed in " + tElapsed + "ms.")
        TesterGC.time = tElapsed.toDouble()
        //Debug.stopMethodTracing();
    }
}