package com.agents

import com.google.gson.JsonObject

interface Tool {
    val name: String
    val description: String
    val parameters: JsonObject

    suspend fun execute(args: Map<String, Any>): ToolResult
}

sealed class ToolResult {
    data class Success(val output: String) : ToolResult()
    data class Error(val message: String) : ToolResult()
}
