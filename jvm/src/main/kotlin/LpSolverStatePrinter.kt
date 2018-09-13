package net.ldvsoft.simplex_lp_solver

/**
 * This method is intended for debug purposes. It provides semi-readable
 * multiline string, representing LpSolver.State in table-like view.
 *
 * You can provide your IDE to render State class using one-liner like this:
 * `net.ldvsoft.simplex_lp_solver.print(this).split("\n").toTypedArray()`
 */
fun print(state: State): String = state.run { buildString {
    append("state: n = $n m = $m\n")
    append("| B:")
    (0 until s).filter { posB[it] != -1 }.forEach { append(" $it->${posB[it]}") }
    append("\n")
    append("| N:")
    (0 until s).filter { posN[it] != -1 }.forEach { append(" $it->${posN[it]}") }
    append("\n")
    append("| ", "i/j".td, " | ")
    (0 until s).filter { posN[it] != -1 }.forEach { append((it to posN[it]).td, " ") }
    append("| ", "b".td, "\n")
    (0 until s).filter { posB[it] != -1 }.forEach { i ->
        val pi = posB[i]
        append("| ", (i to pi).td, " | ")
        (0 until s).filter { posN[it] != -1 }.forEach { j -> append(a[pi][posN[j]].td, " ") }
        append("| ", b[pi].td, "\n")
    }
    append("| ", "c".td, " | ")
    (0 until s).filter { posN[it] != -1 }.forEach { append(c[posN[it]].td, " ") }
    append("| ", v.td)
} }

private val Double.td: String get() = "%9.4f".format(this)
private val String.td: String get() = "%9s".format(this)
private val Pair<Int, Int>.td: String get() = "%3d (%3d)".format(this.first, this.second)