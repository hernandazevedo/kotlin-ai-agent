package com.agents.mcp

import com.agents.Tool
import com.agents.ToolResult
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Adapter that bridges MCP tools to the AI Agent's Tool interface.
 * Converts between Gson JsonObject (used by Agent) and kotlinx.serialization JsonObject (used by MCP).
 */
class McpToolAdapter(
    private val mcpClient: McpClient,
    private val mcpTool: McpTool
) : Tool {
    override val name: String = mcpTool.name
    override val description: String = mcpTool.description
    override val parameters: JsonObject = convertKotlinxToGson(mcpTool.inputSchema)

    override suspend fun execute(args: Map<String, Any>): ToolResult {
        return try {
            val result = mcpClient.callTool(mcpTool.name, args)

            if (result.isError) {
                ToolResult.Error(result.content.firstOrNull()?.text ?: "Unknown error")
            } else {
                val output = result.content.joinToString("\n") { it.text }
                ToolResult.Success(output)
            }
        } catch (e: McpException) {
            ToolResult.Error("MCP error: ${e.message}")
        } catch (e: Exception) {
            ToolResult.Error("Failed to execute tool '${mcpTool.name}': ${e.message}")
        }
    }

    /**
     * Convert kotlinx.serialization JsonObject to Gson JsonObject
     */
    private fun convertKotlinxToGson(kotlinxJson: kotlinx.serialization.json.JsonObject): JsonObject {
        val jsonString = kotlinxJson.toString()
        return JsonParser.parseString(jsonString).asJsonObject
    }
}
