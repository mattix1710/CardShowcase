package com.example.cardshowcase.playerHandling

import android.content.Context
import com.example.cardshowcase.cardsHandling.CardItem
import com.example.cardshowcase.cardsHandling.CardManager
import com.example.cardshowcase.cardsHandling.CardValue
import com.example.cardshowcase.cardsHandling.HouseType

/**
 * Class representing an instance of a Player
 */
class Player(playerName: String, playerNumber: Int, context: Context, cardManager: CardManager): PlayersCards(context, cardManager) {
    private var uniquePlayerID: Int = 123
    var playerName: String = "Player"
    var playerNumber: Int = 0

    init {
        this.playerName = playerName
        this.playerNumber = playerNumber
        this.uniquePlayerID = (0..99999).random()
    }

    fun initPlayerCards(arrayList: ArrayList<CardItem>){
        playerCards = arrayList
    }

    fun getPlayerID(): Int{
        return uniquePlayerID
    }

}