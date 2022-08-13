package com.example.cardshowcase.cardshandling

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cardshowcase.R

class CardAdapter(var cardList: ArrayList<CardItem>,
                  private val eventListener: CardListListener,
                  val context: Context
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.single_card,
            parent, false)  // false - tells the layoutInflater to don't add this view to recyclerView right now

        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentItem = cardList[position]

        if(currentItem.isSelected()){
            holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.card_background_selected)
        }else if(currentItem.isSelectedOnTop()){
            holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.card_background_selected_on_top)
        }else{
            holder.imageView.background = ContextCompat.getDrawable(context, R.drawable.card_background_transparent)
        }

        holder.imageView.setImageResource(currentItem.getCardId())
        holder.itemView.setOnClickListener{
            eventListener.onItemClick(position, holder.itemView)
        }
        holder.itemView.setOnLongClickListener {
            eventListener.onItemLongClick(position, holder.itemView)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int = cardList.size

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.single_card)
    }
}