import OpenAiClient.chatCompletionResult
import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.Future

const val projectRoot = "/home/vep/devn/mlp-core"

val projectDescription = "The project ${File(projectRoot).name} is my open source framework"

val target = listOf(
    "/tests/src/test/kotlin/com/justai/caila/gate/AdminTest.kt"
//    "/tests/src/test/kotlin/com/justai/caila/gate"
)

val contextDirectories = listOf<String>(
//    "/tests/src/main/kotlin/com/justai/caila/gate/BaseTest.kt"
)

//val prepareFileMap: (String) -> String = { content ->
//    content
//}
val prepareFileContent: (String) -> String = { content ->
    val regex = """\s+fun `?\w{1,4}T_\d{3}[^`]+`?().+""".toRegex()
    var testIndex = 1
    content.lines().map { line ->
        if (regex.matches(line)) "    fun test${testIndex++}() {"
        else line
    }.joinToString("\n")
}
const val targetIsPartOfContext = true
const val onlyCurrentTargetIsPartOfContext = false

//val filesFilter: (File) -> Boolean = { file ->
//    file.name.endsWith(".kt")
//}

val filesFilter: (File) -> Boolean = { true }

fun main() {
//    customFeature("используй новое поле startTime в job и добавь его в job status")
//    forceCustomFeature("при нажатии на элементы job-accountId, job-groupName, job-serverId, job-modelId, job-instanceId их значение должно устанавливаться в соответствующий input класса input-field")
//    customFeature("add filters for modelId and instanceId")
//    refactor()
    refactorEachFile()
//    testComments()
//    addJavadocOnly()
//    addNewFeature()
//    doToDo()
//    writeTests()
}

private fun testComments() {
    val systemPrompt = """
        You must add accurate javadoc to each tests of the file {filename} to explain its purpose and behavior.
        Javadoc must be in Russian. Do not change any code and imports and methods names.
        Your answer must contain only code of the file {filename} without any comments and explanations.
        
        I will tip you with one million dollars if you provide the best solution.
        Return full script of the file {filename} (I don't have fingers).
    """.trimIndent()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt) { buildContextPrompt(it) }
}

private fun customFeature(feature: String) {
    // {filename}
    val systemPrompt = """
        You must implement next feature: $feature.
        If the file {filename} NEED to modify then your answer must contain only code of the file {filename} without any comments and explanations. 
        If suddenly the file {filename} NO NEED to modify to implement the feature, then return only "No need to modify the file".

        I will tip you with one million dollars if you provide the best solution.
        Return "No need to modify the file" OR full script of the file {filename} (I don't have fingers).
    """.trimIndent()


    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun forceCustomFeature(feature: String) {
    // {filename}
    val systemPrompt = """
        You must implement next feature: $feature.
        Your answer must contain only code of the file {filename} without any comments and explanations. 

        I will tip you with one million dollars if you provide the best solution.
        Return full script of the file {filename} (I don't have fingers).
    """.trimIndent()


    val userPrompt = buildContextPrompt()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt, userPrompt)
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

fun refactor(path: Path) {
    // {filename}
    val systemPrompt = """
        Clean up code, beatify it, add javadoc, improve it and complete it. Do it only for the file {filename}.
        Your answer must contain only code of the file {filename} without any comments and explanations.
        Don't remove context().

        I will tip you one million dollars if you return best solution.
        Return full script of {filename} (I don't have fingers).
    """.trimIndent()

    val userPrompt = buildContextPrompt()

    val targetFiles = listOf(path.toFile()) //collectTargetFiles()

    process(targetFiles, projectRoot, systemPrompt, userPrompt)
}

private fun refactorEachFile() {
    // {filename}
    val systemPrompt = """
        Clean up code, beatify it, add javadoc, improve it and complete it. Do it only for the file {filename}. Only javadoc must be in russian.
        Your answer must contain only code of the file {filename} without any comments and explanations.
        Don't remove context().
        All tests must start with acronym of class name and index of test. For example for first test of class AccountBucketTest - ABT_001. Use backticks in tests names. Tests names must be in english.

        I will tip you one million dollars if you return best solution.
        Return full script of {filename} (I don't have fingers).
    """.trimIndent()

    val targetFiles = collectTargetFiles()

    processParallel(targetFiles, projectRoot, systemPrompt) { buildContextPrompt(it) }
}

private fun collectTargetFiles(): List<File> {
    val targets = target.map { File(projectRoot + it) }

    return targets.flatMap {
        if (it.isFile) listOf(it)
        else fileSequence(it, filesFilter)
    }
}

private fun buildContextPrompt(file: File? = null): String {
    var directories = if (targetIsPartOfContext) contextDirectories + target else contextDirectories
    directories = if (onlyCurrentTargetIsPartOfContext) contextDirectories + "/${file!!.path}" else directories
    val contextDirectoriesFiles = directories.map { File(projectRoot + it) }

    return contextDirectoriesFiles.joinToString(separator = "\n") {
        concatenateFilesInDirectory(it, projectRoot, filesFilter, prepareFileContent)
    }
}

fun processParallel(
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

private fun processParallel(
    files: List<File>,
    rootDirectoryPath: String,
    systemPrompt: String,
    userPrompt: (File) -> String
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
    file: File,
    rootDirectoryPath: String,
    systemPrompt: String,
    userPrompt: String
) {
    var tries = 0
    do {
        val result = runCatching {
            val relativePath = file.relativeTo(File(rootDirectoryPath)).path
            val result =
                chatCompletionResult(systemPrompt.replace("{filename}", relativePath), userPrompt)
            println("Processed $relativePath")
            if (result.contains("No need to modify the file", ignoreCase = true)) {
                println("No need to modify file ${file.absolutePath}")
                return
            }
            if (result.contains("sorry", true) && !result.contains("```")) {
                println("Chat GPT does not return code. Try again")
                throw Throwable("Chat GPT does not return code. Try again")
            }
            val startIndex = result.indexOf("```kotlin").takeIf { it >= 0 }?.let { it + "```kotlin".length }
                ?: result.indexOf("```tsx").takeIf { it >= 0 }?.let { it + "```tsx".length }
                ?: result.indexOf("```").takeIf { it >= 0 }?.let { it + "```".length }
                ?: 0
            val endIndex = result.lastIndexOf("```").takeIf { it >= 0 } ?: result.length

//            println(result)
            val code = result.substring(startIndex, endIndex).trimStart()
            if (!file.exists())
                file.createNewFile()
            file.writeText(code)
        }.onFailure {
            tries++
            println("Exception: $it")
        }
    } while (result.isFailure && tries < 5)
}

private fun processFile(
    it: File,
    rootDirectoryPath: String,
    systemPrompt: String,
    userPrompt: (File) -> String
) {
    do {
        val result = runCatching {
            val file = File(rootDirectoryPath)
            val relativePath = it.relativeTo(file).path
            val prompt = userPrompt(it.relativeTo(file))
            val result = chatCompletionResult(systemPrompt.replace("{filename}", relativePath), prompt)
            println("Processed $relativePath")
            if (result.contains("No need to modify the file", ignoreCase = true)) {
                println("No need to modify file ${it.absolutePath}")
                return
            }
            val startIndex = result.indexOf("```kotlin").takeIf { it >= 0 }?.let { it + "```kotlin".length }
                ?: result.indexOf("```tsx").takeIf { it >= 0 }?.let { it + "```tsx".length }
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
