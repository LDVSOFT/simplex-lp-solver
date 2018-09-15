package net.ldvsoft.simplex_lp_solver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LpProblemBuilderDslTest {
    @Test
    fun simpleTest() {
        var x: LpVariable? = null
        var y: LpVariable? = null
        var c1: LpConstraint? = null
        var c2: LpConstraint? = null
        var c3: LpConstraint? = null
        var f1: LpFunction? = null
        var f2: LpFunction? = null

        val problem = lpProblem {
            assertTrue(variables.isEmpty())

            x = variable("x")
            y = variable("y", canBeNegative = true)

            assertEquals(setOf(x, y), variables.toSet())

            assertTrue(constraints.isEmpty())
            c1 = constraint { x!! + 2.0 * y!! lessOrEquals 4.0 * x!! - 2.0 }
            assertEquals(setOf(c1!!), constraints)
            assertEquals(mapOf(x!! to -3.0, y!! to 2.0), c1!!.f.terms)
            assertEquals(LpConstraintSign.LESS_OR_EQUAL, c1!!.sign)
            assertEquals(-2.0, c1!!.constantValue - c1!!.f.free)

            c2 = constraint { x!! + 2.0 * y!! greaterOrEquals 4.0 * x!! - 2.0 }
            assertEquals(setOf(c1!!, c2!!), constraints)
            assertEquals(LpConstraintSign.GREATER_OR_EQUAL, c2!!.sign)
            assertEquals(-2.0, c2!!.constantValue - c2!!.f.free)

            c3 = constraint { x!! + 2.0 * y!! equals 4.0 * x!! - 2.0 }
            assertEquals(setOf(c1!!, c2!!, c3!!), constraints)
            assertEquals(LpConstraintSign.EQUAL, c3!!.sign)
            assertEquals(-2.0, c3!!.constantValue - c3!!.f.free)

            assertNull(function)
            f1 = minimize { y!! + 3.0 }
            assertEquals(f1!!, function)
            assertEquals(LpFunctionOptimization.MININIZE, f1!!.optimization)
            assertEquals(mapOf(y!! to 1.0), f1!!.f.terms)
            assertEquals(3.0, f1!!.f.free)

            f2 = maximize { y!! + 3.0 }
            assertEquals(f2!!, function)
            assertEquals(LpFunctionOptimization.MAXIMIZE, f2!!.optimization)
            assertEquals(mapOf(y!! to 1.0), f2!!.f.terms)
            assertEquals(3.0, f2!!.f.free)
        }

        assertEquals(setOf(x!!, y!!), problem.variables.toSet())
        assertEquals(setOf(c1!!, c2!!, c3!!), problem.constraints.toSet())
        assertEquals(f2!!, problem.function)
    }
}