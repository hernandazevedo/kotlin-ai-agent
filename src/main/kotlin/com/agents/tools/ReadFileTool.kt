package com.agents.tools

import com.agents.FileSystemProvider
import com.agents.Tool
import com.agents.ToolResult
import com.google.gson.JsonObject

class ReadFileTool(private val fileSystem: FileSystemProvider) : Tool {
    override val name = "read_file"
    override val description = "Reads the contents of a file at the specified path"
    override val parameters: JsonObject = JsonObject().apply {
        addProperty("type", "object")
        add("properties", JsonObject().apply {
            add("path", JsonObject().apply {
                addProperty("type", "string")
                addProperty("description", "The file path to read")
            })
        })
        add("required", com.google.gson.JsonArray().apply {
            add("path")
        })
    }

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as? String
            ?: return ToolResult.Error("Missing required parameter: path")

        return fileSystem.readFile(path).fold(
            onSuccess = { content ->
                ToolResult.Success("File content of $path:\n```\n$content\n```")
            },
            onFailure = { error ->
                ToolResult.Error("Failed to read file: ${error.message}")
            }
        )
    }
}
