package com.example.ptakoinformator

import android.content.Context
import com.example.ptakoinformator.ml.Model10epok

class TFLiteModelManager {
    companion object{
        @Volatile
        private var INSTANCE: Model10epok? = null

        fun getInstance(context: Context): Model10epok{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance = Model10epok.newInstance(context)
                    INSTANCE = instance
                }
                return instance
            }
        }

        fun releaseModel(){
            INSTANCE?.close()
        }
    }

    // TODO: Remove the comments below
    // Example of running model inference and getting result.
    // val model = TFLiteModelManager.getInstance(context)
    // val outputs = model.process(TensorImage.fromBitmap(plik_zdjęcia))
    // val probability = outputs.probabilityAsCategoryList
}