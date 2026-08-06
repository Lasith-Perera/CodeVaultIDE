package com.example.codevaultide.editor

<<<<<<< HEAD

import androidx.lifecycle.ViewModel
import com.example.codevaultide.database.VersionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class EditorViewModel : ViewModel(){



    private val _code =
        MutableStateFlow(
            """
package com.example.codevault


fun main(){

    println("Hello CodeVault IDE")

}

""".trimIndent()
        )



    val code:StateFlow<String>
            = _code



    private val undoStack =
        ArrayDeque<String>()



    private val redoStack =
        ArrayDeque<String>()



    fun updateCode(
        newCode:String
    ){


        if(newCode != _code.value){


            undoStack.addLast(
                _code.value
            )


            _code.value =
                newCode


            redoStack.clear()


        }


    }




    fun undo(){


        if(undoStack.isNotEmpty()){


            redoStack.addLast(
                _code.value
            )


            _code.value =
                undoStack.removeLast()


        }


    }





    fun redo(){


        if(redoStack.isNotEmpty()){


            undoStack.addLast(
                _code.value
            )


            _code.value =
                redoStack.removeLast()


        }


    }





    fun setCode(
        value:String
    ){

        _code.value=value

    }



    fun rollbackToFileVersion(
        version: VersionEntity
    ){

        updateCode(
            version.deltaText
        )

    }



=======
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditorViewModel : ViewModel() {
    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _fileName = MutableStateFlow("Main.kt")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _fileId = MutableStateFlow<Long?>(null)
    val fileId: StateFlow<Long?> = _fileId.asStateFlow()

    fun setFileId(id: Long?) {
        _fileId.value = id
    }

    fun loadFile(id: Long?, name: String, content: String) {
        _fileId.value = id
        _fileName.value = name
        _code.value = content
    }

    fun updateCode(newCode: String) {
        _code.value = newCode
    }

    fun setCode(newCode: String) {
        _code.value = newCode
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    // Rollback functionality for HistoryScreen
    fun rollbackToFileVersion(versionContent: String) {
        _code.value = versionContent
    }

    fun undo() { /* Undo Logic */ }
    fun redo() { /* Redo Logic */ }
>>>>>>> origin/main
}