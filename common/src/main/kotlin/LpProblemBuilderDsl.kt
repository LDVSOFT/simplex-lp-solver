package net.ldvsoft.simplex_lp_solver

@DslMarker
annotation class LpTaskBuilderMarker

/**
 * Methods present here are for easy building of LpProblem using DSL:
 * ```
 * buildLp {
 *   val x = variable("x")
 *   val y = variable("y")
 *   constraint { x + y lessOrEquals 2.0 }
 *   minimize { x }
 * }
 * ```
 */

@LpTaskBuilderMarker
open class LpExpressionBuilder {
    protected val Double.asE: LpExpressionLike
        get() { return object : LpExpressionLike {
            override val terms: Map<LpVariable, Double> get() = emptyMap()
            override val free: Double get() = this@asE
        } }

    private fun Map<LpVariable, Double>.merge(that: Map<LpVariable, Double>, f: (Double, Double) -> Double)
            = toMutableMap().also { result ->
        that.forEach { (k, v) -> result[k] = f(result[k] ?: .0, v) }
    }

    operator fun LpExpressionLike.unaryPlus() = this
    operator fun LpExpressionLike.unaryMinus() = LpExpression(terms.mapValues { (_, v) -> v }, -free)

    operator fun LpExpressionLike.plus(that: LpExpressionLike)
            = LpExpression(this.terms.merge(that.terms, Double::plus), this.free + that.free)
    operator fun LpExpression.plus(that: Double) = this + that.asE
    operator fun Double.plus(that: LpExpressionLike) = this.asE + that

    operator fun LpExpressionLike.minus(that: LpExpressionLike) = this + (-that)
    operator fun LpExpression.minus(that: Double) = this - that.asE
    operator fun Double.minus(that: LpExpressionLike) = this.asE - that

    operator fun LpExpressionLike.times(that: Double)
            = LpExpression(terms.mapValues { (_, v) -> v * that }, free * that)
    operator fun Double.times(that: LpExpressionLike) = that * this
}

class LpConstraintBuilder : LpExpressionBuilder() {
    infix fun LpExpressionLike.greaterOrEquals(that: LpExpressionLike)
            = LpConstraint(this - that, LpConstraintSign.GREATER_OR_EQUAL, .0)
    infix fun LpExpressionLike.greaterOrEquals(that: Double) = this greaterOrEquals that.asE
    infix fun Double.greaterOrEquals(that: LpExpressionLike) = this.asE greaterOrEquals that

    infix fun LpExpressionLike.lessOrEquals(that: LpExpressionLike)
            = LpConstraint(this - that, LpConstraintSign.LESS_OR_EQUAL, .0)
    infix fun LpExpressionLike.lessOrEquals(that: Double) = this lessOrEquals that.asE
    infix fun Double.lessOrEquals(that: LpExpressionLike) = this.asE lessOrEquals that

    infix fun LpExpressionLike.equals(that: LpExpressionLike)
            = LpConstraint(this - that, LpConstraintSign.EQUAL, .0)
    infix fun LpExpressionLike.equals(that: Double) = this equals that.asE
    infix fun Double.equals(that: LpExpressionLike) = this.asE equals that
}

inline fun lpProblem(block: LpProblemBuilder.() -> Unit): LpProblem = LpProblemBuilder().apply(block).build()

inline fun LpProblemBuilder.constraint(block: LpConstraintBuilder.() -> LpConstraint) { addConstraint(LpConstraintBuilder().block()) }

inline fun LpProblemBuilder.function(optimization: LpFunctionOptimization, block: LpExpressionBuilder.() -> LpExpressionLike) {
    setFunction(LpFunction(LpExpressionBuilder().block(), optimization))
}

inline fun LpProblemBuilder.maximize(block: LpExpressionBuilder.() -> LpExpressionLike) = function(LpFunctionOptimization.MAXIMIZE, block)
inline fun LpProblemBuilder.minimize(block: LpExpressionBuilder.() -> LpExpressionLike) = function(LpFunctionOptimization.MININIZE, block)

fun LpProblemBuilder.variable(name: String, canBeNegative: Boolean = false): LpVariable
        = LpVariable(name, canBeNegative).also { addVariable(it) }