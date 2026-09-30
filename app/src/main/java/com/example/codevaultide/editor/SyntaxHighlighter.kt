package com.example.codevaultide.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString


object SyntaxHighlighter {


    private val keywords = setOf(
        "package",
        "import",

        "class",
        "object",
        "interface",

        "fun",
        "val",
        "var",

        "return",

        "if",
        "else",
        "when",

        "for",
        "while",
        "do",

        "in",
        "is",
        "as",

        "override",

        "private",
        "public",
        "protected",
        "internal",

        "data",
        "sealed",
        "enum",

        "open",
        "abstract",

        "suspend",

        "true",
        "false",
        "null"
    )


    fun highlight(
        code: String
    ): AnnotatedString {


        return buildAnnotatedString {


            var index = 0


            while(index < code.length){


                val current = code[index]


                /*
                 * Comments
                 */
                if(
                    current == '/' &&
                    index + 1 < code.length &&
                    code[index + 1] == '/'
                ){


                    val end =
                        code.indexOf(
                            '\n',
                            index
                        ).let{

                            if(it == -1)
                                code.length
                            else
                                it
                        }


                    pushStyle(
                        SpanStyle(
                            color = Color.Gray
                        )
                    )

                    append(
                        code.substring(
                            index,
                            end
                        )
                    )

                    pop()


                    index = end

                    continue
                }



                /*
                 * Strings
                 */
                if(current == '"'){


                    var end = index + 1


                    while(
                        end < code.length &&
                        code[end] != '"'
                    ){

                        end++

                    }


                    if(end < code.length){
                        end++
                    }



                    pushStyle(
                        SpanStyle(
                            color = Color.Green
                        )
                    )


                    append(
                        code.substring(
                            index,
                            end
                        )
                    )


                    pop()


                    index = end

                    continue
                }



                /*
                 * Annotation
                 */
                if(current == '@'){


                    var end = index + 1


                    while(
                        end < code.length &&
                        (
                                code[end].isLetterOrDigit()
                                        ||
                                        code[end] == '_'
                                )
                    ){

                        end++

                    }



                    pushStyle(
                        SpanStyle(
                            color = Color.Magenta
                        )
                    )


                    append(
                        code.substring(
                            index,
                            end
                        )
                    )


                    pop()


                    index = end

                    continue

                }



                /*
                 * Numbers
                 */
                if(current.isDigit()){


                    var end=index


                    while(
                        end < code.length &&
                        code[end].isDigit()
                    ){

                        end++

                    }


                    pushStyle(
                        SpanStyle(
                            color = Color(0xFFFFB74D)
                        )
                    )


                    append(
                        code.substring(
                            index,
                            end
                        )
                    )


                    pop()


                    index=end

                    continue
                }



                /*
                 * Words
                 */
                if(
                    current.isLetter()
                    ||
                    current=='_'
                ){


                    var end=index


                    while(
                        end < code.length &&
                        (
                                code[end].isLetterOrDigit()
                                        ||
                                        code[end]=='_'
                                )
                    ){

                        end++

                    }



                    val word =
                        code.substring(
                            index,
                            end
                        )


                    if(
                        keywords.contains(word)
                    ){


                        pushStyle(
                            SpanStyle(
                                color = Color.Cyan
                            )
                        )


                        append(word)


                        pop()


                    }
                    else{


                        append(word)

                    }



                    index=end

                    continue

                }



                /*
                 * Normal characters
                 */
                append(current)

                index++

            }


        }


    }


}