package net.ldvsoft.simplex_lp_solver

sealed class LpSolution

/**
 * Returned by solver when linear programming problem has no solution.
 */
object NoSolution : LpSolution()

/**
 * Returned by solver when linear programming problem is unbounded.
 */
object Unbounded : LpSolution()

/**
 * Returned by solver when linear programming problem is solvable.
 * @param functionValue value or function
 * @param variablesValues values of variables that provide given function value
 */
data class Solved(
        val functionValue: Double,
        val variablesValues: Map<LpVariable, Double>
) : LpSolution()