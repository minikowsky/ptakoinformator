package com.example.ptakoinformator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.data.BirdDao
import com.example.ptakoinformator.data.BirdDatabase
import java.lang.IllegalArgumentException

class HistoryViewModel(application: Application): AndroidViewModel(application) {
    private val birdDao: BirdDao = BirdDatabase.getInstance(application).birdDao

    val birds: LiveData<List<Bird>> = birdDao.getAll()
}

class HistoryViewModelFactory(private val application: Application)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}