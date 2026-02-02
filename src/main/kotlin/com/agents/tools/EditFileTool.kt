package com.agents.tools

import com.agents.FileSystemProvider
import com.agents.Tool
import com.agents.ToolResult
import com.google.gson.JsonObject

class EditFileTool(private val fileSystem: FileSystemProvider) : Tool {
    override val name = "edit_file"
    override val description = "Edits a file by replacing its content with new content"
    override val parameters: JsonObject = JsonObject().apply {
        addProperty("type", "object")
        add("properties", JsonObject().apply {
            add("path", JsonObject().apply {
                addProperty("type", "string")
                addProperty("description", "The file path to edit")
            })
            add("content", JsonObject().apply {
                addProperty("type", "string")
                addProperty("description", "The new content for the file")
            })
        })
        add("required", com.google.gson.JsonArray().apply {
            add("path")
            add("content")
        })
    }

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        val path = args["path"] as? String
            ?: return ToolResult.Error("Missing required parameter: path")
        val content = args["content"] as? String
            ?: return ToolResult.Error("Missing required parameter: content")

        if (!fileSystem.canWrite) {
            return ToolResult.Error("Write operations are not permitted with this file system provider")
        }

        return fileSystem.writeFile(path, content).fold(
            onSuccess = {
                ToolResult.Success("Successfully updated file: $path")
            },
            onFailure = { error ->
                ToolResult.Error("Failed to edit file: ${error.message}")
            }
        )
    }
}
