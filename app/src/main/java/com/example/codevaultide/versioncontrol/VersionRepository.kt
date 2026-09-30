package com.example.codevaultide.versioncontrol

import com.example.codevaultide.database.VersionDao
import com.example.codevaultide.database.VersionEntity

/**
 * Stores one full BASE version per file and DELTA versions after it.
 *
 * Branch roots are also DELTA records. They point to the version they were
 * created from instead of duplicating the full file content.
 */
class VersionRepository(
    private val dao: VersionDao
) {

    private val diffManager = DiffManager()

    /**
     * Reconstruct a version by walking backwards to the single BASE version,
     * then applying each delta in chronological parent -> child order.
     */
    suspend fun resolveVersion(versionId: Long): String? {
        val target = dao.getVersionById(versionId) ?: return null

        val deltaChain = mutableListOf<VersionEntity>()
        var cursor: VersionEntity? = target

        while (
            cursor != null &&
            cursor.storageType != VersionEntity.STORAGE_BASE
        ) {
            deltaChain += cursor

            cursor = cursor.parentVersionId?.let { parentId ->
                dao.getVersionById(parentId)
            }
        }

        val base = cursor ?: return null

        var result =
            base.snapshotContent
                ?: base.content
                ?: return null

        deltaChain
            .asReversed()
            .forEach { version ->

                result =
                    diffManager.applyPatch(
                        result,
                        version.deltaPatch.orEmpty()
                    )
            }

        return result
    }

    /**
     * Create a normal version on a branch.
     *
     * Only the very first version of the entire file stores full content.
     * Every version after that stores a delta.
     */
    suspend fun createVersion(
        fileId: Long,
        fileName: String,
        content: String,
        description: String,
        branchName: String = "main"
    ): Long? {

        val latestOnBranch =
            dao.getLatestVersion(
                fileId,
                branchName
            )

        /*
         * No version exists on this branch.
         */
        if (latestOnBranch == null) {

            val existingAnyVersion =
                dao.getLatestVersionAnyBranch(
                    fileId
                )

            /*
             * No history exists at all.
             *
             * This becomes the ONE AND ONLY
             * full BASE version.
             */
            if (existingAnyVersion == null) {

                return dao.insertVersion(
                    VersionEntity(
                        fileId = fileId,
                        versionNumber = 1,
                        parentVersionId = null,
                        branchName = branchName,

                        snapshotContent = content,

                        content = null,
                        deltaPatch = null,

                        storageType =
                            VersionEntity.STORAGE_BASE,

                        description = description,
                        storedFileName = fileName
                    )
                )
            }

            /*
             * Safety fallback.
             *
             * If another branch already exists,
             * DO NOT create another full snapshot.
             *
             * Instead link this new branch to the
             * latest existing version.
             */
            val parentContent =
                resolveVersion(
                    existingAnyVersion.id
                )
                    ?: return null

            val patch =
                diffManager.createPatch(
                    parentContent,
                    content
                )

            return dao.insertVersion(
                VersionEntity(
                    fileId = fileId,
                    versionNumber = 1,

                    parentVersionId =
                        existingAnyVersion.id,

                    branchName =
                        branchName,

                    snapshotContent = null,
                    content = null,

                    deltaPatch =
                        patch,

                    storageType =
                        VersionEntity.STORAGE_DELTA,

                    description =
                        description,

                    storedFileName = fileName
                )
            )
        }

        /*
         * Normal save on an existing branch.
         */

        val previousContent =
            resolveVersion(
                latestOnBranch.id
            )
                ?: return null

        /*
         * Do not create duplicate versions
         * when file content hasn't changed.
         */
        if (previousContent == content) {
            return null
        }

        val nextVersionNumber =
            latestOnBranch.versionNumber + 1

        val patch =
            diffManager.createPatch(
                previousContent,
                content
            )

        return dao.insertVersion(
            VersionEntity(
                fileId = fileId,

                versionNumber =
                    nextVersionNumber,

                parentVersionId =
                    latestOnBranch.id,

                branchName =
                    branchName,

                snapshotContent = null,
                content = null,

                deltaPatch =
                    patch,

                storageType =
                    VersionEntity.STORAGE_DELTA,

                description =
                    description,

                storedFileName = fileName
            )
        )
    }

    /**
     * Create the first version of a new branch.
     *
     * IMPORTANT:
     *
     * No full snapshot is created here.
     *
     * The new branch version points to the exact
     * version it was branched from.
     */
    suspend fun createBranchVersion(
        fileId: Long,
        fileName: String,
        parentVersionId: Long,
        content: String,
        description: String,
        branchName: String
    ): Long? {

        if (branchName.isBlank()) {
            return null
        }

        /*
         * Prevent duplicate branch creation.
         */
        val existingBranch =
            dao.getLatestVersion(
                fileId,
                branchName
            )

        if (existingBranch != null) {
            return null
        }

        /*
         * Parent must exist.
         */
        val parent =
            dao.getVersionById(
                parentVersionId
            )
                ?: return null

        /*
         * Prevent cross-file parent linking.
         */
        if (parent.fileId != fileId) {
            return null
        }

        val parentContent =
            resolveVersion(
                parentVersionId
            )
                ?: return null

        /*
         * Usually the branch is created with exactly
         * the same content as the parent.
         *
         * That produces an empty/no-op delta instead
         * of duplicating the full file.
         */
        val patch =
            diffManager.createPatch(
                parentContent,
                content
            )

        return dao.insertVersion(
            VersionEntity(
                fileId = fileId,

                versionNumber = 1,

                parentVersionId =
                    parentVersionId,

                branchName =
                    branchName,

                snapshotContent = null,
                content = null,

                deltaPatch =
                    patch,

                storageType =
                    VersionEntity.STORAGE_DELTA,

                description =
                    description,

                storedFileName = fileName
            )
        )
    }
}
