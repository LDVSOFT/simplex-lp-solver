package net.ldvsoft.simplex_lp_solver

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.opentest4j.TestAbortedException
import java.nio.charset.Charset
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.max
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

            val testsSequence = loader.getResources("lp-test-pack.zip")
                    .toList()
                    .flatMap { resourceUrl ->
                        val resourcePath = Paths.get(resourceUrl.toURI())
                        FileSystems.newFileSystem(resourcePath, null).use { fs ->
                            val target = Files.createTempDirectory("lp-test-pack")
                            val results = mutableListOf<TestDescription>()
                            fs.rootDirectories.forEach { fsRoot ->
                                Files.walkFileTree(fsRoot, object : SimpleFileVisitor<Path>() {
                                    override fun visitFile(path: Path, attrs: BasicFileAttributes?): FileVisitResult {
                                        val relPath = fsRoot.relativize(path)
                                        val targetPath = target.resolve(relPath.toString())
                                        Files.copy(path, targetPath)
                                        results += TestDescription(resourcePath.toString(), relPath.fileName.toString(), targetPath)
                                        return super.visitFile(path, attrs)
                                    }
                                })

                            }
                            results
                        }
                    }.asSequence()
            return (sequenceOf(TestDescription()) + testsSequence).asStream()

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