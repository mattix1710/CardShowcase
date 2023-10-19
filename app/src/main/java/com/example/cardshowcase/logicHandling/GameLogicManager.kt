package com.example.cardshowcase.logicHandling

import android.content.Context
import android.widget.TextView
import com.example.cardshowcase.cardsHandling.CardManager
import com.example.cardshowcase.cardsHandling.Penalty
import com.example.cardshowcase.playerHandling.Player

class GameLogicManager(val context: Context, val cardManager: CardManager, val penaltyInfo: TextView) {

    var currentPenalty = Penalty(context, penaltyInfo)

    init {
        currentPenalty = cardManager.currentPenalty
    }

    fun drawCards(player: Player, ){

    }

    fun penaltyChecker(){
        // TODO: PENALTIES
    }
}