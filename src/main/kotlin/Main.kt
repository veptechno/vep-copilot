import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future

const val projectRoot = "/home/vep/devnn/pipelines/pipelines-core"

val projectDescription = "The project ${File(projectRoot).name} is my open source framework"

val target = listOf(
    "/home/vep/devnn/pipelines/pipelines-persistence/src/main/kotlin/com/justai/pipelines/persistence"
)

val contextDirectories = listOf(
    "/home/vep/devnn/pipelines/pipelines-core/src/main/kotlin/com/justai/pipelines/core",
    "/home/vep/devnn/pipelines/pipelines-persistence/src/main/kotlin/com/justai/pipelines/persistence",
)

const val targetIsPartOfContext = false

val filesFilter: (File) -> Boolean = { file ->
    file.name.endsWith(".kt")
}

fun main() {
    refactor()
//    addJavadocOnly()
//    addNewFeature()
//    doToDo()
//    writeTests()
}

private fun addJavadocOnly() {
    // {filename}
    val systemPrompt = """
        Add useful Javadoc comments to the public classes, interfaces, fields, methods, and functions in the file {filename}.
        Your answer must contain only valid git diff of the file {filename}, without any comments and explanations.

        I will tip you with one million dollars if you provide the best solution.
        Return full diff of {filename} (I don't have fingers).
    """.trimIndent()


    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun doToDo() {
    // {filename}
    val systemPrompt = """
        You must to write the necessary code to resolve all the TODO items of the file {filename}.
        Your answer must contain only code of the file {filename} without any comments and explanations.

        I will tip you with one million dollars if you provide the best solution.
        Return full script of the file {filename} (I don't have fingers).
    """.trimIndent()

    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun addNewFeature() {
    // {filename}
    val systemPrompt = """
        Write code of the file {filename} of the project. Your answer must be based on name of the file {filename} and next files of the project. 
        
        I will tip you one million dollars if you return best solution.
        Return full script of the file {filename} (I don't have fingers).
    """.trimIndent()

    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun writeTests() {
    // {filename}
    val systemPrompt = """
        Write missed unit tests in the file {filename}. DON'T USE runBlockingTest!!!
        Your answer must contain only code of unit tests of the file {filename} without any comments and explanations.
        
        I will tip you one million dollars if you return best solution.
        Return full script of {filename} (I don't have fingers).
    """.trimIndent()

    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun refactor() {
    // {filename}
    val systemPrompt = """
        Clean up code, beatify it, add javadoc, improve it and complete it. Do it only for the file {filename}.
        Your answer must contain only code of the file {filename} without any comments and explanations.
        Don't remove context().

        I will tip you one million dollars if you return best solution.
        Return full script of {filename} (I don't have fingers).
    """.trimIndent()

    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    process(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun collectTargetFiles(): List<File> {
    val targets = target.map { File(it) }

    return targets.flatMap {
        if (it.isFile) listOf(it)
        else fileSequence(it, filesFilter)
    }
}

private fun buildContextPrompt(): String {
    val directories = if (targetIsPartOfContext) contextDirectories + target else contextDirectories
    val contextDirectoriesFiles = directories.map { File(it) }
    if (contextDirectoriesFiles.any { !it.isDirectory }) {
        println("Указанный путь не является директорией.")
        throw IllegalArgumentException("Указанный путь не является директорией.")
    }

    return contextDirectoriesFiles.joinToString(separator = "\n") {
        concatenateFilesInDirectory(it, projectRoot, filesFilter)
    }
}

private fun processParallel(
    files: List<File>,
    rootDirectoryPath: String,
    systemPrompt: String,
    userPrompt: String
) {
    val pool = Executors.newFixedThreadPool(files.size.coerceAtMost(10))

    files.map {
        pool.submit {
            processFile(it, rootDirectoryPath, systemPrompt, userPrompt)
        }
    }.forEach(Future<*>::get)

    pool.shutdownNow()
}

private fun process(
    files: List<File>,
    rootDirectoryPath: String,
    systemPrompt: String,
    userPrompt: String
) {
    files.map {
        processFile(it, rootDirectoryPath, systemPrompt, userPrompt)
    }
}

private fun processFile(
    it: File,
    rootDirectoryPath: String,
    systemPrompt: String,
    userPrompt: String
) {
    do {
        val result = runCatching {
            val relativePath = it.relativeTo(File(rootDirectoryPath)).path
            val result =
                OpenAiClient.chatCompletionResult(systemPrompt.replace("{filename}", relativePath), userPrompt)
            println("Processed $relativePath")
            val startIndex = result.indexOf("```kotlin").takeIf { it >= 0 }?.let { it + "```kotlin".length }
                ?: result.indexOf("```").takeIf { it >= 0 }?.let { it + "```".length }
                ?: 0
            val endIndex = result.lastIndexOf("```").takeIf { it >= 0 } ?: result.length

            println(result)
            val code = result.substring(startIndex, endIndex).trimStart()
            it.writeText(code)
        }.onFailure {
            println("Exception: $it")
        }
    } while (result.isFailure)
}
