package com.example.ptakoinformator.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "birds")
data class Bird(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "location")
    val location: String,
    @Embedded
    val classification: Classification
)
