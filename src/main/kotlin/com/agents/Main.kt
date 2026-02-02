package com.agents

import com.agents.tools.CreateFileTool
import com.agents.tools.EditFileTool
import com.agents.tools.ListDirectoryTool
import com.agents.tools.ReadFileTool

suspend fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: kotlin-ai-agent <project-path> <task>")
        println("Example: kotlin-ai-agent /path/to/project \"Add a function to calculate fibonacci numbers\"")
        return
    }

    val projectPath = args[0]
    val task = args[1]

    val apiKey = System.getenv("OPENAI_API_KEY") ?: run {
        // Fallback to .env file
        val envFile = java.io.File(".env")
        if (envFile.exists()) {
            envFile.readLines()
                .firstOrNull { it.startsWith("OPENAI_API_KEY=") }
                ?.substringAfter("OPENAI_API_KEY=")
                ?: throw IllegalStateException("OPENAI_API_KEY not found in .env file")
        } else {
            throw IllegalStateException("OPENAI_API_KEY environment variable not set and .env file not found")
        }
    }

    val executor = OpenAIExecutor(apiKey, OpenAIModels.Chat.GPT4Turbo)

    val agent = AIAgent(
        promptExecutor = executor,
        toolRegistry = ToolRegistry {
            tool(ListDirectoryTool(JVMFileSystemProvider.ReadOnly))
            tool(ReadFileTool(JVMFileSystemProvider.ReadOnly))
            tool(CreateFileTool(JVMFileSystemProvider.ReadWrite))
            tool(EditFileTool(JVMFileSystemProvider.ReadWrite))
        },
        systemPrompt = """
            You are a highly skilled programmer tasked with updating the provided codebase according to the given task.

            You have access to the following tools:
            - list_directory: Lists all files and directories in a path
            - read_file: Reads the contents of a file
            - create_file: Creates a new file with specified content
            - edit_file: Modifies an existing file by replacing its content

            Your approach should be:
            1. First, explore the project structure using list_directory
            2. Read relevant files to understand the current implementation
            3. Create new files using create_file when needed
            4. Modify existing files using edit_file
            5. Provide a clear summary of what you changed and why

            Be strategic about which files to read - avoid reading unnecessary files to conserve context.
            Always provide informative error messages if something goes wrong.
        """.trimIndent(),
        strategy = maxIterationsStrategy(100),
        verbose = true
    )

    val input = "Project absolute path: $projectPath\n\n## Task\n$task"

    println("Starting AI Agent...")
    println("Project: $projectPath")
    println("Task: $task")
    println("=" .repeat(80))

    val result = agent.run(input)

    println("\n" + "=".repeat(80))
    println("FINAL RESULT:")
    println(result)
}
