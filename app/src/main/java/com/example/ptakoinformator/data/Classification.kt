package com.example.ptakoinformator.data

data class Classification(
    val mainProbability: Float,
    val mainClassification: String,
    val secondProbability: Float,
    val secondClassification:String,
    val thirdProbability: Float,
    val thirdClassification:String
)
