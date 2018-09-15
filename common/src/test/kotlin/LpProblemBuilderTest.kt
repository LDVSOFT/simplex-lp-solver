package net.ldvsoft.simplex_lp_solver

import kotlin.test.*

class LpProblemBuilderTest {
    @Test
    fun variablesTest() {
        val x = LpVariable("x")
        val y = LpVariable("y", canBeNegative = true)

        LpProblemBuilder().apply {
            assertTrue { variables.isEmpty() }

            addVariable(x)
            assertEquals(setOf(x), variables.toSet())

            assertFails { addVariable(x) }
            assertEquals(setOf(x), variables.toSet())

            addVariable(y)
            assertEquals(setOf(x, y), variables.toSet())

            assertFails { build() }
            function = LpFunction(x, LpFunctionOptimization.MAXIMIZE)

            assertEquals(setOf(x, y), build().variables.toSet())
        }

    }

    @Test
    fun constraintsTest() {
        val x = LpVariable("x")
        val y = LpVariable("y")

        val xConstraint = LpConstraint(x, LpConstraintSign.LESS_OR_EQUAL, 2.0)
        val xyConstraint = LpConstraint(
                LpExpression(mapOf(x to 1.0, y to 2.0)),
                LpConstraintSign.LESS_OR_EQUAL,
                2.0
        )

        LpProblemBuilder().apply {
            assertFails { addConstraint(xConstraint) }
            assertTrue { constraints.isEmpty() }

            addVariable(x)
            addConstraint(xConstraint)
            assertEquals(setOf(xConstraint), constraints)

            assertFails { addConstraint(xConstraint) }
            assertEquals(setOf(xConstraint), constraints)

            assertFails { build() }
            function = LpFunction(x, LpFunctionOptimization.MAXIMIZE)

            assertEquals(listOf(xConstraint), build().constraints)

            assertFails { addConstraint(xyConstraint) }
            assertEquals(setOf(xConstraint), constraints)

            addVariable(y)
            addConstraint(xyConstraint)
            assertEquals(setOf(xConstraint, xyConstraint), constraints)

            assertEquals(setOf(xyConstraint, xConstraint), build().constraints.toSet())
        }
    }

    @Test
    fun functionTest() {
        val x = LpVariable("x")
        val y = LpVariable("y")
        val f1 = LpFunction(x, LpFunctionOptimization.MAXIMIZE)
        val f2 = LpFunction(x, LpFunctionOptimization.MININIZE)
        val f3 = LpFunction(LpExpression(mapOf(x to 1.0, y to 1.0)), LpFunctionOptimization.MININIZE)

        LpProblemBuilder().apply {
            assertNull(function)

            assertFails { function = f1 }
            assertNull(function)
            assertFails { build() }

            addVariable(x)
            function = f1
            assertEquals(f1, function)
            assertEquals(f1, build().function)

            function = f2
            assertEquals(f2, function)
            assertEquals(f2, build().function)

            assertFails { function = f3 }
        }
    }
}