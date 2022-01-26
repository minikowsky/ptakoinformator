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
        holder.classifiedBirdView.setFirstResult(current?.classification?.mainClassification, (current?.classification?.mainProbability?.times(
            100
        )))
        holder.classifiedBirdView.setSecondResult(current?.classification?.secondClassification, (current?.classification?.secondProbability?.times(
            100
        )))
        holder.classifiedBirdView.setThirdResult(current?.classification?.thirdClassification, (current?.classification?.thirdProbability?.times(
            100
        )))

    }



    override fun getItemCount(): Int = birds.value?.size?:0
}