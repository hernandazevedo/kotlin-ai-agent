package com.agents

interface Strategy {
    fun shouldContinue(iteration: Int, result: ExecutionResult): Boolean
}

fun singleRunStrategy(): Strategy = object : Strategy {
    override fun shouldContinue(iteration: Int, result: ExecutionResult): Boolean {
        return when (result) {
            is ExecutionResult.ToolCall -> true
            is ExecutionResult.FinalAnswer -> false
            is ExecutionResult.Error -> false
        }
    }
}

fun maxIterationsStrategy(maxIterations: Int): Strategy = object : Strategy {
    override fun shouldContinue(iteration: Int, result: ExecutionResult): Boolean {
        if (iteration >= maxIterations) return false

        return when (result) {
            is ExecutionResult.ToolCall -> true
            is ExecutionResult.FinalAnswer -> false
            is ExecutionResult.Error -> false
        }
    }
}
