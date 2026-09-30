<<<<<<< HEAD
package com.example.codevaultide.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codevaultide.database.AppDatabase
import com.example.codevaultide.database.FileEntity
import com.example.codevaultide.database.VersionEntity
import com.example.codevaultide.database.VersionWithFile
import com.example.codevaultide.versioncontrol.VersionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val versionDao = db.versionDao()
    private val fileDao = db.fileDao()

    private val repository = VersionRepository(versionDao)

    private val versionMutex = Mutex()

    // Undo / Redo

    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    private var lastCapturedCode = ""

    // Editor State

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _externalRevision = MutableStateFlow(0)
    val externalRevision: StateFlow<Int> =
        _externalRevision.asStateFlow()

    private val _fileName =
        MutableStateFlow("Main.kt")

    val fileName: StateFlow<String> =
        _fileName.asStateFlow()

    private val _fileId =
        MutableStateFlow<Long?>(null)

    val fileId: StateFlow<Long?> =
        _fileId.asStateFlow()

    private val _isReadOnly =
        MutableStateFlow(false)

    val isReadOnly: StateFlow<Boolean> =
        _isReadOnly.asStateFlow()

    private val _cursorPosition =
        MutableStateFlow(0)

    val cursorPosition: StateFlow<Int> =
        _cursorPosition.asStateFlow()

    // Branch State
    private val _currentBranch =
        MutableStateFlow("main")

    val currentBranch: StateFlow<String> =
        _currentBranch.asStateFlow()

    private val _branches =
        MutableStateFlow<List<String>>(
            listOf("main")
        )

    val branches: StateFlow<List<String>> =
        _branches.asStateFlow()

    // Selected Version

    private val _selectedVersionContent =
        MutableStateFlow<String?>(null)

    val selectedVersionContent:
            StateFlow<String?> =
        _selectedVersionContent.asStateFlow()

    // All Version History

    val allVersionHistory:
            StateFlow<List<VersionWithFile>> =
        versionDao
            .getAllVersionsWithFileName()
            .stateIn(
                viewModelScope,
                SharingStarted
                    .WhileSubscribed(5_000),
                emptyList()
            )

    // Current File Version History

    val versionHistory:
            StateFlow<List<VersionEntity>> =
        _fileId
            .flatMapLatest { id ->

                if (id == null) {

                    flowOf(emptyList())

                } else {

                    versionDao
                        .getVersionsForFile(id)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted
                    .WhileSubscribed(5_000),
                emptyList()
            )
    // FILE TYPE

    fun isMarkdownFile(): Boolean {

        val name =
            _fileName.value.lowercase()

        return name.endsWith(".md") ||
                name.endsWith(".markdown")
    }

    // FILE ID
    fun setFileId(id: Long?) {
        _fileId.value = id
        refreshBranches(id)
    }

    // LOAD FILE

    fun loadFile(
        id: Long?,
        name: String,
        content: String,
        createInitialBackup: Boolean = true
    ) {

        _fileId.value = id
        _fileName.value = name
        _code.value = content

        _externalRevision.value++

        _currentBranch.value =
            "main"

        undoStack.clear()
        redoStack.clear()

        lastCapturedCode =
            content

        if (id == null) {

            _branches.value =
                listOf("main")

            _isReadOnly.value =
                false

            return
        }

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val file =
                fileDao.getFileById(id)

            _isReadOnly.value =
                file?.isReadOnly ?: false

            val latest =
                versionDao
                    .getLatestVersion(
                        id,
                        "main"
                    )
                    ?: versionDao
                        .getLatestVersionAnyBranch(
                            id
                        )

            if (latest == null) {

                if (createInitialBackup) {

                    repository.createVersion(
                        fileId = id,
                        fileName = name,
                        content = content,
                        description = "Initial Version",
                        branchName = "main"
                    )
                }

                _currentBranch.value =
                    "main"

            } else {

                _currentBranch.value =
                    latest.branchName
            }

            refreshBranchesInternal(id)
        }
    }
    // UPDATE CODE

    fun updateCode(
        newCode: String
    ) {

        if (_code.value == newCode) {
            return
        }

        redoStack.clear()

        _code.value =
            newCode
    }

    // TAKE SNAPSHOT

    fun takeSnapshot() {

        if (
            lastCapturedCode !=
            _code.value
        ) {

            undoStack.add(
                lastCapturedCode
            )

            if (
                undoStack.size > 50
            ) {
                undoStack.removeAt(0)
            }

            lastCapturedCode =
                _code.value
        }
    }

    // SET CODE

    fun setCode(
        newCode: String
    ) {

        if (_code.value == newCode) {
            return
        }

        _code.value = newCode
        lastCapturedCode = newCode

        // Force the editor TextFieldValue to refresh for programmatic
        // changes such as crash-recovery restore.
        _externalRevision.value++
    }

    // SET FILE NAME

    fun setFileName(
        name: String
    ) {

        _fileName.value =
            name
    }

    // CURSOR

    fun updateCursorPosition(
        position: Int
    ) {

        _cursorPosition.value =
            position.coerceAtLeast(0)
    }

    // AI GENERATED CODE

    fun applyAiGeneratedCode(
        generatedCode: String
    ) {

        _code.value =
            generatedCode

        lastCapturedCode =
            generatedCode
    }

    // INSERT AI CODE AT CURSOR

    fun insertAiCodeAtCursor(
        aiSnippet: String
    ) {

        val currentText =
            _code.value

        val position =
            _cursorPosition.value
                .coerceIn(
                    0,
                    currentText.length
                )

        val newText =
            currentText.substring(
                0,
                position
            ) +
                    "\n" +
                    aiSnippet +
                    "\n" +
                    currentText.substring(
                        position
                    )

        _code.value =
            newText

        _cursorPosition.value =
            position +
                    aiSnippet.length +
                    2

        lastCapturedCode =
            newText
    }

    // SAVE VERSION SNAPSHOT

    fun saveVersionSnapshot(
        summary: String =
            "Manual Save"
    ) {

        saveCurrentFileSnapshot(
            name =
                _fileName.value,

            content =
                _code.value,

            summary =
                summary
        )
    }

    // SAVE CURRENT FILE

    fun saveCurrentFileSnapshot(
        name: String,
        content: String,
        summary: String =
            "Manual Save"
    ) {

        _isSaving.value =
            true

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            versionMutex.withLock {

                var id =
                    _fileId.value

                val context =
                    getApplication<Application>()

                val now =
                    System.currentTimeMillis()

                val cleanName =
                    name.ifBlank {
                        "Main.kt"
                    }

                // Create new file if needed

                if (id == null) {

                    id =
                        now

                    fileDao.insertFile(
                        FileEntity(
                            id = id,
                            name =
                                cleanName,
                            path =
                                "/workspace/$cleanName",
                            content =
                                content,
                            lastModified =
                                now,
                            isReadOnly =
                                _isReadOnly.value
                        )
                    )

                    _fileId.value =
                        id

                    _fileName.value =
                        cleanName

                } else {

                    val existing =
                        fileDao.getFileById(
                            id
                        )

                    if (existing == null) {

                        fileDao.insertFile(
                            FileEntity(
                                id =
                                    id,
                                name =
                                    cleanName,
                                path =
                                    "/workspace/$cleanName",
                                content =
                                    content,
                                lastModified =
                                    now,
                                isReadOnly =
                                    _isReadOnly.value
                            )
                        )

                    } else {

                        fileDao.updateFile(
                            existing.copy(
                                name =
                                    cleanName,

                                content =
                                    content,

                                path =
                                    existing.path
                                        .ifBlank {
                                            "/workspace/$cleanName"
                                        },

                                lastModified =
                                    now,

                                isReadOnly =
                                    _isReadOnly.value
                            )
                        )
                    }
                }

                val fileId =
                    id
                        ?: return@withLock

                // Physical file save

                try {

                    val workspaceDir =
                        java.io.File(
                            context.filesDir,
                            "workspace"
                        )

                    if (
                        !workspaceDir.exists()
                    ) {
                        workspaceDir.mkdirs()
                    }

                    val physicalFile =
                        java.io.File(
                            workspaceDir,
                            cleanName
                        )

                    physicalFile.writeText(
                        content
                    )

                } catch (_: Exception) {
                    // Database copy is still retained.
                }

                // Create version

                repository.createVersion(
                    fileId =
                        fileId,

                    fileName =
                        cleanName,

                    content =
                        content,

                    description =
                        summary,

                    branchName =
                        _currentBranch.value
                )

                refreshBranchesInternal(
                    fileId
                )

                _isSaving.value =
                    false
            }
        }
    }

    // LOAD VERSION CONTENT

    fun loadVersionContent(
        versionId: Long
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            _selectedVersionContent.value =
                repository
                    .resolveVersion(
                        versionId
                    )
        }
    }
    // GET VERSION CONTENT

    fun getVersionContent(
        versionId: Long,
        callback: (String?) -> Unit
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val content =
                repository
                    .resolveVersion(
                        versionId
                    )

            callback(content)
        }
    }
    // ROLLBACK VERSION

    fun rollbackToFileVersion(
        version: VersionEntity
    ) {

        val id = version.fileId

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            versionMutex.withLock {

                val restored =
                    repository
                        .resolveVersion(
                            version.id
                        )
                        ?: return@withLock

                val existingFile =
                    fileDao.getFileById(id)

                val fileName =
                    existingFile?.name
                        ?: version.storedFileName
                        ?: "Restored_File"

                // Update editor state to the restored file/version
                _fileId.value = id
                _fileName.value = fileName
                _code.value = restored

                _currentBranch.value =
                    version.branchName

                lastCapturedCode =
                    restored

                _externalRevision.value++

                val now =
                    System.currentTimeMillis()

                if (existingFile != null) {

                    fileDao.updateFile(
                        existingFile.copy(
                            content =
                                restored,

                            lastModified =
                                now
                        )
                    )

                } else {

                    // Re-create the file entity if it was deleted
                    fileDao.insertFile(
                        FileEntity(
                            id = id,
                            name = fileName,
                            path = "/workspace/$fileName",
                            content = restored,
                            lastModified = now,
                            isReadOnly = false
                        )
                    )
                }

                repository.createVersion(
                    fileId =
                        id,

                    fileName =
                        fileName,

                    content =
                        restored,

                    description =
                        "Restore v${version.versionNumber}",

                    branchName =
                        version.branchName
                )

                // Clear temporary crash cache for this file on restore
                try {
                    val context = getApplication<Application>()
                    val cacheFile = java.io.File(
                        context.cacheDir,
                        "editor_cache_${id ?: "new"}.tmp"
                    )
                    if (cacheFile.exists()) {
                        cacheFile.delete()
                    }
                } catch (_: Exception) {
                }

                undoStack.clear()
                redoStack.clear()
            }
        }
    }
    // CREATE BRANCH

    fun createBranch(
        name: String
    ) {

        val cleanName =
            name.trim()

        val id =
            _fileId.value
                ?: return

        if (
            cleanName.isBlank() ||
            cleanName ==
            _currentBranch.value
        ) {
            return
        }

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            versionMutex.withLock {

                if (
                    cleanName in
                    versionDao
                        .getBranches(id)
                ) {
                    return@withLock
                }

                val current =
                    versionDao
                        .getLatestVersion(
                            id,
                            _currentBranch.value
                        )
                        ?: versionDao
                            .getLatestVersionAnyBranch(
                                id
                            )
                        ?: return@withLock

                val currentContent =
                    repository
                        .resolveVersion(
                            current.id
                        )
                        ?: return@withLock

                /* New branch now references the current version as its parent.
                It does NOT save another full copy.
                 */
                repository.createBranchVersion(
                    fileId =
                        id,

                    fileName =
                        _fileName.value,

                    parentVersionId =
                        current.id,

                    content =
                        currentContent,

                    description =
                        "Branch created from " +
                                "${current.branchName} " +
                                "v${current.versionNumber}",

                    branchName =
                        cleanName
                )

                _currentBranch.value =
                    cleanName

                _code.value =
                    currentContent

                lastCapturedCode =
                    currentContent

                _externalRevision.value++

                undoStack.clear()
                redoStack.clear()

                refreshBranchesInternal(
                    id
                )
            }
        }
    }
    // CHECKOUT BRANCH

    fun checkoutBranch(
        name: String
    ) {

        val cleanName =
            name.trim()

        val id =
            _fileId.value
                ?: return

        if (
            cleanName.isBlank()
        ) {
            return
        }

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val latest =
                versionDao
                    .getLatestVersion(
                        id,
                        cleanName
                    )
                    ?: return@launch

            val content =
                repository
                    .resolveVersion(
                        latest.id
                    )
                    ?: return@launch

            _currentBranch.value =
                cleanName

            _code.value =
                content

            lastCapturedCode =
                content

            _externalRevision.value++

            undoStack.clear()
            redoStack.clear()
        }
    }
    // REFRESH BRANCHES

    private fun refreshBranches(
        id: Long?
    ) {

        if (id == null) {

            _branches.value =
                listOf("main")

            return
        }

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            refreshBranchesInternal(
                id
            )
        }
    }

    private suspend fun refreshBranchesInternal(
        id: Long
    ) {

        val branchList =
            versionDao
                .getBranches(id)
                .ifEmpty {
                    listOf("main")
                }

        _branches.value =
            branchList
    }
    // UNDO

    fun undo() {

        if (
            undoStack.isNotEmpty()
        ) {

            val current =
                _code.value

            redoStack.add(
                current
            )

            val previous =
                undoStack.removeAt(
                    undoStack.lastIndex
                )

            _code.value =
                previous

            lastCapturedCode =
                previous

            _externalRevision.value++
        }
    }
    // REDO

    fun redo() {

        if (
            redoStack.isNotEmpty()
        ) {

            val current =
                _code.value

            undoStack.add(
                current
            )

            val next =
                redoStack.removeAt(
                    redoStack.lastIndex
                )

            _code.value =
                next

            lastCapturedCode =
                next

            _externalRevision.value++
        }
    }
    // DELETE VERSION

    fun deleteVersion(
        versionId: Long
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            versionDao
                .deleteVersionById(
                    versionId
                )
        }
    }

    // DELETE VERSION HISTORY

    fun deleteVersionHistory(
        fileId: Long
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            versionDao
                .deleteVersionsForFile(
                    fileId
                )
        }
    }
    // CLEAR EDITOR

    fun clearEditor() {

        updateCode("")
    }
    // READ ONLY

    fun toggleReadOnly() {

        val id =
            _fileId.value
                ?: return

        val newState =
            !_isReadOnly.value

        _isReadOnly.value =
            newState

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            fileDao
                .getFileById(id)
                ?.let { file ->

                    fileDao.updateFile(
                        file.copy(
                            isReadOnly =
                                newState
                        )
                    )
                }
        }
    }
}
=======
package com.example.codevaultide.editor

import androidx.lifecycle.ViewModel
import com.example.codevaultide.ui.screens.FileVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileHistoryItem(
    val fileId: Long,
    val fileName: String,
    val versions: List<FileVersion>
)

class EditorViewModel : ViewModel() {
    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _fileName = MutableStateFlow("Main.kt")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _fileId = MutableStateFlow<Long?>(null)
    val fileId: StateFlow<Long?> = _fileId.asStateFlow()

    private val _cursorPosition = MutableStateFlow(0)
    val cursorPosition: StateFlow<Int> = _cursorPosition.asStateFlow()

    // Map storing version history list keyed by fileId
    private val fileHistoryMap = mutableMapOf<Long, MutableList<FileVersion>>()
    private val fileNameMap = mutableMapOf<Long, String>()

    // Expose all files that have history
    private val _allFilesWithHistory = MutableStateFlow<List<FileHistoryItem>>(emptyList())
    val allFilesWithHistory: StateFlow<List<FileHistoryItem>> = _allFilesWithHistory.asStateFlow()

    // Selected file history for viewing
    private val _selectedFileHistory = MutableStateFlow<FileHistoryItem?>(null)
    val selectedFileHistory: StateFlow<FileHistoryItem?> = _selectedFileHistory.asStateFlow()

    private val _versionHistory = MutableStateFlow<List<FileVersion>>(emptyList())
    val versionHistory: StateFlow<List<FileVersion>> = _versionHistory.asStateFlow()

    fun setFileId(id: Long?) {
        _fileId.value = id
        id?.let { refreshHistoryForFile(it) }
    }

    fun loadFile(id: Long?, name: String, content: String) {
        _fileId.value = id
        _fileName.value = name
        _code.value = content

        if (id != null) {
            fileNameMap[id] = name
            if (!fileHistoryMap.containsKey(id)) {
                fileHistoryMap[id] = mutableListOf(
                    FileVersion(
                        id = System.currentTimeMillis(),
                        timestamp = getCurrentFormattedTime(),
                        summary = "Initial Version",
                        content = content
                    )
                )
            }
            refreshHistoryForFile(id)
            updateAllFilesWithHistory()
        } else {
            _versionHistory.value = emptyList()
        }
    }

    fun selectFileForHistory(fileId: Long) {
        val versions = fileHistoryMap[fileId] ?: emptyList()
        val name = fileNameMap[fileId] ?: "Unknown File"
        _selectedFileHistory.value = FileHistoryItem(fileId, name, versions)
    }

    fun clearSelectedFileHistory() {
        _selectedFileHistory.value = null
    }

    fun updateCode(newCode: String) {
        _code.value = newCode
    }

    fun saveVersionSnapshot(summary: String = "Manual Save") {
        val currentId = _fileId.value ?: return
        val currentContent = _code.value

        val newVersion = FileVersion(
            id = System.currentTimeMillis(),
            timestamp = getCurrentFormattedTime(),
            summary = summary,
            content = currentContent
        )

        val historyList = fileHistoryMap.getOrPut(currentId) { mutableListOf() }
        historyList.add(0, newVersion)
        refreshHistoryForFile(currentId)
        updateAllFilesWithHistory()
    }

    fun rollbackToFileVersion(versionContent: String) {
        _code.value = versionContent
    }

    private fun refreshHistoryForFile(id: Long) {
        val history = fileHistoryMap[id]?.toList() ?: emptyList()
        _versionHistory.value = history
        val name = fileNameMap[id] ?: _fileName.value
        _selectedFileHistory.value = FileHistoryItem(id, name, history)
    }

    private fun updateAllFilesWithHistory() {
        _allFilesWithHistory.value = fileHistoryMap.map { (id, versions) ->
            FileHistoryItem(
                fileId = id,
                fileName = fileNameMap[id] ?: "File $id",
                versions = versions.toList()
            )
        }
    }

    private fun getCurrentFormattedTime(): String {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    fun undo() {}
    fun redo() {}
    fun clearEditor() { _code.value = "" }
}
>>>>>>> origin/main
