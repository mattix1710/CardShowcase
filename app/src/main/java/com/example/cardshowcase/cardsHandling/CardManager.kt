package com.example.cardshowcase.cardsHandling

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.example.cardshowcase.R
import com.example.cardshowcase.playerHandling.PlayersCards
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.w3c.dom.Text

enum class DemandedTypeSelector{
    none, Jack, Ace, wrongCards
}

class CardManager(val context: Context, val penaltyInfo: TextView){

    var allPlayingCards  = ArrayList<CardItem>()
    var currentFreeCards = ArrayList<CardItem>()
    var usedPlayingCards = ArrayList<CardItem>()
    var displayedCard    = CardItem(R.drawable.card_empty)
    var currentPenalty: Penalty = Penalty(context, penaltyInfo)

    var queenFunctional: Boolean = false
    var allowsPairToPlay: Boolean = true

    init {
        populateCardDeck()

        enableModifiers()

        allPlayingCards = currentFreeCards.clone() as ArrayList<CardItem>
    }

    private fun enableModifiers(){
        if(queenFunctional){
            for(card in currentFreeCards){
                if(card.getCardValue() == CardValue.queen){
                    card.setFunctional()
                }
            }
        }
    }

    private fun randomizer(minSize: Int, maxSize: Int, includesMax: Boolean = false): Int{
        return if(includesMax)
            (minSize .. maxSize).random()
        else
            (minSize until maxSize).random()
    }

    fun setDisplayedCardImage(view: ImageView){
        view.setImageResource(displayedCard.getCardId())
    }

    fun setInitialDisplayedCard(view: ImageView, viewInfo: TextView){
        displayedCard =
            currentFreeCards.get(randomizer(0, currentFreeCards.size, false))
        view.setImageResource(displayedCard.getCardId())
        currentFreeCards.remove(displayedCard)
        setCurrentCardInfo(viewInfo, true)
    }

    /**
     * sets information (value and house) of currently displayed card on stack
     * * wasFirst - variable indicating that it was the card played at the beginning of the game - i. e. when it was an action card
     */
    @SuppressLint("SetTextI18n")
    fun setCurrentCardInfo(currentCard: TextView, wasFirst: Boolean = false){
        var value: String = displayedCard.getCardValueName()
        var house: String = displayedCard.getCardType().toString()

        currentCard.setTextColor(context.resources.getColor(R.color.current_card_info))

        currentCard.text = "$value of $house"
    }

    fun shuffleUsedCards(): Boolean{
        if(currentFreeCards.isEmpty()){
            // if all the cards have players
            if(usedPlayingCards.isEmpty()){
                Toast.makeText(context, "There are no cards left to draw!", Toast.LENGTH_SHORT).show()
                return false
            }
            // if there are some leftover cards on the stack - place them into non-used cards stack
            for(it in usedPlayingCards) currentFreeCards.add(it)
            usedPlayingCards.clear()
        }

        return true
    }

    fun randomizeInitCards(): ArrayList<CardItem>{
        val array: ArrayList<CardItem> = ArrayList()

        for(it in 1..5){
            val randomizedCard: CardItem =
                currentFreeCards.get(randomizer(0, currentFreeCards.size, false))
            array.add(randomizedCard)
            currentFreeCards.remove(randomizedCard)
        }
        return array
    }

    // INFO: debugging function - for temporary use
    fun debug_randomizeInitCards(quantityRandom: Int = 5, wantedCards: ArrayList<CardItem>): ArrayList<CardItem>{
        val array: ArrayList<CardItem> = ArrayList()

        // set custom cards for players...
        for(card in wantedCards){
            Log.i("WANTED", card.display())
            for(free in currentFreeCards){
                if(free.sameAs(card)) {
                    array.add(free)
                    currentFreeCards.remove(free)
                    break
                }
            }
        }

        for(it in 1..quantityRandom){
            var randomizedCard: CardItem =
                currentFreeCards.get(randomizer(0, currentFreeCards.size, false))
            array.add(randomizedCard)
            currentFreeCards.remove(randomizedCard)
        }
        return array
    }

    fun drawFromFreeCards(): CardItem{
        return currentFreeCards.get(randomizer(0, currentFreeCards.size, false))
    }

    /** function used to play chosen cards
     * - sets current on top card as displayed card
     * - removes cards from the players hand
     * - manages used cards
     * - sets the penalty if action cards played
     * **/
    fun managePlayingCards(cardsChosen: ArrayList<CardItem>,
                           playerCards: ArrayList<CardItem>,
                           displayedImg: ImageView,
                           displayedInfo: TextView,
                           penaltyInfo: TextView): DemandedTypeSelector{

        var demandedType = DemandedTypeSelector.none
//        val cardsChosen = ArrayList<CardItem>()
//        for(it in selectedCards) cardsChosen.add(playerCards[it])

        if(currentPenalty.enabled()){   // if penalty is already set
            // add cards to used stack and set displayed card
            for (card in cardsChosen) {
                if (card.isFunctional() && card.isSelectedOnTop()) {
                    if (card.getCardValue() == CardValue.jack) {
                        demandedType = DemandedTypeSelector.Jack//demandedFigureAlertDialog()
                    } else if (card.getCardValue() == CardValue.ace) {
                        demandedType = DemandedTypeSelector.Ace//demandedHouseAlertDialog()
                    } else {
                        // update penalty INFO
                        currentPenalty.updatePenalty(cardsChosen, penaltyInfo)
                        break
                    }
                }
            }
            manageUsedCards(cardsChosen, playerCards, displayedImg, displayedInfo, penaltyInfo)

        } else {
            // check if selected cards are functional
            for (card in cardsChosen) {
                if (card.isFunctional() && card.isSelectedOnTop()) {
                    if (card.getCardValue() == CardValue.jack) {
                        demandedType = DemandedTypeSelector.Jack//demandedFigureAlertDialog()
                    } else if (card.getCardValue() == CardValue.ace) {
                        demandedType = DemandedTypeSelector.Ace//demandedHouseAlertDialog()
                    } else {    // ERROR: for 2s and 3s PENALTY is NOT visible
                        currentPenalty.setPenalty(cardsChosen)
                        break
                    }
                }
            }
            manageUsedCards(cardsChosen, playerCards, displayedImg, displayedInfo, penaltyInfo)
        }

        return demandedType
    }

    /**
     * manage used cards - change displayed card, add the rest of played cards to the usedCards stack
     * **/
    fun manageUsedCards(cardsChosen: ArrayList<CardItem>, playerCards: ArrayList<CardItem>,
                        displayedImg: ImageView, displayedInfo: TextView, penaltyInfo: TextView){
        // set card ON TOP as a displayedCard
        for (card in cardsChosen) {
            if (card.isSelectedOnTop()) {
                usedPlayingCards.add(displayedCard)
                card.resetSelected()
                displayedCard = card
                displayedImg.setImageResource(displayedCard.getCardId())
                playerCards.remove(card)
                cardsChosen.remove(card)
                break
            }
        }

        // for the rest of the cards
        for (card in cardsChosen) {
            card.resetSelected()
            usedPlayingCards.add(card)
            playerCards.remove(card)
        }

        setCurrentCardInfo(displayedInfo)
        penaltyInfo.text = "-"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun drawCardFromStack(playerCards: ArrayList<CardItem>,
                          cardAdapter: CardAdapter){
        var quantityToGet: Int = 1

        for(it in 1..quantityToGet){
            if(shuffleUsedCards()){         // if there are some cards available
                var randomizedCard: CardItem = drawFromFreeCards()
                playerCards.add(randomizedCard)
                currentFreeCards.remove(randomizedCard)
            }
        }

        cardAdapter.notifyDataSetChanged()
    }
    private fun populateCardDeck() {

        // TREFLE
        currentFreeCards.add(CardItem(R.drawable.card_clubs_02, HouseType.Clubs, CardValue.two, true))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_03, HouseType.Clubs, CardValue.three, true))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_04, HouseType.Clubs, CardValue.four, true))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_05, HouseType.Clubs, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_06, HouseType.Clubs, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_07, HouseType.Clubs, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_08, HouseType.Clubs, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_09, HouseType.Clubs, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_10, HouseType.Clubs, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_j, HouseType.Clubs, CardValue.jack, true))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_q, HouseType.Clubs, CardValue.queen, true))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_k, HouseType.Clubs, CardValue.king))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_a, HouseType.Clubs, CardValue.ace, true))
        // diamonds
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_02, HouseType.Diamonds, CardValue.two, true))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_03, HouseType.Diamonds, CardValue.three, true))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_04, HouseType.Diamonds, CardValue.four, true))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_05, HouseType.Diamonds, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_06, HouseType.Diamonds, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_07, HouseType.Diamonds, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_08, HouseType.Diamonds, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_09, HouseType.Diamonds, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_10, HouseType.Diamonds, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_j, HouseType.Diamonds, CardValue.jack, true))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_q, HouseType.Diamonds, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_k, HouseType.Diamonds, CardValue.king))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_a, HouseType.Diamonds, CardValue.ace, true))
        // spadesI
        currentFreeCards.add(CardItem(R.drawable.card_spades_02, HouseType.Spades, CardValue.two, true))
        currentFreeCards.add(CardItem(R.drawable.card_spades_03, HouseType.Spades, CardValue.three, true))
        currentFreeCards.add(CardItem(R.drawable.card_spades_04, HouseType.Spades, CardValue.four, true))
        currentFreeCards.add(CardItem(R.drawable.card_spades_05, HouseType.Spades, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_spades_06, HouseType.Spades, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_spades_07, HouseType.Spades, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_spades_08, HouseType.Spades, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_spades_09, HouseType.Spades, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_spades_10, HouseType.Spades, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_spades_j, HouseType.Spades, CardValue.jack, true))
        currentFreeCards.add(CardItem(R.drawable.card_spades_q, HouseType.Spades, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_spades_k, HouseType.Spades, CardValue.king, true))
        currentFreeCards.add(CardItem(R.drawable.card_spades_a, HouseType.Spades, CardValue.ace, true))
        // KIERY
        currentFreeCards.add(CardItem(R.drawable.card_hearts_02, HouseType.Hearts, CardValue.two, true))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_03, HouseType.Hearts, CardValue.three, true))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_04, HouseType.Hearts, CardValue.four, true))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_05, HouseType.Hearts, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_06, HouseType.Hearts, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_07, HouseType.Hearts, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_08, HouseType.Hearts, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_09, HouseType.Hearts, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_10, HouseType.Hearts, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_j, HouseType.Hearts, CardValue.jack, true))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_q, HouseType.Hearts, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_k, HouseType.Hearts, CardValue.king, true))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_a, HouseType.Hearts, CardValue.ace, true))
//        // JOKERY
//        currentFreeCards.add(CardItem(R.drawable.card_joker_black, HouseType.Joker, CardValue.red))
//        currentFreeCards.add(CardItem(R.drawable.card_joker_red, HouseType.Joker, CardValue.black))
    }
}

    val cardsIds = arrayOf<Int>(
    R.drawable.card_hearts_a,
    R.drawable.card_hearts_02,
    R.drawable.card_hearts_03,
    R.drawable.card_hearts_04,
    R.drawable.card_hearts_05,
    R.drawable.card_hearts_06,
    R.drawable.card_hearts_07,
    R.drawable.card_hearts_08,
    R.drawable.card_hearts_09,
    R.drawable.card_hearts_10,
    R.drawable.card_hearts_j,
    R.drawable.card_hearts_q,
    R.drawable.card_hearts_k,
    //R.drawable.card_empty,
    R.drawable.card_diamonds_a,
    R.drawable.card_diamonds_02,
    R.drawable.card_diamonds_03,
    R.drawable.card_diamonds_04,
    R.drawable.card_diamonds_05,
    R.drawable.card_diamonds_06,
    R.drawable.card_diamonds_07,
    R.drawable.card_diamonds_08,
    R.drawable.card_diamonds_09,
    R.drawable.card_diamonds_10,
    R.drawable.card_diamonds_j,
    R.drawable.card_diamonds_q,
    R.drawable.card_diamonds_k,
    //R.drawable.card_back,
    R.drawable.card_clubs_a,
    R.drawable.card_clubs_02,
    R.drawable.card_clubs_03,
    R.drawable.card_clubs_04,
    R.drawable.card_clubs_05,
    R.drawable.card_clubs_06,
    R.drawable.card_clubs_07,
    R.drawable.card_clubs_08,
    R.drawable.card_clubs_09,
    R.drawable.card_clubs_10,
    R.drawable.card_clubs_j,
    R.drawable.card_clubs_q,
    R.drawable.card_clubs_k,
    //R.drawable.card_joker_red,
    R.drawable.card_spades_a,
    R.drawable.card_spades_02,
    R.drawable.card_spades_03,
    R.drawable.card_spades_04,
    R.drawable.card_spades_05,
    R.drawable.card_spades_06,
    R.drawable.card_spades_07,
    R.drawable.card_spades_08,
    R.drawable.card_spades_09,
    R.drawable.card_spades_10,
    R.drawable.card_spades_j,
    R.drawable.card_spades_q,
    R.drawable.card_spades_k
    //R.drawable.card_joker_black
)