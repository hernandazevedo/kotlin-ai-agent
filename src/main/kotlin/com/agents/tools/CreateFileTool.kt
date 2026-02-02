package com.agents.tools

import com.agents.FileSystemProvider
import com.agents.Tool
import com.agents.ToolResult
import com.google.gson.JsonObject
import java.io.File

class CreateFileTool(private val fileSystem: FileSystemProvider) : Tool {
    override val name = "create_file"
    override val description = "Creates a new file with the specified content"
    override val parameters: JsonObject = JsonObject().apply {
        addProperty("type", "object")
        add("properties", JsonObject().apply {
            add("path", JsonObject().apply {
                addProperty("type", "string")
                addProperty("description", "The file path to create")
            })
            add("content", JsonObject().apply {
                addProperty("type", "string")
                addProperty("description", "The content for the new file")
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

        val file = File(path)

        // Check if file already exists
        if (file.exists()) {
            return ToolResult.Error("File already exists: $path. Use edit_file to modify existing files.")
        }

        // Create parent directories if they don't exist
        file.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }

        return fileSystem.writeFile(path, content).fold(
            onSuccess = {
                ToolResult.Success("Successfully created file: $path")
            },
            onFailure = { error ->
                ToolResult.Error("Failed to create file: ${error.message}")
            }
        )
    }
}