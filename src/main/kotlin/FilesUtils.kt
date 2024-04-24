import java.io.File

fun concatenateFilesInDirectory(directory: File, rootPath: String, filter: (File) -> Boolean, prepareFileContent: (String) -> String): String {
    return fileSequence(directory, filter).joinToString("") { file ->
        val relativePath = file.relativeTo(File(rootPath)).path
        """
File $relativePath. Content:
```
${prepareFileContent(file.readText())}
```


""".trimIndent()
    }
}

fun fileSequence(directory: File, filter: (File) -> Boolean): List<File> =
    if (directory.isFile) listOf(directory)
    else directory.walk().filter { it.isFile && filter(it) }.toList()
