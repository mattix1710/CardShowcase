package com.example.cardshowcase.cardsHandling

import android.view.View

interface CardListListener {
    fun onItemClick(position: Int, view: View)
    fun onItemLongClick(position: Int, view: View)
}