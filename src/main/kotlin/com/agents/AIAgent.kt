package com.agents

class AIAgent(
    private val promptExecutor: PromptExecutor,
    private val toolRegistry: ToolRegistry,
    private val systemPrompt: String,
    private val strategy: Strategy = singleRunStrategy(),
    private val maxIterations: Int = 100,
    private val verbose: Boolean = true
) {
    private val conversationHistory = mutableListOf<Message>()

    suspend fun run(userInput: String): String {
        conversationHistory.clear()
        conversationHistory.add(Message("system", systemPrompt))
        conversationHistory.add(Message("user", userInput))

        var iteration = 0
        var lastResult: ExecutionResult? = null

        while (iteration < maxIterations) {
            iteration++

            if (verbose) {
                println("\n=== Iteration $iteration ===")
            }

            val result = promptExecutor.execute(conversationHistory, toolRegistry.getAllTools())
            lastResult = result

            when (result) {
                is ExecutionResult.ToolCall -> {
                    if (verbose) {
                        println("Tool called: ${result.toolName}")
                        println("Arguments: ${result.arguments}")
                    }

                    // First, add the assistant's message with the tool_calls
                    conversationHistory.add(
                        Message(
                            role = "assistant",
                            content = null,
                            tool_calls = result.rawToolCalls
                        )
                    )

                    val tool = toolRegistry.getTool(result.toolName)
                    if (tool == null) {
                        val errorMsg = "Tool not found: ${result.toolName}"
                        conversationHistory.add(
                            Message(
                                role = "tool",
                                content = errorMsg,
                                tool_call_id = result.id,
                                name = result.toolName
                            )
                        )
                        continue
                    }

                    val toolResult = tool.execute(result.arguments)
                    val resultMessage = when (toolResult) {
                        is ToolResult.Success -> {
                            if (verbose) println("Tool result: ${toolResult.output}")
                            toolResult.output
                        }
                        is ToolResult.Error -> {
                            if (verbose) println("Tool error: ${toolResult.message}")
                            "Error: ${toolResult.message}"
                        }
                    }

                    // Then, add the tool result message with tool_call_id
                    conversationHistory.add(
                        Message(
                            role = "tool",
                            content = resultMessage,
                            tool_call_id = result.id,
                            name = result.toolName
                        )
                    )

                    if (!strategy.shouldContinue(iteration, result)) {
                        break
                    }
                }

                is ExecutionResult.FinalAnswer -> {
                    if (verbose) {
                        println("Final answer: ${result.content}")
                    }
                    return result.content
                }

                is ExecutionResult.Error -> {
                    if (verbose) {
                        println("Execution error: ${result.message}")
                    }
                    return "Error: ${result.message}"
                }
            }
        }

        return when (lastResult) {
            is ExecutionResult.FinalAnswer -> lastResult.content
            else -> "Agent stopped after $iteration iterations"
        }
    }
}
