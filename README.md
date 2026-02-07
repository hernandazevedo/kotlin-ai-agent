# Kotlin AI Agent

![Demo](assets/screen1.gif)

A minimalist AI agent that can explore, read, and modify code, built in Kotlin following the JetBrains tutorial.

## Features

- **Project Exploration**: Lists directories and files
- **Code Reading**: Reads and analyzes files
- **Code Editing**: Modifies files based on tasks
- **OpenAI Integration**: Uses GPT-4 for intelligent decision-making

## Available Tools

1. **ListDirectoryTool**: Lists all files and directories in a path
2. **ReadFileTool**: Reads file contents
3. **EditFileTool**: Modifies files by replacing their content
4. **CreateFileTool**: Creates new files with specified content

## Prerequisites

- JDK 17 or higher
- Gradle
- OpenAI API Key

## Setup

1. Clone the repository or create the project

2. Configure your OpenAI API key:
```bash
export OPENAI_API_KEY="your-api-key-here"
```

3. Build the project:
```bash
./gradlew build
```

## Usage

Run the agent by providing the project path and task:

```bash
./gradlew run --args="/path/to/project 'Your task here'"
```

### Examples

```bash
# Add a new function
./gradlew run --args="/Users/username/my-project 'Add a function to calculate Fibonacci numbers'"

# Refactor code
./gradlew run --args="/Users/username/my-project 'Refactor the UserService class to use dependency injection'"

# Fix bugs
./gradlew run --args="/Users/username/my-project 'Fix the bug in the authentication method'"
```

## Architecture

### Main Components

- **AIAgent**: Orchestrates the main execution loop
- **PromptExecutor**: Interfaces with the OpenAI API
- **ToolRegistry**: Manages available tools
- **FileSystemProvider**: Abstraction for file system operations
- **Strategy**: Defines when the agent should continue or stop

### Execution Flow

1. The agent receives a task and the project path
2. Explores the project using `list_directory`
3. Reads relevant files with `read_file`
4. Makes necessary modifications via `edit_file`
5. Returns a summary of the changes made

## Limitations

- Maximum of 100 iterations by default
- Requires a valid OpenAI API key
- Write operations require `FileSystemProvider.ReadWrite`

## Next Steps

- Add code verification
- Implement shell execution
- Improve error handling
- Add support for more LLMs

## References

Based on the article: [Building AI Agents in Kotlin - Part 1: A Minimal Coding Agent](https://blog.jetbrains.com/ai/2025/11/building-ai-agents-in-kotlin-part-1-a-minimal-coding-agent/)
