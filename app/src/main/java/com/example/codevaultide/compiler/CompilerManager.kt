package com.example.codevaultide.compiler

import kotlinx.coroutines.delay

/**
 * Simulates a Kotlin compiler service.
 */
class CompilerManager {

    /**
     * Simulates compiling the given [code].
     * Returns a pair of (IsSuccess, OutputMessage)
     */
    suspend fun compileAndRun(code: String): Pair<Boolean, String> {
        // Simulate network/process delay
        delay(1500)

        return when {
            code.isBlank() -> {
                false to "Error: No code provided to the compiler."
            }
            code.contains("fun main()") -> {
                true to """
                    > Starting compilation...
                    > Successfully compiled Main.kt
                    > Running...
                    
                    Hello from CodeVault IDE!
                    
                    Process finished with exit code 0
                """.trimIndent()
            }
            else -> {
                false to """
                    > Starting compilation...
                    e: Main.kt: (1, 1): Expecting a 'fun main()' entry point.
                    
                    Compilation failed with 1 error
                """.trimIndent()
            }
        }
    }
}
