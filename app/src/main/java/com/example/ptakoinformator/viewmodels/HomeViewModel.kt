package com.example.ptakoinformator.viewmodels

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.ptakoinformator.TFLiteModelManager
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel() : ViewModel(){

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
            return  getTop3Results(outputs.probabilityAsCategoryList)
        }
        else {
            @Suppress("DEPRECATION")
            var bitmap=MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val model = TFLiteModelManager.getInstance(context)
            val outputs = model.process(TensorImage.fromBitmap(bitmap))
            return  getTop3Results(outputs.probabilityAsCategoryList)
        }

    }

    private fun getTop3Results(result:MutableList<Category>): List<Category> {
        val sortedResults=result.sortedByDescending { it.score }
        return sortedResults.take(3)
    }



}