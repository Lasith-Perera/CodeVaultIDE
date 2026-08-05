package com.example.codevaultide.versioncontrol



data class Delta(
    val added:String,
    val removed:String
)



object DeltaManager {



    fun createDelta(
        oldText:String,
        newText:String
    ):Delta{


        val oldLines =
            oldText.lines()


        val newLines =
            newText.lines()



        val added =
            newLines
                .filter {
                    !oldLines.contains(it)
                }
                .joinToString("\n")



        val removed =
            oldLines
                .filter {
                    !newLines.contains(it)
                }
                .joinToString("\n")



        return Delta(
            added,
            removed
        )


    }




    fun applyDelta(
        original:String,
        delta:Delta
    ):String{


        var result =
            original



        if(delta.removed.isNotEmpty()){

            result =
                result.replace(
                    delta.removed,
                    ""
                )

        }



        if(delta.added.isNotEmpty()){

            result +=
                "\n" + delta.added

        }


        return result

    }


}