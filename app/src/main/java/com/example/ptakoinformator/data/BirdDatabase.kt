package com.example.ptakoinformator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Bird::class], version = 1, exportSchema = false)
abstract class BirdDatabase: RoomDatabase() {
    abstract val birdDao: BirdDao

    companion object{
        @Volatile
        private var INSTANCE: BirdDatabase? = null

        fun getInstance(context: Context): BirdDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BirdDatabase::class.java,
                        "bird_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}