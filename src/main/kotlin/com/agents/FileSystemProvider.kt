package com.agents

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText

sealed class JVMFileSystemProvider : FileSystemProvider {

    object ReadOnly : JVMFileSystemProvider() {
        override val canWrite: Boolean = false
    }

    object ReadWrite : JVMFileSystemProvider() {
        override val canWrite: Boolean = true
    }

    override fun listDirectory(path: String): Result<List<String>> = runCatching {
        val dir = File(path)
        if (!dir.exists()) {
            return Result.failure(Exception("Directory does not exist: $path"))
        }
        if (!dir.isDirectory) {
            return Result.failure(Exception("Path is not a directory: $path"))
        }
        dir.listFiles()?.map { it.name } ?: emptyList()
    }

    override fun readFile(path: String): Result<String> = runCatching {
        val file = Path.of(path)
        if (!file.exists()) {
            return Result.failure(Exception("File does not exist: $path"))
        }
        file.readText()
    }

    override fun writeFile(path: String, content: String): Result<Unit> = runCatching {
        if (!canWrite) {
            return Result.failure(Exception("Write operation not permitted"))
        }
        val file = File(path)
        file.writeText(content)
    }
}

interface FileSystemProvider {
    val canWrite: Boolean
    fun listDirectory(path: String): Result<List<String>>
    fun readFile(path: String): Result<String>
    fun writeFile(path: String, content: String): Result<Unit>
}
