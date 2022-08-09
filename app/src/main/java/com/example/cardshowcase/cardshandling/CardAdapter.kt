package com.example.cardshowcase.cardshandling

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
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

        //creating borders of selected images
        /*view.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                view!!.background = ContextCompat.getDrawable(context, R.drawable.card_background_highlight)
                var scale: Float = context.resources.displayMetrics.density
                view!!.setPadding((3 * scale + 0.5f).toInt())
                //Toast.makeText(context, "Card selected", Toast.LENGTH_SHORT).show()
            }
        })*/

        /*view.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(view: View?): Boolean {
                view!!.background = ContextCompat.getDrawable(context, R.drawable.card_background_transparent)
                view!!.setPadding(0)
                Toast.makeText(context, "Card unselected", Toast.LENGTH_SHORT).show()
                return true
            }
        })*/

        return view
    }

}