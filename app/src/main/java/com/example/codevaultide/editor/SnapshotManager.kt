package com.example.codevaultide.editor

object SnapshotManager {

    private val snapshots =
        mutableListOf<Snapshot>()


    fun add(snapshot: Snapshot){
        snapshots.add(snapshot)
    }


    fun getSnapshots(
        fileId: Long
    ): List<Snapshot>{

        return snapshots.filter {
            it.fileId == fileId
        }
            .sortedByDescending {
                it.timestamp
            }

    }


    fun restore(
        id: Long
    ): Snapshot? {

        return snapshots.find {
            it.id == id
        }

    }

}