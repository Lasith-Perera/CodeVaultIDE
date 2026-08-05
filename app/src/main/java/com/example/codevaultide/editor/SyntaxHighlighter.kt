package com.example.codevaultide.editor


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString



object SyntaxHighlighter {


    private val keywords =
        listOf(
            "fun",
            "class",
            "val",
            "var",
            "return",
            "if",
            "else",
            "for",
            "while",
            "import",
            "package"
        )



    fun highlight(
        code:String
    ):AnnotatedString{


        return buildAnnotatedString{


            code.split(" ")
                .forEach{word->


                    if(
                        keywords.contains(
                            word.trim()
                        )
                    ){

                        pushStyle(
                            SpanStyle(
                                color =
                                    Color.Cyan
                            )
                        )

                        append(word)

                        pop()

                    }
                    else{


                        append(word)

                    }


                    append(" ")

                }


        }


    }


}