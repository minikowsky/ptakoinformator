package com.example.ptakoinformator.customview

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ptakoinformator.R
class ClassifiedBirdView(context: Context, attrs: AttributeSet?): ConstraintLayout(context, attrs) {
    private val imageView:ImageView
    private val txtViewFirstResult:TextView
    private val txtViewSecondResult:TextView
    private val txtViewThirdResult:TextView
    private val txtViewDate:TextView
    init {
        inflate(context, R.layout.classified_bird_view, this)
        val attributes= context.obtainStyledAttributes(attrs, R.styleable.ClassifiedBirdView)
        imageView= findViewById<ImageView>(R.id.image_view_bird)
        txtViewFirstResult=findViewById<TextView>(R.id.txt_view_first_result)
        txtViewSecondResult=findViewById<TextView>(R.id.txt_view_second_result)
        txtViewThirdResult=findViewById<TextView>(R.id.txt_view_third_result)
        txtViewDate=findViewById<TextView>(R.id.txt_view_date)
        setImage(attributes.getString(R.styleable.ClassifiedBirdView_image))
        setDate(attributes.getString(R.styleable.ClassifiedBirdView_date))
        setFirstResult(attributes.getString(R.styleable.ClassifiedBirdView_firstResult))
        setSecondResult(attributes.getString(R.styleable.ClassifiedBirdView_secondResult))
        setThirdResult(attributes.getString(R.styleable.ClassifiedBirdView_thirdResult))
        attributes.recycle()

    }
    fun setImage(path:String?){
        imageView.setImageBitmap(
            ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(path),64,64))
    }
    fun setDate(date:String?){
        txtViewDate.setText("Data: ${date?:""}")
    }
    fun setFirstResult(result:String?){
        txtViewFirstResult.setText("1: ${result?:""}")
    }
    fun setSecondResult(result:String?){
        txtViewSecondResult.setText("2: ${result?:""}")
    }
    fun setThirdResult(result:String?){
        txtViewThirdResult.setText("3: ${result?:""}")
    }
}

