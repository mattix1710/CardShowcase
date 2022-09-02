package com.example.cardshowcase.cardsHandling

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.example.cardshowcase.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class CardManager(val context: Context){

    var allPlayingCards  = ArrayList<CardItem>()
    var currentFreeCards = ArrayList<CardItem>()
    var usedPlayingCards = ArrayList<CardItem>()
    var displayedCard    = CardItem(R.drawable.card_empty)
    var currentPenalty: Penalty = Penalty()

    var queenFunctional: Boolean = false

    init {
        populateCardDeck()
        allPlayingCards = currentFreeCards.clone() as ArrayList<CardItem>
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
    fun setCurrentCardInfo(currentCard: TextView, wasFirst: Boolean = false){
        var value: String = displayedCard.getCardValueName()
        var house: String = displayedCard.getCardType().toString()

        if(displayedCard.getCardValue() == CardValue.two && !wasFirst)
            currentCard.setTextColor(context.resources.getColor(R.color.current_card_functional_info))
        else
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
        var array: ArrayList<CardItem> = ArrayList()

        for(it in 1..5){
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
     * **/
    fun managePlayingCards(selectedCards: ArrayList<Int>,
                           playerCards: ArrayList<CardItem>,
                           displayedImg: ImageView,
                           displayedInfo: TextView){

        // select only chosen cards from playerCards
        var cardsChosen = ArrayList<CardItem>()
        for(it in selectedCards){
            cardsChosen.add(playerCards[it])
        }

        // if there is no penalty set
        if(currentPenalty.type.equals(Penalty.PenaltyType.none)) {
            if(checkIfActionCards(cardsChosen)){    // if played cards are action cards
                //TODO: set penalty
                defineActionCards(cardsChosen)
                //TODO: manageCards()
            } else{                                 // if played cards are regular cards
                //TODO: manageCards()
            }
        } else if(currentPenalty.type.equals(Penalty.PenaltyType.draw)){
            if(checkIfActionCards(playerCards)){
                //TODO: action cards
            }
        }

        ///////////////////////////// manageCards()



        // set card ON TOP as a displayedCard
        for(card in cardsChosen){
            if(card.isSelectedOnTop()){
                usedPlayingCards.add(displayedCard)
                card.resetSelected()
                displayedCard = card
                displayedImg.setImageResource(displayedCard.getCardId())
                playerCards.remove(card)
                cardsChosen.remove(card)
                break
            }
        }

        // for rest of the cards
        for(card in cardsChosen){
            card.resetSelected()
            usedPlayingCards.add(card)
            playerCards.remove(card)
        }

        if(selectedCards.size == 1)
            Toast.makeText(context, "You played ${selectedCards.size} card!", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context, "You played ${selectedCards.size} cards!", Toast.LENGTH_SHORT).show()

        setCurrentCardInfo(displayedInfo)
    }

    private fun checkIfActionCards(cards: ArrayList<CardItem>): Boolean{
        for(card in cards){
            if(card.isFunctional())
                return true
        }
        return false
    }

    private fun defineActionCards(cards: ArrayList<CardItem>){
        // all cards chosen are of the same figure, so we have to check only the last one in the array
        // last -> it will decide about the penalty of some cards (i.e. king of spades, king od hearts)
        var currentCard: CardItem? = null

        for(card in cards){
            if(card.isSelectedOnTop())
                currentCard = card
        }

        when(currentCard!!.getCardValue()){
            CardValue.two -> currentPenalty.setDrawCards(2, cards.size)
            CardValue.three -> currentPenalty.setDrawCards(3, cards.size)
            CardValue.four -> currentPenalty.setHaltPlayer(cards.size)
            CardValue.queen -> currentPenalty.reset()
            CardValue.jack -> {
            //TODO: demand figure AlertDialog
                demandedFigureAlertDialog()
            }
            CardValue.king -> {
                if(currentCard!!.getCardType() == HouseType.Spades){
                    // TODO: rethink
                    currentPenalty.setDrawBackCards(-5)
                } else if(currentCard!!.getCardType() == HouseType.Hearts){
                    currentPenalty.setDrawCards(5, 1)
                }
            }
            CardValue.ace -> {
                //TODO: demand house AlertDialog
                demandedHouseAlertDialog()
            }
            else -> Log.i("ACTION CARDS", "Wrong cards are selected as action cards!")
        }
    }

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

    private fun demandedFigureAlertDialog(){
        var singleItems = arrayOf("5", "6", "7", "8", "9", "10")
        if(queenFunctional)
            singleItems.plus("Queen")
        var checkedItem = 0

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.demand_figure_message)
            .setNeutralButton(R.string.demand_figure_cancel){
                dialog, which ->
                // Returns Jack as a regular card
                currentPenalty.demandedFigure = Penalty.DemandFigure.none
            }
            .setPositiveButton(R.string.demand_figure_ok){ dialog, which ->
                // Accept current choice
                when(checkedItem){
                    0 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Five
                    1 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Six
                    2 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Seven
                    3 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Eight
                    4 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Nine
                    5 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Ten
                    6 -> currentPenalty.demandedFigure = Penalty.DemandFigure.Queen
                    else -> currentPenalty.demandedFigure = Penalty.DemandFigure.none
                }
            }
            .setSingleChoiceItems(singleItems, checkedItem){ dialog, which ->
                Log.i("DEMAND", which.toString())
                checkedItem = which
            }
            .setCancelable(false)
            .show()
    }

    private fun demandedHouseAlertDialog(){
        val singleItems = arrayOf(context.resources.getString(R.string.heart_emoji),
                        context.resources.getString(R.string.clubs_emoji),
                        context.resources.getString(R.string.spades_emoji),
                        context.resources.getString(R.string.diamond_emoji))
        var checkedItem = 0

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.demand_house_message)
            .setNeutralButton(R.string.demand_house_cancel){ dialog, which ->
                // Returns Ace as a regular card
                currentPenalty.demandedHouse = Penalty.DemandHouse.none
            }
            .setPositiveButton(R.string.demand_house_ok){ dialog, which ->
                // Accept current choice
                when(checkedItem){
                    0 -> currentPenalty.demandedHouse = Penalty.DemandHouse.Hearts
                    1 -> currentPenalty.demandedHouse = Penalty.DemandHouse.Clubs
                    2 -> currentPenalty.demandedHouse = Penalty.DemandHouse.Spades
                    3 -> currentPenalty.demandedHouse = Penalty.DemandHouse.Diamonds
                    else -> currentPenalty.demandedHouse = Penalty.DemandHouse.none
                }
            }
            .setSingleChoiceItems(singleItems, checkedItem){ dialog, which ->
                checkedItem = which
            }
            .setCancelable(false)
            .show()
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
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_q, HouseType.Diamonds, CardValue.queen, true))
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
        currentFreeCards.add(CardItem(R.drawable.card_spades_q, HouseType.Spades, CardValue.queen, true))
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
        currentFreeCards.add(CardItem(R.drawable.card_hearts_q, HouseType.Hearts, CardValue.queen, true))
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