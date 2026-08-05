package com.example.codevaultide.editor


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun CodeEditor(

    modifier: Modifier = Modifier,

    code:String,

    onCodeChange:(String)->Unit

){



    Row(

        modifier = modifier

    ){


        Column(

            modifier =
                Modifier
                    .width(45.dp)

        ){


            repeat(
                code.lines().size
            ){index->


                Text(
                    text="${index+1}",
                    color=Color.Gray,
                    modifier=
                        Modifier.padding(
                            start=8.dp
                        )
                )


            }


        }



        BasicTextField(

            value = code,


            onValueChange =
                onCodeChange,


            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),



            textStyle =
                TextStyle(

                    color=Color.White,

                    fontFamily =
                        FontFamily.Monospace,

                    fontSize =
                        15.sp

                )

        )


    }



}