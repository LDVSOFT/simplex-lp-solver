package net.ldvsoft.simplex_lp_solver

import kotlin.math.abs

object LpSolver {
    private const val EPS = 1e-9

    class State internal constructor(val n: Int, val m: Int) {
        val s: Int
            get() = n + m

        val a = Array(s) { DoubleArray(s) }
        val b = DoubleArray(s)
        val c = DoubleArray(s)
        var v: Double = 0.0
        var x = DoubleArray(s)
        val posN = IntArray(s) { i -> if (i < n) i else -1 }
        val posB = IntArray(s) { i -> if (i < n) -1 else i - n }

        fun copyTo(target: State) {
            check(target.s >= s)
            
            for (i in 0 until s) {
                for (j in 0 until s) {
                    target.a[i][j] = a[i][j]
                }
                target.b[i] = b[i]
                target.c[i] = c[i]
                target.posN[i] = posN[i]
                target.posB[i] = posB[i]
            }
            target.v = v
        }
        
        fun pivot(l: Int, e: Int) {
            val pl = posB[l]
            val pe = posN[e]
            check(pl != -1)
            check(pe != -1)

            val t = a[pl][pe]
            b[pl] /= t
            for (j in 0 until s) {
                val pj = posN[j]
                if (pj == -1 || j == e) continue
                a[pl][pj] /= t
            }
            a[pl][pe] = 1 / t
            for (i in 0 until s) {
                val pi = posB[i]
                if (pi == -1 || i == l) continue
                val t1 = a[pi][pe]
                b[pi] -= t1 * b[pl]
                for (j in 0 until s) {
                    val pj = posN[j]
                    if (pj == -1 || j == e) continue
                    a[pi][pj] -= t1 * a[pl][pj]
                }
                a[pi][pe] = -t1 * a[pl][pe]
            }
            for (j in 0 until s) {
                val pj = posN[j]
                if (pj == -1 || j == e) continue
                c[pj] -= c[pe] * a[pl][pj]
            }
            v += c[pe] * b[pl]
            c[pe] = -c[pe] * a[pl][pe]

            posN[e] = -1
            posN[l] = pe
            posB[l] = -1
            posB[e] = pl
        }

        fun doSimplex(): Boolean {
            while (true) {
                var e = -1
                var pe = -1
                for (i in 0 until s) {
                    val pi = posN[i]
                    if (pi == -1) continue
                    if (c[pi] > EPS) {
                        e = i
                        pe = pi
                        break
                    }
                }
                if (e == -1) break
                var l = -1
                var pl = -1
                for (i in 0 until s) {
                    val pi = posB[i]
                    if (pi == -1 || a[pi][pe] < EPS) continue
                    if (l == -1 || b[pl] / a[pl][pe] > b[pi] / a[pi][pe]) {
                        l = i
                        pl = pi
                    }
                }
                check(l == -1 || abs(a[pl][pe]) > EPS)
                if (l == -1) return false
                pivot(l, e)
            }
            for (i in 0 until s)
                x[i] = if (posB[i] != -1) b[posB[i]] else .0
            return true
        }
    }

    private class Task(n: Int, m: Int) {
        val originalState = State(n, m)
        val solvingState = State(n, m)

        private fun boost(): Boolean {
            var k = -1
            var pk = -1

            for (i in 0 until solvingState.s) {
                // FIXME use the fact that the structure is known? until 
                if (solvingState.posB[i] != -1 && (k == -1 || solvingState.b[pk] > solvingState.b[solvingState.posB[i]])) {
                    k = i
                    pk = solvingState.posB[i]
                }
            }
            if (solvingState.b[pk] >= -EPS) {
                return true
            }

            val startState = State(solvingState.n + 1, solvingState.m).also {
                solvingState.copyTo(it)
                for (i in 0 until it.s) {
                    it.posB[i] = -1
                    it.posN[i] = -1
                }
                it.posN[0] = solvingState.n
                for (i in 0 until solvingState.n) {
                    it.posN[i + 1] = i
                }
                for (i in 0 until it.m) {
                    it.posB[solvingState.n + i + 1] = i
                    it.a[i][solvingState.n] = -1.0
                }
                for (i in 0 until it.s) {
                    it.c[i] = .0
                }
                it.c[solvingState.n] = -1.0
            }

            startState.apply {
                pivot(k + 1, 0)
                doSimplex()
                if (abs(x[0]) > EPS) return false
                if (posB[0] != -1) {
                    val p0 = posB[0]
                    var i = -1
                    for (j in 0 until s) {
                        val pj = posN[j]
                        if (pj != -1 && abs(a[p0][pj]) > EPS) {
                            i = j
                            break
                        }
                    }
                    check(i != -1)
                    pivot(0, i)
                }
            }

            for (i in 0 until solvingState.s) {
                solvingState.posN[i] = -1
                solvingState.posB[i] = startState.posB[i + 1]
                solvingState.b[i] = startState.b[i]
            }
            var freeC = 0
            for (i in 0 until solvingState.s) {
                if (startState.posN[i + 1] == -1) continue
                val pi = freeC++
                solvingState.posN[i] = pi
                for(j in 0 until solvingState.s)
                    solvingState.a[j][pi] = startState.a[j][startState.posN[i + 1]]
                solvingState.c[pi] = startState.c[startState.posN[i + 1]]
            }
            return true
        }

        fun solve(): Boolean {
            originalState.copyTo(solvingState)
            
            if (!boost()) return false

            // FIXME for now, these are just copied, however, they don't *feel* right
            run {}
            for (i in 0 until solvingState.n) {
                solvingState.c[i] = .0
            }
            for (j in 0 until solvingState.s) {
                val pj = originalState.posN[j] // FIXME REALLY?!
                if (pj == -1) continue
                if (solvingState.posN[j] != -1) {
                    solvingState.c[solvingState.posN[j]] += originalState.c[pj]
                    continue
                }
                solvingState.v += originalState.c[pj] * solvingState.b[solvingState.posB[j]]
                for (k in 0 until originalState.s) {
                    val pk = solvingState.posN[k]
                    if (pk == -1) continue
                    solvingState.c[pk] += originalState.c[pj] * -solvingState.a[solvingState.posB[j]][pk]
                }
            }
            // FIXME over

            return solvingState.doSimplex()
        }
    }

    fun solve(variables: List<LpVariable>, inequalities: List<LpExpression>, function: LpExpression): Map<LpVariable, Double>? {
        val variablesMapping = variables.mapIndexed { index, v -> v to index }.toMap()

        val n = variables.size
        val m = inequalities.size

        val task = Task(n, m)
        // NB! This implementation wants inequalities in form of
        // ∑ a_i x_i ≤ b_i
        // while interface stores them in form of
        // ∑ a_i x_i + b_i ≥ 0
        // so negation is needed
        inequalities.forEachIndexed { index, lpExpression ->
            lpExpression.terms.forEach { (v, c) ->
                task.originalState.a[index][variablesMapping[v]!!] = c * -1 // see comment above
            }
            task.originalState.b[index] = lpExpression.free
        }
        function.terms.forEach { (v, c) ->
            task.originalState.c[variablesMapping[v]!!] = c
        }

        val solved = task.solve()

        return if (solved) task.solvingState.x.take(n).mapIndexed { index, d -> variables[index] to d }.toMap() else null
    }
}