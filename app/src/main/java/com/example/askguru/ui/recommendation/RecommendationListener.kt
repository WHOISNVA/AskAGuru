package com.example.askguru.ui.recommendation

import com.example.askguru.viewmodel.home.Recommendation

interface RecommendationListener {

    fun onClickAccepted(list: Recommendation, position: Int)
    fun onClickRejected(list: Recommendation, position: Int)
    fun onItemClick(list: Recommendation, position: Int)

}