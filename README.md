# Kotlin AI Agent

![Demo](assets/screen1.gif)

A minimalist AI agent that can explore, read, modify code, and perform Git operations, built in Kotlin following the JetBrains tutorial. Features MCP (Model Context Protocol) integration for extensible tool support.

## Features

- **Project Exploration**: Lists directories and files
- **Code Reading**: Reads and analyzes files
- **Code Editing**: Modifies files based on tasks
- **Git Operations**: Status, diff, commit, branch management via MCP
- **OpenAI Integration**: Uses GPT-4 for intelligent decision-making
- **MCP Integration**: Extensible tool system using Model Context Protocol

## Available Tools

### File System Tools
1. **ListDirectoryTool**: Lists all files and directories in a path
2. **ReadFileTool**: Reads file contents
3. **EditFileTool**: Modifies files by replacing their content
4. **CreateFileTool**: Creates new files with specified content

### Git Tools (via MCP Server)
5. **git_status**: Show the working tree status
6. **git_diff**: Show changes between commits, commit and working tree
7. **git_commit**: Record changes to the repository
8. **git_log**: Show commit logs
9. **git_branch**: List, create, or delete branches
10. **git_checkout**: Switch branches or restore working tree files
11. **git_add**: Add file contents to the staging area

**Note**: Git tools are automatically discovered when the MCP Git Server is running. If the server is unavailable, the agent falls back to file system tools only.

## Prerequisites

- JDK 17 or higher
- Gradle
- OpenAI API Key
- (Optional) [MCP Git Server](https://github.com/hernandazevedo/mcp-git-server) running for Git operations

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

4. (Optional) Start the MCP Git Server for Git operations:

First, clone and setup the [MCP Git Server](https://github.com/hernandazevedo/mcp-git-server):
```bash
git clone https://github.com/hernandazevedo/mcp-git-server.git
cd mcp-git-server
chmod +x mcp-git-server.sh
```

Then start the server:
```bash
# In a separate terminal
./gradlew run
# or
./mcp-git-server.sh http
```

The MCP server will start on `http://localhost:8080` by default.

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

# With Git operations (requires MCP server running)
./gradlew run --args="/Users/username/my-project 'Fix the calculator bug and commit the changes with a descriptive message'"

# Check git status
./gradlew run --args="/Users/username/my-project 'Check the git status and tell me what files have changed'"
```

## Architecture

### Main Components

- **AIAgent**: Orchestrates the main execution loop
- **PromptExecutor**: Interfaces with the OpenAI API
- **ToolRegistry**: Manages available tools
- **FileSystemProvider**: Abstraction for file system operations
- **Strategy**: Defines when the agent should continue or stop

### MCP Integration Components

- **McpClient**: HTTP client for JSON-RPC communication with MCP servers
- **McpProtocol**: Data classes implementing MCP 2024-11-05 specification
- **McpToolAdapter**: Bridges MCP tools to the Agent's Tool interface
- **McpToolDiscovery**: Auto-discovers and registers tools from MCP servers

### Execution Flow

1. The agent initializes and connects to the MCP server (if available)
2. Auto-discovers Git tools from the MCP server
3. Receives a task and the project path
4. Explores the project using `list_directory`
5. Reads relevant files with `read_file`
6. Makes necessary modifications via `edit_file`
7. (Optional) Performs Git operations via MCP tools
8. Returns a summary of the changes made

## Configuration

### Environment Variables

- `OPENAI_API_KEY`: Your OpenAI API key (required)
- `MCP_SERVER_URL`: MCP server URL (default: `http://localhost:8080/mcp`)
- `GIT_WORKING_DIR`: Working directory for Git operations on MCP server side

### Example Configuration

```bash
export OPENAI_API_KEY="sk-..."
export MCP_SERVER_URL="http://localhost:8080/mcp"
export GIT_WORKING_DIR="/path/to/repo"
```

## MCP Integration Details

This project implements **Option 3: Full MCP Client Integration**, which:

- Follows the [Model Context Protocol](https://modelcontextprotocol.io/) specification (2024-11-05)
- Uses JSON-RPC 2.0 over HTTP for communication
- Auto-discovers tools from MCP servers at startup
- Gracefully degrades when MCP server is unavailable
- Supports multiple MCP servers (extensible architecture)

### Tool Discovery Process

1. Agent starts and attempts to connect to MCP server
2. Sends `initialize` request to establish connection
3. Sends `tools/list` request to discover available tools
4. Creates adapter instances for each tool
5. Registers tools with the agent's ToolRegistry
6. Tools are now available for the AI to use

## Limitations

- Maximum of 100 iterations by default
- Requires a valid OpenAI API key
- Write operations require `FileSystemProvider.ReadWrite`
- Git operations require MCP Git Server to be running

## Next Steps

- Add more MCP servers (database, API tools, etc.)
- Add code verification
- Implement MCP server lifecycle management
- Improve error handling and retry logic
- Add support for more LLMs
- Create comprehensive integration tests

## References

- Based on the article: [Building AI Agents in Kotlin - Part 1: A Minimal Coding Agent](https://blog.jetbrains.com/ai/2025/11/building-ai-agents-in-kotlin-part-1-a-minimal-coding-agent/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [MCP Git Server](https://github.com/hernandazevedo/mcp-git-server) - The MCP server used for Git operations integration
