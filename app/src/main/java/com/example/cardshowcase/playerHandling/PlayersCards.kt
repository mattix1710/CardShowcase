package com.example.cardshowcase.playerHandling

import android.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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

    protected var selectedCards = SelectedCardsStruct()
    protected var playerCards = ArrayList<CardItem>()

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

    /**
     * function used to play selected cards from players hand
     * - checks if selected cards are properly chosen
     * - if correct -> cardManager will take care of the cards
     * - if incorrect -> there will be an AlertDialog displayed
     * **/
    fun playCards(displayedCard: ImageView, displayedCardInfo: TextView): Boolean{
        var isGood: Boolean = false

        if(selectedCards.list.size == 1){
            var card = playerCards[selectedCards.list[0]]
            if(cardManager.displayedCard.getCardValue() == card.getCardValue()
                || cardManager.displayedCard.getCardType() == card.getCardType()){
                cardManager.managePlayingCards(selectedCards.list, playerCards,
                    displayedCard, displayedCardInfo)
                resetSelectedCards()
                isGood = true
            } else{
                wrongCardAlertDialog()
                isGood = false
            }
        } else if(selectedCards.selectionType == SelectedCardsStruct.SelectionType.value){
            // if selected cards have the same value as displayed card - i.e. stacking the same value
            cardManager.managePlayingCards(selectedCards.list, playerCards, displayedCard, displayedCardInfo)
            resetSelectedCards()
            isGood = true
        } else if(selectedCards.selectionType == SelectedCardsStruct.SelectionType.house){
            // if selected cards base on the house type of the displayed card
            var matchesHouseType: Boolean = false
            var wrongOnTop: Boolean = false

            for(it in selectedCards.list){
                if(playerCards[it].getCardType() == cardManager.displayedCard.getCardType())
                    matchesHouseType = true
                if(playerCards[it].isSelectedOnTop()){
                    if (playerCards[it].getCardType() == cardManager.displayedCard.getCardType()){
                        wrongOnTop = true
                        break
                    }
                }
            }

            if(wrongOnTop || !matchesHouseType){
                wrongCardAlertDialog(wrongOnTop, matchesHouseType)
                isGood = false
            } else{
                cardManager.managePlayingCards(selectedCards.list, playerCards, displayedCard, displayedCardInfo)
                resetSelectedCards()
                isGood = true
            }
        }

        return isGood
    }

    private fun wrongCardAlertDialog(wrongOnTop: Boolean = false, matchesHouseType: Boolean = false){
        val alert = AlertDialog.Builder(context)
        var cardOnStack =  cardManager.displayedCard

        if(wrongOnTop && matchesHouseType){
            alert.setMessage("Wrong card was chosen to display!")
            alert.setPositiveButton("Choose the other card", null)
        } else if(wrongOnTop && !matchesHouseType){
            alert.setMessage("This set of cards cannot be played! You need to select 1 card of ${cardOnStack.getCardType()}.")
            alert.setPositiveButton("Choose the other card", null)
        } else {
            alert.setMessage("This card can't be played!\nTry ${cardOnStack.getCardValueName()}s or ${cardOnStack.getCardType()}")
            alert.setPositiveButton("OK", null)
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
        alert.setPositiveButton("OK", null)
        alert.create().show()
    }

    fun playCardsButtonUpdate(playCardButton: Button){
        when(selectedCards.list.size){
            0 -> {
                playCardButton.isEnabled = false
                playCardButton.text = "Play a card"}
            1 -> {
                playCardButton.isEnabled = true
                playCardButton.text = "Play a card"}
            2 -> {
                if(selectedCards.twosPlaying())
                    playCardButton.text = "Play 2 cards"
                else
                    playCardButton.isEnabled = false }
            3 -> {
                playCardButton.isEnabled = true
                playCardButton.text = "Play 3 cards"}
            4 -> {
                playCardButton.isEnabled = true
                playCardButton.text = "Play 4 cards"}
            else -> tooManySelected(playCardButton)
        }
    }

    private fun tooManySelected(playCardButton: Button){
        playCardButton.isEnabled = false
    }

}