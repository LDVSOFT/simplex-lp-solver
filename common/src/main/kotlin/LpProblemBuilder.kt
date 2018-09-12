package net.ldvsoft.simplex_lp_solver

/**
 * Helper class to build LpProblem.
 */
@LpTaskBuilderMarker
class LpProblemBuilder {
    private val variables = mutableMapOf<String, LpVariable>()
    private val constraints = mutableSetOf<LpConstraint>()
    private var function: LpFunction? = null

    /**
     * Builds LpProblem.
     */
    fun build(): LpProblem {
        check(function != null) { "Function must be provided" }
        return LpProblem(variables.values.toList(), constraints.toList(), function!!)
    }

    /**
     * Adds new variable. Fails if there already is a variable with given name.
     */
    fun addVariable(v: LpVariable) {
        check(v.name !in variables) { "Variable with name \"$v.name\" already added." }
        variables[v.name] = v
    }

    /**
     * Adds new constraint. Fails if already added or if it uses variable not added via
     * `addVariable` call.
     */
    fun addConstraint(c: LpConstraint) {
        check(c.f.terms.keys.all { it in variables.values }) { "Unregistred variable" }
        constraints += c
    }

    /**
     * Sets function to optimize. Fails if uses variable not added via `addVariable` call.
     */
    fun setFunction(f: LpFunction) {
        check(f.f.terms.keys.all { it in variables.values }) { "Unregistred variable" }
        function = f
    }
}