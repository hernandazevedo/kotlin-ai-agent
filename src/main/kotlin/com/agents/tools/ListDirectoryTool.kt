package com.agents.tools

import com.agents.FileSystemProvider
import com.agents.Tool
import com.agents.ToolResult
import com.google.gson.JsonObject

class ListDirectoryTool(private val fileSystem: FileSystemProvider) : Tool {
    override val name = "list_directory"
    override val description = "Lists all files and directories in the specified path"
    override val parameters: JsonObject = JsonObject().apply {
        addProperty("type", "object")
        add("properties", JsonObject().apply {
            add("path", JsonObject().apply {
                addProperty("type", "string")
                addProperty("description", "The directory path to list")
            })
        })
        add("required", com.google.gson.JsonArray().apply {
            add("path")
        })
    }

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as? String
            ?: return ToolResult.Error("Missing required parameter: path")

        return fileSystem.listDirectory(path).fold(
            onSuccess = { files ->
                val output = files.joinToString("\n") { "- $it" }
                ToolResult.Success("Contents of $path:\n$output")
            },
            onFailure = { error ->
                ToolResult.Error("Failed to list directory: ${error.message}")
            }
        )
    }
}
