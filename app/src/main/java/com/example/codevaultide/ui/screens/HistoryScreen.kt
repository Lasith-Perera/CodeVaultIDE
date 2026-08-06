package com.example.codevaultide.ui.screens

<<<<<<< HEAD

=======
import android.widget.Toast
import androidx.compose.foundation.clickable
>>>>>>> origin/main
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
<<<<<<< HEAD
import androidx.compose.material.icons.filled.Difference
=======
>>>>>>> origin/main
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
<<<<<<< HEAD
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.codevaultide.database.VersionEntity
import com.example.codevaultide.editor.EditorViewModel
import com.example.codevaultide.versioncontrol.VersionHistoryViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

=======
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codevaultide.editor.EditorViewModel

data class FileVersion(
    val id: Long,
    val timestamp: String,
    val summary: String,
    val content: String
)
>>>>>>> origin/main

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
<<<<<<< HEAD

    onBackClick: () -> Unit = {},

    onCompareClick: (VersionEntity) -> Unit = {},

    versionViewModel: VersionHistoryViewModel = viewModel(),

    editorViewModel: EditorViewModel

) {


    val versions by versionViewModel
        .versions
        .collectAsState(initial = emptyList())


    var selectedVersion by remember {
        mutableStateOf<VersionEntity?>(null)
    }



    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "Version History",
                        fontWeight = FontWeight.Bold
                    )

                },


                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ){

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "Back"
                        )

                    }

                }

            )

        }

    ){ padding ->



        if(versions.isEmpty()){


            EmptyHistoryView(
                modifier =
                    Modifier.padding(padding)
            )


        }
        else{


            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ){


                items(
                    versions
                ){ version ->


                    VersionCard(

                        version = version,


                        onRestore = {

                            selectedVersion =
                                version

                        },


                        onCompare = {

                            onCompareClick(
                                version
                            )

                        }

                    )


                }


            }


        }


    }



    if(selectedVersion != null){


        RestoreDialog(

            version =
                selectedVersion!!,


            onConfirm = {


                editorViewModel
                    .rollbackToFileVersion(
                        selectedVersion!!
                    )


                selectedVersion = null

                onBackClick()

            },


            onDismiss = {

                selectedVersion = null

            }

        )


    }


}





@Composable
fun VersionCard(

    version: VersionEntity,

    onRestore: () -> Unit,

    onCompare: () -> Unit

){


    val date =
        SimpleDateFormat(
            "MMM dd, yyyy • HH:mm",
            Locale.getDefault()
        )
            .format(
                Date(version.timestamp)
            )



    Card(

        modifier =
            Modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp)

    ){



        Row(

            modifier =
                Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically

        ){



            Surface(

                shape =
                    RoundedCornerShape(10.dp),

                color =
                    MaterialTheme.colorScheme.primaryContainer

            ){


                Text(

                    text =
                        "v${version.versionNumber}",

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),

                    fontWeight =
                        FontWeight.Bold

                )


            }



            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )



            Column(

                modifier =
                    Modifier.weight(1f)

            ){



                Text(

                    text =
                        version.description
                            .ifBlank {
                                "Snapshot"
                            },

                    fontWeight =
                        FontWeight.Bold

                )



                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )



                Text(

                    text =
                        date,

                    style =
                        MaterialTheme.typography.bodySmall,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant

                )

            }



            IconButton(

                onClick =
                    onCompare

            ){

                Icon(

                    imageVector =
                        Icons.Default.Difference,

                    contentDescription =
                        "Compare"

                )

            }




            IconButton(

                onClick =
                    onRestore

            ){

                Icon(

                    imageVector =
                        Icons.Default.Restore,

                    contentDescription =
                        "Restore"

                )

            }


        }


    }


}





@Composable
fun RestoreDialog(

    version: VersionEntity,

    onConfirm: () -> Unit,

    onDismiss: () -> Unit

){


    AlertDialog(

        onDismissRequest =
            onDismiss,


        title = {

            Text(
                "Restore Version ${version.versionNumber}?"
            )

        },


        text = {

            Text(
                "Current changes will be replaced with this previous version."
            )

        },


        confirmButton = {


            Button(

                onClick =
                    onConfirm

            ){

                Text(
                    "Restore"
                )

            }

        },


        dismissButton = {


            TextButton(

                onClick =
                    onDismiss

            ){

                Text(
                    "Cancel"
                )

            }

        }


    )


}





@Composable
fun EmptyHistoryView(

    modifier: Modifier = Modifier

){


    Box(

        modifier =
            modifier
                .fillMaxSize(),

        contentAlignment =
            Alignment.Center

    ){


        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally

        ){


            Icon(

                imageVector =
                    Icons.Default.History,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(60.dp),

                tint =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant

            )



            Spacer(
                Modifier.height(12.dp)
            )



            Text(
                "No versions created yet"
            )


        }


    }


=======
    editorViewModel: EditorViewModel,
    onBackClick: () -> Unit = {},
    onCompareClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeFileName by editorViewModel.fileName.collectAsState()

    val dummyVersions = listOf(
        FileVersion(1, "10 mins ago", "Added main function", "// Current Version\nfun main() {\n    println(\"Hello World\")\n}"),
        FileVersion(2, "1 hour ago", "Updated logic", "// Version 2\nfun main() {\n    // Initial setup\n}"),
        FileVersion(3, "Yesterday", "Initial Commit", "// Version 1\n")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Column {
                        Text("Version History", fontWeight = FontWeight.Bold)
                        Text(activeFileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyVersions) { version ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(version.summary, fontWeight = FontWeight.Bold)
                                Text(version.timestamp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(
                            onClick = {
                                editorViewModel.rollbackToFileVersion(version.content)
                                Toast.makeText(context, "Restored to ${version.timestamp}", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            }
                        ) {
                            Icon(
                                Icons.Default.Restore,
                                contentDescription = "Restore Version",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
>>>>>>> origin/main
}