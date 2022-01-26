package com.example.ptakoinformator.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Size
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ptakoinformator.R
import com.squareup.picasso.Picasso

class ClassifiedBirdView(context: Context, attrs: AttributeSet?): ConstraintLayout(context, attrs) {
    private val imageView:ImageView
    private val txtViewFirstResult:TextView
    private val txtViewSecondResult:TextView
    private val txtViewThirdResult:TextView
    private val txtViewFourthResult:TextView
    private val txtViewDate:TextView
    init {
        inflate(context, R.layout.classified_bird_view, this)
        imageView= findViewById<ImageView>(R.id.image_view_bird)
        txtViewFirstResult=findViewById<TextView>(R.id.txt_view_first_result)
        txtViewSecondResult=findViewById<TextView>(R.id.txt_view_second_result)
        txtViewThirdResult=findViewById<TextView>(R.id.txt_view_third_result)
        txtViewFourthResult=findViewById<TextView>(R.id.txt_view_fourth_result)
        txtViewDate=findViewById<TextView>(R.id.txt_view_date)

    }

    fun setPhoto(string: String?){
        Picasso.get()
            .load("file:$string")
            .resize(200, 200)
            .centerCrop()
            .into(imageView)
    }
    fun setDate(date:String?){
        txtViewDate.text=("Data: ${date}")
    }
    fun setFirstResult(classification: String?, result:Float?){
        txtViewFirstResult.text=("1: ${classification}-${String.format("%.2f", result)}")
    }
    fun setSecondResult(classification: String?, result:Float?){
        txtViewSecondResult.text=("2: ${classification}-${String.format("%.2f", result)}")
    }
    fun setThirdResult(classification: String?, result:Float?){
        txtViewThirdResult.text=("3: ${classification}-${String.format("%.2f", result)}")
    }
    fun setFourthResult(classification: String?, result:Float?){
        txtViewFourthResult.text=("4: ${classification}-${String.format("%.2f", result)}")
    }



}

