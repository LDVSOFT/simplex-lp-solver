package net.ldvsoft.simplex_lp_solver

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.opentest4j.TestAbortedException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.max
import kotlin.streams.asSequence
import kotlin.streams.asStream
import kotlin.test.assertTrue

class LpSolverTestPacksRunner {
    data class TestDescription(
            val testPack: String = "",
            val testCase: String = "",
            val testBody: Path = Paths.get("")
    ) {
        val fake: Boolean get() = testPack.isEmpty()

        override fun toString() = if (fake) "<fake>" else "Pack $testPack, case $testCase"
    }

    @DisplayName("External tests")
    @ParameterizedTest(name = "External test; {0}")
    @MethodSource("discoverTests")
    fun runExternalTest(description: TestDescription) {
        if (description.fake) {
            throw TestAbortedException("<This test case is here to mute error>")
        }

        val (problem, expectedSolution) = loadTest(Files.readAllBytes(description.testBody).toString(Charset.defaultCharset()))

        val actualSolution = solve(problem)

        when (expectedSolution) {
            is NoSolution -> assertTrue { actualSolution is NoSolution }
            is Unbounded  -> assertTrue { actualSolution is Unbounded }
            is Solved -> {
                assertTrue { actualSolution is Solved }
                val expected = expectedSolution.functionValue
                val actual = (actualSolution as Solved).functionValue

                val max = max(abs(expected), abs(actual))
                val delta = abs(expected - actual)
                assertTrue("Too different: expected $expected, actual $actual") {
                    delta / max(1.0, max) < 1e-6
                }
            }
        }
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun discoverTests(): Stream<TestDescription> {
            @Suppress("JAVA_CLASS_ON_COMPANION")
            val loader = javaClass.classLoader

            val testsPath = System.getenv("LP_TEST_PACKS").orEmpty()

            val tests = testsPath
                    .split(':')
                    .asSequence()
                    .flatMap { testsPathEntry ->
                        val testsPathEntryPath = Paths.get(testsPathEntry)
                        check(Files.isDirectory(testsPathEntryPath))
                        Files.walk(testsPathEntryPath, 2)
                                .asSequence()
                                .filter { Files.isRegularFile(it) }
                                .filterNot {
                                    it.parent.fileName.toString().startsWith(".")
                                            || it.fileName.toString().startsWith(".")
                                }
                                .map { testPath ->
                                    val pack = testPath.parent.fileName.toString()
                                    val case = testPath.fileName.toString()
                                    TestDescription(pack, case, testPath)
                                }
                    }

            return tests.asStream()
        }
    }
}

fun loadTest(text: String): Pair<LpProblem, LpSolution> {
    Scanner(text.reader()).use { scanner ->
        val output = scanner.nextLine()
        val solution = when (output) {
            "Unbounded" -> Unbounded
            "No solution" -> NoSolution
            else -> Solved(output.toDouble(), emptyMap())
        }

        val n = scanner.nextInt()
        val m = scanner.nextInt()

        val problem = lpProblem {
            val variables = (0 until n).map { variable("x$it") }

            fun LpExpressionBuilder.readTerms()
                    = variables
                    .map { v -> v * scanner.nextDouble() }
                    .reduce { a, b -> a + b }

            repeat(m) {
                constraint {
                    val terms = readTerms()
                    val c = scanner.nextDouble()
                    terms lessOrEquals c
                }
            }
            maximize { readTerms() }
        }

        return problem to solution
    }
}