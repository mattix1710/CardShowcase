package com.example.cardshowcase.players

import com.example.cardshowcase.cardshandling.CardItem
import com.example.cardshowcase.cardshandling.CardValue

/*class SelectedCardsStruct{
    enum class SelectionType{
        one, house, value
    }
    val MAX_SELECTED = 4
    private var if2sPlay: Boolean = true

    var list = ArrayList<Int>()
    var selectionType: SelectionType = SelectedCardsStruct.SelectionType.one
    var selectionValue: CardValue = CardValue.none

    fun twosPlaying(): Boolean{
        return if2sPlay
    }
}*/

/**
 * Class representing an instance of a Player
 */
class Player(playerName: String, playerNumber: Int) {
    val uniquePlayerID: Int = 123
    var playerCards = ArrayList<CardItem>()
    var playerName: String = "Player"
    var playerNumber: Int = 0
}