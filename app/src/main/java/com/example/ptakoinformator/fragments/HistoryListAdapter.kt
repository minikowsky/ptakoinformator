package com.example.ptakoinformator.fragments

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.ptakoinformator.R
import com.example.ptakoinformator.customview.ClassifiedBirdView
import com.example.ptakoinformator.data.Bird
import com.example.ptakoinformator.data.Classification
import kotlin.math.floor

class HistoryListAdapter(private val birds: LiveData<List<Bird>>)
    :RecyclerView.Adapter<HistoryListAdapter.Holder>() {
        inner class Holder(itemView: View) :RecyclerView.ViewHolder(itemView) {
            val classifiedBirdView: ClassifiedBirdView = itemView.findViewById(R.id.history_list_item)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item,parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = birds.value?.get(position)
        holder.classifiedBirdView.setPhoto(current?.photoUri)
        holder.classifiedBirdView.setDate(current?.date)

        val firstProb = current?.classification?.mainProbability
        val secondProb = current?.classification?.secondProbability
        val thirdProb = current?.classification?.thirdProbability

        holder.classifiedBirdView.setFirstResult(current?.classification?.mainClassification,
            floor(firstProb!! * 100 / 20) + 1)
        holder.classifiedBirdView.setSecondResult(current.classification.secondClassification,
            floor(secondProb!! * 100 / 20) + 1)
        holder.classifiedBirdView.setThirdResult(current.classification.thirdClassification,
            floor(thirdProb!! * 100 / 20) + 1)
    }



    override fun getItemCount(): Int = birds.value?.size?:0
}