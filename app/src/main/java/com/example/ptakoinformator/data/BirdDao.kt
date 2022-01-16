package com.example.ptakoinformator.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds")
    fun getAll(): LiveData<List<Bird>>

    @Query("SELECT * FROM birds ORDER BY id DESC LIMIT 1")
    fun getLast(): LiveData<Bird>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(bird: Bird)

    @Delete
    fun delete(bird: Bird)
}