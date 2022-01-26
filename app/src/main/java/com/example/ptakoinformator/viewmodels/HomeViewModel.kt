package com.example.ptakoinformator.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.*
import com.example.ptakoinformator.TFLiteModelManager
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.data.BirdDao
import com.example.ptakoinformator.data.BirdDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import java.lang.IllegalArgumentException

class HomeViewModel(application: Application) : AndroidViewModel(application){
    private val birdDao: BirdDao = BirdDatabase.getInstance(application).birdDao
    val lastBird: LiveData<Bird> = birdDao.getLast()

    fun classifyBird(uri: Uri, context: Context): List<Category> {
        if(Build.VERSION.SDK_INT >= 29) {
            val source: ImageDecoder.Source = ImageDecoder.createSource(
                context.contentResolver,
                uri
            )
            var bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val model = TFLiteModelManager.getInstance(context)
            val outputs = model.process(TensorImage.fromBitmap(bitmap))
            return getTop4Results(outputs.probabilityAsCategoryList)
        }
        else {
            @Suppress("DEPRECATION")
            var bitmap=MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val model = TFLiteModelManager.getInstance(context)
            val outputs = model.process(TensorImage.fromBitmap(bitmap))
            return getTop4Results(outputs.probabilityAsCategoryList)
        }

    }

    private fun getTop4Results(result:MutableList<Category>): List<Category> {
        val sortedResults=result.sortedByDescending { it.score }
        return sortedResults.take(4)
    }


    fun createBird(bird: Bird){
        viewModelScope.launch(Dispatchers.IO) {
            birdDao.insert(bird)
        }
    }

}

class HomeViewModelFactory(private val application: Application)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    }