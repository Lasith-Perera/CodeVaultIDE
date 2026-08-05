package com.example.codevaultide.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codevaultide.editor.EditorViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onBackClick: () -> Unit = {},
    onFileClick: () -> Unit = {},
    editorViewModel: EditorViewModel
){


    Scaffold(

        topBar = {

            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text("Files")
                }

            )

        }

    ){padding ->


        Column(

            modifier =
                Modifier
                    .padding(padding)
                    .padding(20.dp)

        ){


            FileItem("Main.kt") {
                editorViewModel.updateCode("// Opened Main.kt\npackage com.example.codevault\n\nfun main() {\n    println(\"Main file\")\n}")
                onFileClick()
            }

            FileItem("Login.kt") {
                editorViewModel.updateCode("// Opened Login.kt\nclass Login {\n    fun authenticate() { }\n}")
                onFileClick()
            }

            FileItem("README.md") {
                editorViewModel.updateCode("# CodeVault IDE\n\nThis is a local IDE with version control.")
                onFileClick()
            }


        }


    }


}



@Composable
fun FileItem(name:String, onClick: () -> Unit = {}){


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        onClick = onClick

    ){


        Row(

            modifier =
                Modifier.padding(20.dp)

        ){


            Icon(
                Icons.Default.Description,
                null
            )


            Spacer(
                Modifier.width(15.dp)
            )


            Text(name)


        }


    }


}