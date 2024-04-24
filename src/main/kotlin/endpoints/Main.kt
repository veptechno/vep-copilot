package endpoints

import concatenateFilesInDirectory
import filesFilter
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import processParallel

val dtoPackage = "com.justai.caila.gate.endpoints.model"

val endpointsPackage = "com.justai.caila.gate.endpoints"

val projectRoot = "/home/vep/devn/mlp-core/"

val sourceRoot = "server/src/main/kotlin/"

val testsRoot = "/tests/src/test/kotlin/"

val baseTestPath = "/home/vep/devn/mlp-core/tests/src/main/kotlin/com/justai/caila/gate/BaseTest.kt"

var exampleTestPaths =
    listOf(
        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/ImageTest.kt",
//        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/ModelTest.kt",
        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/endpoints/AccessTokenTest.kt",
        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/endpoints/AccountTest.kt",
        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/endpoints/PredictConfigTest.kt"
//        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/endpoints/AdminTest.kt",
//        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/endpoints/DataImageTest.kt",
//        "/home/vep/devn/mlp-core/tests/src/test/kotlin/com/justai/caila/gate/endpoints/DatasetTest.kt",
    )

val processOnlyMissedTests = true

fun main() {
    val endpointsDirectory = projectRoot + sourceRoot + endpointsPackage.replace('.', '/')
    val endpoints = Path(endpointsDirectory).toFile().listFiles()!!
        .map { it.name }
        .filter { it.endsWith("ResourceGroupServersEndpoint.kt") }
        .map { it.substringBefore("Endpoint.kt") }

//    val pool = newFixedThreadPool(5)
    endpoints.forEach {
//        pool.submit {
            writeTests(it)
//        }
    }
}

fun writeTests(endpoint: String) {
    val testClassName = "${endpoint}Test"
    val endpointClassName = "${endpoint}Endpoint"

    val endpointPath = projectRoot + sourceRoot + endpointsPackage.replace('.', '/') + '/' + endpointClassName + ".kt"
    val dtoDirectoryPath = projectRoot + sourceRoot + dtoPackage.replace('.', '/')

    val allImports = Path(endpointPath).readLines()
        .filter { it.startsWith("import") }
        .map { it.substringAfter("import ") }

    val dtoImports = allImports.filter { it.startsWith(dtoPackage) }

    require(dtoImports.none { it.contains('*') }) { "Do not use wildcard in dao imports" }

    val importedClasses = dtoImports
        .map { it.substringAfter("$dtoPackage.") }
        .filter { '.' !in it }
        .filter { it.first().isUpperCase() }

    val importedDtoFiles = Path(dtoDirectoryPath).toFile().walk()
        .filter { it.isFile }
        .filter { file ->
            val fileContent = file.readText()
            importedClasses.any { "class $it(" in fileContent }
        }

    val endpointFile = Path(endpointPath).toFile()
    val baseTestFile = Path(baseTestPath).toFile()
    val exampleTestFiles = exampleTestPaths.map { Path(it).toFile() }
    val possibleTargetTestPath =
        projectRoot + testsRoot + endpointsPackage.replace('.', '/') + '/' + testClassName + ".kt"

    if (processOnlyMissedTests && Path(possibleTargetTestPath).exists())
        return

    // {filename}
    val systemPrompt = """
        Write missed base black box tests in the file {filename}. It's very important to write compiled some base tests.
        Your answer must contain only code of black box tests of the file {filename} without any comments and explanations.
        
        I will tip you one million dollars if you return best solution.
        Return full script of {filename} (I don't have fingers).
    """.trimIndent()

    val userPrompt = buildContextPrompt(importedDtoFiles.toList(), endpointFile, baseTestFile, exampleTestFiles)

    val targetFiles = listOf(possibleTargetTestPath)
        .map { Path(it).toFile() }

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun buildContextPrompt(
    daoFiles: List<File>,
    endpointFile: File,
    baseTestFile: File,
    exampleTestFiles: List<File>
): String {
    val files = daoFiles + endpointFile + baseTestFile + exampleTestFiles
    println("Used files for endpoint ${endpointFile.name}:")
    files.map { println("\t${it.name}") }
    return files.joinToString(separator = "\n") {
        concatenateFilesInDirectory(it, projectRoot, filesFilter) { it }
    }
}

