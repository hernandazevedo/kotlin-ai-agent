package com.agents

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

interface PromptExecutor {
    suspend fun execute(messages: List<Message>, tools: List<Tool>): ExecutionResult
}

data class Message(
    val role: String,
    val content: String? = null,
    val tool_calls: List<Map<String, Any>>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
)

sealed class ExecutionResult {
    data class ToolCall(
        val toolName: String,
        val arguments: Map<String, Any>,
        val id: String = java.util.UUID.randomUUID().toString(),
        val rawToolCalls: List<Map<String, Any>>? = null
    ) : ExecutionResult()

    data class FinalAnswer(val content: String) : ExecutionResult()
    data class Error(val message: String) : ExecutionResult()
}

class OpenAIExecutor(private val apiKey: String, private val model: String = "gpt-4") : PromptExecutor {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun execute(messages: List<Message>, tools: List<Tool>): ExecutionResult {
        try {
            val requestBody = buildRequestBody(messages, tools)
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return ExecutionResult.Error("API request failed: ${response.code} - ${response.body?.string()}")
                }

                val responseBody = response.body?.string()
                    ?: return ExecutionResult.Error("Empty response body")

                return parseResponse(responseBody)
            }
        } catch (e: Exception) {
            return ExecutionResult.Error("Execution failed: ${e.message}")
        }
    }

    private fun buildRequestBody(messages: List<Message>, tools: List<Tool>): String {
        val request = JsonObject().apply {
            addProperty("model", model)
            add("messages", gson.toJsonTree(messages.map { msg ->
                mutableMapOf<String, Any?>("role" to msg.role).apply {
                    if (msg.content != null) {
                        put("content", msg.content)
                    }
                    if (msg.tool_calls != null) {
                        put("tool_calls", msg.tool_calls)
                    }
                    if (msg.tool_call_id != null) {
                        put("tool_call_id", msg.tool_call_id)
                    }
                    if (msg.name != null) {
                        put("name", msg.name)
                    }
                }
            }))

            if (tools.isNotEmpty()) {
                add("tools", gson.toJsonTree(tools.map { tool ->
                    mapOf(
                        "type" to "function",
                        "function" to mapOf(
                            "name" to tool.name,
                            "description" to tool.description,
                            "parameters" to tool.parameters
                        )
                    )
                }))
                addProperty("parallel_tool_calls", false)
            }
        }
        return gson.toJson(request)
    }

    private fun parseResponse(responseBody: String): ExecutionResult {
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        val choices = jsonResponse.getAsJsonArray("choices")

        if (choices == null || choices.size() == 0) {
            return ExecutionResult.Error("No choices in response")
        }

        val message = choices[0].asJsonObject.getAsJsonObject("message")
        val toolCalls = message.getAsJsonArray("tool_calls")

        return if (toolCalls != null && toolCalls.size() > 0) {
            val toolCall = toolCalls[0].asJsonObject
            val function = toolCall.getAsJsonObject("function")
            val toolName = function.get("name").asString
            val argumentsJson = function.get("arguments").asString
            @Suppress("UNCHECKED_CAST")
            val arguments = gson.fromJson(argumentsJson, Map::class.java) as Map<String, Any>
            val id = toolCall.get("id").asString

            // Convert tool calls array to list of maps
            @Suppress("UNCHECKED_CAST")
            val rawToolCalls = gson.fromJson(toolCalls.toString(), List::class.java) as List<Map<String, Any>>

            ExecutionResult.ToolCall(toolName, arguments, id, rawToolCalls)
        } else {
            val content = message.get("content")?.asString ?: ""
            ExecutionResult.FinalAnswer(content)
        }
    }
}

object OpenAIModels {
    object Chat {
        const val GPT4 = "gpt-4"
        const val GPT4Turbo = "gpt-4-turbo-preview"
        const val GPT35Turbo = "gpt-3.5-turbo"
    }
}
