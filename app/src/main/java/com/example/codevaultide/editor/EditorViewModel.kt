package com.example.codevaultide.editor


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



}