package com.example.cardshowcase.cardshandling

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.cardshowcase.R

class CardAdapter(var context: Context, var arrayList: ArrayList<CardItem>) : BaseAdapter() {
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view: View = View.inflate(context, R.layout.single_card, null)

        var cards: ImageView = view.findViewById(R.id.single_card)

        var cardItem: CardItem = arrayList.get(position)

        cards.setImageResource(cardItem.getCardId()!!)

        return view
    }

}