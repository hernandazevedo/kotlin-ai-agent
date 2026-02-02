package com.agents

class ToolRegistry(configure: ToolRegistry.() -> Unit) {
    private val tools = mutableMapOf<String, Tool>()

    init {
        configure()
    }

    fun tool(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): Tool? = tools[name]

    fun getAllTools(): List<Tool> = tools.values.toList()
}
