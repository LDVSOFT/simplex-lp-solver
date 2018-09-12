package net.ldvsoft.simplex_lp_solver

/**
 * Interface indicating linear expression or it's parts.
 */
interface LpExpressionLike {
    /**
     * Terms of linear expression: mapping from variables to their coefficients
     */
    val terms: Map<LpVariable, Double>
    /**
     * Freestanding term
     */
    val free: Double
}

/**
 * Variable in linear programming problem.
 * @param name Name of the variable. Must be unique and not start with underscore.
 * @param canBeNegative Can this variable assume negative values. It's not recommended to add (x >= 0)
 * constraint if it's known to always be such and let this property to false.
 *
 * Implements LpExpressionLike interface so it can be used as a standalone linear expression.
 */
data class LpVariable(val name: String, val canBeNegative: Boolean = false): LpExpressionLike {
    init {
        check(!name.startsWith('_')) { "Variables starting with underscore are reserved." }
    }

    override val terms: Map<LpVariable, Double>
        get() = mapOf(this to 1.0)
    override val free: Double
        get() = .0
}

/**
 * Class storing linear expressions to be used in constraints or as a function
 * in linear programming problem.
 * @see LpExpressionLike
 */
data class LpExpression(
        override val terms: Map<LpVariable, Double>,
        override val free: Double = 0.0
): LpExpressionLike

/**
 * Sign used in constraints.
 */
enum class LpConstraintSign {
    EQUAL,
    LESS_OR_EQUAL,
    GREATER_OR_EQUAL
}

/**
 * Constraint in linear programming problem in form "linearExpression sign constantValue".
 * @param f expression
 * @param sign sign that tights expression and constant
 * @param constantValue constant
 */
data class LpConstraint(val f: LpExpressionLike, val sign: LpConstraintSign, val constantValue: Double)

/**
 * Required optimization of function in linear programming problem.
 */
enum class LpFunctionOptimization {
    MAXIMIZE,
    MININIZE
}

/**
 * Function in linear programming problem to be optimized.
 */
data class LpFunction(val f: LpExpressionLike, val optimization: LpFunctionOptimization)

/**
 * Linear programming problem with given variables, constraints and a function to be maximized.
 *
 * All variables mentioned in constraints and function must be present in variables list.
 */
data class LpProblem(
        val variables: List<LpVariable>,
        val constraints: List<LpConstraint>,
        val function: LpFunction
) {
    init {
        check(constraints.all { it.f.terms.keys.all { it in variables } }) {
            "Variables used in constraints must be present in variables list"
        }
        check(function.f.terms.keys.all { it in variables }) {
            "Variables used in function must be present in variables list"
        }
    }
}