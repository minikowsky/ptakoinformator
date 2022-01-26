package com.example.ptakoinformator.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.data.BirdDao
import com.example.ptakoinformator.data.BirdDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class HistoryViewModel(application: Application): AndroidViewModel(application) {
    private val birdDao: BirdDao = BirdDatabase.getInstance(application).birdDao

    val birds: LiveData<List<Bird>> = birdDao.getAll()

    fun deleteBird(bird: Bird) {
        viewModelScope.launch(Dispatchers.IO) {
            birdDao.delete(bird)
        }
    }
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