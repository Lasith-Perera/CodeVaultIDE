package com.example.codevaultide.versioncontrol

/** Reconstructs a version by applying its incremental patches from a checkpoint. */
class RollbackManager(private val diffManager: DiffManager = DiffManager()) {
    fun reconstructFile(baseText: String, patches: List<String>): String {
        var result = baseText
        patches.forEach { result = diffManager.applyPatch(result, it) }
        return result
    }
}
