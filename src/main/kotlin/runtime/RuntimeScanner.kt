package runtime

import endpoints.exampleTestPaths
import endpoints.projectRoot
import endpoints.writeTests
import java.nio.file.Files.walk
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.reader
import kotlin.io.path.writeLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import refactor

class RuntimeScanner {

    private val scannerScope = CoroutineScope(newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob())

    fun startScan() = scannerScope.launch {
        while (isActive) {
            bbtScan()
            refactorScan()
        }
    }

    private fun bbtScan() {
        val filesToCoverTests = walk(Path(projectRoot))
            .filter { it.isRegularFile() }
            .filter {
                runCatching { it.reader().buffered().readLine() == "//endpoint-bbt" }
                    .getOrElse { false }
            }

        val testsExamples = walk(Path(projectRoot))
            .filter { it.isRegularFile() }
            .filter {
                runCatching { it.reader().buffered().readLine() == "//endpoint-bbt-example" }
                    .getOrElse { false }
            }

        exampleTestPaths = testsExamples.map { it.absolutePathString() }.toList()

        filesToCoverTests
            .forEach {
                val updatedContent = it.readLines().filter { it != "//endpoint-bbt" }
                it.writeLines(updatedContent)

                writeTests(it.absolutePathString().substringAfterLast("/").substringBefore("Endpoint.kt"))
            }
    }

    private fun refactorScan() {
        val filesToRefactor = walk(Path(projectRoot))
            .filter { it.isRegularFile() }
            .filter {
                runCatching { it.reader().buffered().readLine() == "//refactor" }
                    .getOrElse { false }
            }


        filesToRefactor
            .forEach {
                val updatedContent = it.readLines().filter { it != "//refactor" }
                it.writeLines(updatedContent)

                refactor(it)
            }
    }
}

fun main() {
    runBlocking {
        RuntimeScanner().startScan().join()
    }
}
