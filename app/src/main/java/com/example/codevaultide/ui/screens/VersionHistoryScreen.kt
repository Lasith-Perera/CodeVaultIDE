package com.example.codevaultide.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codevaultide.editor.Snapshot
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryScreen(

    snapshots: List<Snapshot>,

    onBackClick: () -> Unit,

    onRestoreClick: (Snapshot) -> Unit

) {


    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Version History")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ){

                        Text("←")

                    }

                }

            )

        }


    ){ padding ->


        if(snapshots.isEmpty()){


            Box(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),

                contentAlignment =
                    androidx.compose.ui.Alignment.Center

            ){

                Text(
                    "No snapshots available"
                )

            }


        }
        else{


            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)

            ){


                items(snapshots){ snapshot ->


                    Card(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)

                    ){


                        Column(

                            modifier =
                                Modifier.padding(16.dp)

                        ){


                            Text(

                                text = snapshot.fileName,

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium

                            )


                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )


                            Text(
                                text =
                                    snapshot.message,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )


                            Text(

                                text =
                                    formatTime(
                                        snapshot.timestamp
                                    ),

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall

                            )


                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )


                            Button(

                                onClick = {

                                    onRestoreClick(snapshot)

                                }

                            ){

                                Text("Restore")

                            }


                        }

                    }


                }


            }


        }


    }

}



private fun formatTime(
    time: Long
): String {


    return SimpleDateFormat(

        "dd MMM yyyy HH:mm",

        Locale.getDefault()

    ).format(
        Date(time)
    )

}