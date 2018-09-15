package net.ldvsoft.simplex_lp_solver

/**
 * Helper class to build LpProblem.
 */
@LpTaskBuilderMarker
class LpProblemBuilder {
    /**
     * Variables registered to be used in a problem.
     * Add via `addVariable`.
     */
    val variables: Collection<LpVariable>
        get() = variablesMap.values

    /**
     * Constraints added to a problem.
     * Add via `addConstraint`.
     */
    val constraints: Set<LpConstraint>
        get() = constraintsSet

    /**
     * Function to optimize. Setter fails if uses variable not added via `addVariable` call.
     * Must be assigned before build().
     */
    var function: LpFunction? = null
        set(newValue) {
            val v = checkNotNull(newValue)
            check(v.f.terms.keys.all { it in variablesMap.values }) { "Unregistred variable" }
            field = v
        }

    private val variablesMap = mutableMapOf<String, LpVariable>()
    private val constraintsSet = mutableSetOf<LpConstraint>()

    /**
     * Builds LpProblem.
     */
    fun build(): LpProblem {
        check(function != null) { "Function must be provided" }
        return LpProblem(variablesMap.values.toList(), constraintsSet.toList(), function!!)
    }

    /**
     * Adds new variable. Fails if there already is a variable with given name.
     */
    fun addVariable(v: LpVariable) {
        check(v.name !in variablesMap) { "Variable with name \"$v.name\" already added." }
        variablesMap[v.name] = v
    }

    /**
     * Adds new constraint. Fails if already added or if it uses variable not added via
     * `addVariable` call.
     */
    fun addConstraint(c: LpConstraint) {
        check(c !in constraintsSet) { "Constraint already added" }
        check(c.f.terms.keys.all { it in variablesMap.values }) { "Unregistred variable" }
        constraintsSet += c
    }
}