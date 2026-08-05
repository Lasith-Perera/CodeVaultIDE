package com.example.codevaultide.recovery


import android.content.Context
import java.io.File



object AutoBackupManager {



    fun saveBackup(
        context:Context,
        text:String
    ){


        val file =
            File(
                context.filesDir,
                "backup.tmp"
            )


        file.writeText(text)


    }



    fun readBackup(
        context:Context
    ):String?{


        val file =
            File(
                context.filesDir,
                "backup.tmp"
            )


        return if(file.exists()){

            file.readText()

        }
        else{

            null

        }


    }



    fun deleteBackup(
        context:Context
    ){

        File(
            context.filesDir,
            "backup.tmp"
        )
            .delete()

    }


}