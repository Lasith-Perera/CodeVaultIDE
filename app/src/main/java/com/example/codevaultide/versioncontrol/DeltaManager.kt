package com.example.codevaultide.versioncontrol

/** Compatibility facade. Actual deltas are generated/applied by java-diff-utils. */
object DeltaManager {
    private val diffManager = DiffManager()

    fun createDelta(oldText: String, newText: String): Delta =
        Delta(diffManager.createPatch(oldText, newText))

    fun applyDelta(original: String, delta: Delta): String =
        diffManager.applyPatch(original, delta.patch)
}

data class Delta(val patch: String)
