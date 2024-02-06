import java.io.File

fun concatenateFilesInDirectory(directory: File, rootPath: String, filter: (File) -> Boolean): String {
    return fileSequence(directory, filter).joinToString("") { file ->
        val relativePath = file.relativeTo(File(rootPath)).path
        """
File $relativePath. Content:
```
${file.readText()}
```


""".trimIndent()
    }
}

fun fileSequence(directory: File, filter: (File) -> Boolean) =
    directory.walk().filter { it.isFile && filter(it) }.toList()
