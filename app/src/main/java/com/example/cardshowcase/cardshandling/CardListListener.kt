package com.example.cardshowcase.cardshandling

import android.view.View

interface CardListListener {
    fun onItemClick(position: Int, view: View)
    fun onItemLongClick(position: Int, view: View)
}