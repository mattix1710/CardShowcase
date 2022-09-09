package com.example.cardshowcase.playerHandling

import android.content.Context
import com.example.cardshowcase.cardsHandling.CardItem
import com.example.cardshowcase.cardsHandling.CardManager
import com.example.cardshowcase.cardsHandling.CardValue
import com.example.cardshowcase.cardsHandling.HouseType

/**
 * Class representing an instance of a Player
 * - cardsWanted - argument used ONLY for debug purposes
 */
class Player(playerName: String, playerNumber: Int, context: Context, cardManager: CardManager, cardsWanted: ArrayList<CardItem>? = null): PlayersCards(context, cardManager) {
    private var uniquePlayerID: Int = 123
    private var playerName: String = "Player"
    private var playerNumber: Int = 0
    private var awaitingRoundsPenalty: Int = 0

    init {
        this.playerName = playerName
        this.playerNumber = playerNumber
        this.uniquePlayerID = (0..99999).random()

        if(cardsWanted != null)
            this.playerCards = cardManager.debug_randomizeInitCards(4, cardsWanted)
        else
            this.playerCards = cardManager.randomizeInitCards()
    }

    fun getPlayerID(): Int{
        return uniquePlayerID
    }

    fun getName(): String{
        return playerName
    }

    fun getCards(): ArrayList<CardItem>{
        return playerCards
    }

    fun setHaltRoundsPenalty(quantity: Int){
        awaitingRoundsPenalty = quantity
    }

    fun decreaseRoundsPenalty(){
        if(awaitingRoundsPenalty > 1)
            awaitingRoundsPenalty--
    }
}