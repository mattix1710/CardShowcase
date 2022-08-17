package com.example.cardshowcase.playerHandling

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.example.cardshowcase.cardsHandling.CardItem
import com.example.cardshowcase.cardsHandling.CardManager
import com.example.cardshowcase.cardsHandling.CardValue

open class PlayersCards(val context: Context, val cardManager: CardManager) {
    class SelectedCardsStruct{
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
    }

    var selectedCards = SelectedCardsStruct()
    var playerCards = ArrayList<CardItem>()

    fun selectedCard(position: Int){
        var card: CardItem = playerCards[position]          // REMEMBER: it's passed by reference

        if(selectedCards.list.size == selectedCards.MAX_SELECTED){
            notifyMaxCardsSelected()
        } else if(selectedCards.list.isEmpty()){                // if there are no cards selected
            card.resetSelected()
            card.setSelectedOnTop()
            selectedCards.list.add(position)
            selectedCards.selectionType = SelectedCardsStruct.SelectionType.one
            selectedCards.selectionValue = card.getCardValue()
            Toast.makeText(context, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Toast.LENGTH_SHORT).show()
        } else if(selectedCards.list.contains(position)){       // if current card was selected - unselect it!
            card.resetSelected()
            selectedCards.list.remove(position)
            if(selectedCards.list.isEmpty()){
                resetSelectedCards()
            }
            Toast.makeText(context, "${card.getCardValueName()} of ${card.getCardType().toString()} was unselected!", Toast.LENGTH_SHORT).show()
        } else if(selectedCards.selectionValue == card.getCardValue()){
            card.resetSelected()
            card.setSelected()
            selectedCards.list.add(position)
            Toast.makeText(context, "${card.getCardValueName()} of ${card.getCardType().toString()} was selected!", Toast.LENGTH_SHORT).show()

            if(card.getCardValue() == cardManager.displayedCard.getCardValue())
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.value
            else
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.house
        } else{
            wrongCardAlertDialog()
        }
    }

    fun selectedCardLong(position: Int){
        var card: CardItem = playerCards[position]

        if(selectedCards.list.size == selectedCards.MAX_SELECTED){
            notifyMaxCardsSelected()
        } else if(selectedCards.list.isEmpty()){                // if there are no cards selected
            card.resetSelected()
            card.setSelectedOnTop()
            selectedCards.list.add(position)
            selectedCards.selectionType = SelectedCardsStruct.SelectionType.one
            selectedCards.selectionValue = card.getCardValue()
            Toast.makeText(context, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Toast.LENGTH_SHORT).show()
        } else if(selectedCards.list.contains(position)){       // if current was selected - select it as ON TOP
            if(!card.isSelectedOnTop()){
                for(played in playerCards){
                    if(played.isSelectedOnTop()){
                        played.resetSelected()
                        played.setSelected()
                    }
                }
                card.resetSelected()
                card.setSelectedOnTop()
                Toast.makeText(context, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Toast.LENGTH_SHORT).show()
            }
        } else if(selectedCards.selectionValue == card.getCardValue()){
            for(played in playerCards){
                if(played.isSelectedOnTop()){
                    played.resetSelected()
                    played.setSelected()
                }
            }
            card.resetSelected()
            card.setSelectedOnTop()
            selectedCards.list.add(position)
            Toast.makeText(context, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Toast.LENGTH_SHORT).show()

            if(card.getCardValue() == cardManager.displayedCard.getCardValue())
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.value
            else
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.house
        } else{
            wrongCardAlertDialog()
        }
    }

    fun playCards(){
        if(selectedCards.list.size == 1){
            var card = playerCards[selectedCards.list[0]]
            if(cardManager.displayedCard.getCardValue() == card.getCardValue()
                || cardManager.displayedCard.getCardType() == card.getCardType()){
                //cardManager.managePlayingCards()
            }
        }
    }

    private fun wrongCardAlertDialog(wrongOnTop: Boolean = false, matchesHouseType: Boolean = false){
        val alert = AlertDialog.Builder(context)
        var cardOnStack =  cardManager.displayedCard

        if(wrongOnTop && matchesHouseType){
            alert.setMessage("Wrong card was chosen to display!")
            alert.setPositiveButton(
                "Choose the other card"
            ){ dialogInterface, i -> /* player will choose new card by himself */ }
        } else if(wrongOnTop && !matchesHouseType){
            alert.setMessage("This set of cards cannot be played! You need to select 1 card of ${cardOnStack.getCardType()}.")
            alert.setPositiveButton(
                "Choose the other card"
            ) { dialogInterface, i -> /* player will choose new card by himself */}
        } else {
            alert.setMessage("This card can't be played!\nTry ${cardOnStack.getCardValueName()}s or ${cardOnStack.getCardType()}")
            alert.setPositiveButton(
                "OK"
            ) { dialogInterface, i ->
                // What to do after clicking "positive" button
            }
        }
        alert.create().show()
    }

    fun resetSelectedCards() {
        selectedCards.list.clear()
        selectedCards.selectionType = SelectedCardsStruct.SelectionType.one
        selectedCards.selectionValue = CardValue.none
    }

    private fun notifyMaxCardsSelected(){
        val alert = AlertDialog.Builder(context)
        alert.setMessage("Maximum number (4) of cards was selected!")
        alert.setPositiveButton(
            "OK"
        ){ dialogInterface, i -> }
        alert.create().show()
    }

}