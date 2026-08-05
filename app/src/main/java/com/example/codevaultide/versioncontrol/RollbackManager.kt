package com.example.codevaultide.versioncontrol

/**
 * Handles the restoration of files to previous versions.
 */
class RollbackManager(private val deltaManager: DeltaManager) {

    /**
     * Reconstructs a file version using the delta.
     * In a full implementation, this might chain multiple deltas.
     */
    fun reconstructFile(baseText: String, delta: Delta): String {
        return deltaManager.applyDelta(baseText, delta)
    }
}
