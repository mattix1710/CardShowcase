package com.example.cardshowcase

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cardshowcase.cardshandling.*
import com.example.cardshowcase.databinding.FragmentFirstBinding
import com.example.cardshowcase.players.Player
import com.google.android.flexbox.*
import com.google.android.material.snackbar.Snackbar




/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), CardListListener {

    private var _binding: FragmentFirstBinding? = null

    //////////////////////////////////////////////
    // card info among the players
    //
    private lateinit var cardManager: CardManager
    private var penalty: Int = 0
    //
    //////////////////////////////////////////////

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //////////////////////////////////////////////
    // private player card info
    //
    private val thisPlayerNum: Int = 1
    private var players = ArrayList<Player>()
    private var playerCards: ArrayList<CardItem> ? = null

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
    private var selectedCards = SelectedCardsStruct()            // list of selected cards positions
    private var cardAdapter: CardAdapter? = null
    //
    //////////////////////////////////////////////

    private var recyclerView: RecyclerView? = null

    // onCreateView - trying to use only for view inflaters
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set initial resources
        cardManager = CardManager(requireContext())

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
        }

        binding.drawCard.setOnClickListener { drawNewCard() }

        binding.playCardbutton!!.setOnClickListener{ playCards() }
        playCardsButtonUpdate()

        // draw cards to players hand (view)
        playerCards = cardManager.randomizeCardList()
        cardAdapter = CardAdapter(playerCards!!, this@FirstFragment, requireContext())

        recyclerView = binding.cardListView
        recyclerView!!.adapter = cardAdapter

        var layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        //layoutManager.alignContent = AlignContent.FLEX_START          // cannot be used in FlexboxLayoutManager
        layoutManager.alignItems = AlignItems.STRETCH
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.flexWrap = FlexWrap.WRAP

        recyclerView!!.layoutManager = layoutManager

        //draw a card for beginning a game
        cardManager?.setInitialDisplayedCard(binding.displayedCard, binding.currentCardName!!)
    }

    //////////////////////// onItemClick ///////////////////////

    override fun onItemClick(position: Int, view: View) {
        Log.i("Card_click", "card_${position}")

        var card: CardItem = playerCards!!.get(position)        // REMEMBER!: it's passed by reference

        // TODO: Instead of Snackbar -> Toast.LONG

        if(selectedCards.list.size == selectedCards.MAX_SELECTED){
            notifyMaxCardsSelected()
        }
        else if(selectedCards.list.isEmpty()){                   // if there are no cards selected
            card.resetSelected()
            card.setSelectedOnTop()
            selectedCards.list.add(position)
            selectedCards.selectionType = SelectedCardsStruct.SelectionType.one
            selectedCards.selectionValue = card.getCardValue()
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Snackbar.LENGTH_SHORT).show()
        } else if(selectedCards.list.contains(position)) {  // if current card was selected - unselect it!
            card.resetSelected()
            selectedCards.list.remove(position)
            if(selectedCards.list.isEmpty()){
                resetSelectedCards()
            }
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} was unselected!", Snackbar.LENGTH_SHORT).show()

        } else if(selectedCards.selectionValue == card.getCardValue()){
            card.resetSelected()
            card.setSelected()
            selectedCards.list.add(position)
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} was selected!", Snackbar.LENGTH_SHORT).show()

            if(card.getCardValue() ==  cardManager.displayedCard.getCardValue())
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.value
            else
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.house
        } else{
            wrongCardAlertDialog()
        }

        playCardsButtonUpdate()
        cardAdapter!!.notifyDataSetChanged()
    }

    override fun onItemLongClick(position: Int, view: View) {
        var card: CardItem = playerCards!!.get(position)

        // TODO: Instead of Snackbar -> Toast.LONG

        if(selectedCards.list.size == selectedCards.MAX_SELECTED){
            notifyMaxCardsSelected()
        }
        else if(selectedCards.list.isEmpty()){                   // if there are no cards selected
            card.resetSelected()
            card.setSelectedOnTop()
            selectedCards.list.add(position)
            selectedCards.selectionType = SelectedCardsStruct.SelectionType.one
            selectedCards.selectionValue = card.getCardValue()
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Snackbar.LENGTH_SHORT).show()
        } else if(selectedCards.list.contains(position)){   // if current was selected - select it as ON TOP
            if(!card.isSelectedOnTop()) {
                for (played in playerCards!!) {
                    if (played.isSelectedOnTop()) {
                        played.resetSelected()
                        played.setSelected()
                    }
                }
                card.resetSelected()
                card.setSelectedOnTop()
                Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Snackbar.LENGTH_SHORT).show()
            }
        } else if(selectedCards.selectionValue == card.getCardValue()){
            for(played in playerCards!!){
                if(played.isSelectedOnTop()){
                    played.resetSelected()
                    played.setSelected()
                }
            }
            card.resetSelected()
            card.setSelectedOnTop()
            selectedCards.list.add(position)
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Snackbar.LENGTH_SHORT).show()

            if(card.getCardValue() == cardManager.displayedCard.getCardValue())
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.value
            else
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.house
        } else{
            wrongCardAlertDialog()
        }

        playCardsButtonUpdate()
        cardAdapter!!.notifyDataSetChanged()
    }

    private fun drawNewCard() {

        // while new cards are drawed, the state of current cards is reset
        for(card in playerCards!!) card.resetSelected()
        resetSelectedCards()


        var quantityToGet: Int
        if(penalty == 0)
            quantityToGet = 1
        else {
            quantityToGet = penalty
            cardManager.displayedCard.setCardPlayed()
        }

        for(it in 1..quantityToGet) {
            if(cardManager.shuffleUsedCards()) {            // if there are cards available
                var randomizedCard: CardItem = cardManager.drawFromFreeCards()
                playerCards!!.add(randomizedCard)

                //update cardAdapter
                cardManager.currentFreeCards.remove(randomizedCard)
            }
        }

        Log.i("FREE CARDS", cardManager.currentFreeCards.size.toString())
        Log.i("USED CARDS", cardManager.usedPlayingCards.size.toString())

        // update the view
        updatePlayerInfo()
        cardAdapter!!.notifyDataSetChanged()
    }

    private fun playCards(){

        if(selectedCards.list.size == 1){
            var card = playerCards!![selectedCards.list.get(0)]
            if(cardManager.displayedCard.getCardValue() == card.getCardValue()
                ||  cardManager.displayedCard.getCardType() == card.getCardType()){
                managePlayingCards()
            } else{
                wrongCardAlertDialog()
            }
        } else if(selectedCards.selectionType == SelectedCardsStruct.SelectionType.value){
            // if selected cards has the same value as displayed card - i.e. stacking the same value
            managePlayingCards()
        } else if(selectedCards.selectionType == SelectedCardsStruct.SelectionType.house){
            // if selected cards base on the house type of the displayed card
            var matchesHouseType: Boolean = false
            var wrongOnTop: Boolean = false

            for(it in selectedCards.list){
                if(playerCards!![it].getCardType() ==  cardManager.displayedCard.getCardType())
                    matchesHouseType = true
                if(playerCards!![it].isSelectedOnTop()){
                    if(playerCards!![it].getCardType() ==  cardManager.displayedCard.getCardType()){
                        wrongOnTop = true
                        break
                    }
                }
            }

            if(wrongOnTop || !matchesHouseType){
                wrongCardAlertDialog(wrongOnTop, matchesHouseType)
            } else{
                managePlayingCards()
            }
        }
    }

    // TODO: przerzucić może do klasy CardManager (?)
    // INFO: DONE - probably
    private fun managePlayingCards(){
        var cardsChosen = ArrayList<CardItem>()

        for(it in selectedCards.list){
            cardsChosen.add(playerCards!![it])
        }

        // set card ON TOP as a  cardManager.displayedCard
        for(card in cardsChosen){
            if(card.isSelectedOnTop()){
                cardManager.usedPlayingCards.add(cardManager.displayedCard)
                card.resetSelected()
                 cardManager.displayedCard = card
                binding.displayedCard.setImageResource(cardManager.displayedCard.getCardId())
                playerCards!!.remove(card)
                cardsChosen.remove(card)
                break
            }
        }

        // for rest of the cards
        for(card in cardsChosen){
            card.resetSelected()
            cardManager.usedPlayingCards.add(card)
            playerCards!!.remove(card)
        }

        if(selectedCards.list.size == 1)
            Toast.makeText(requireContext(), "You played ${selectedCards.list.size} card!", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(requireContext(), "You played ${selectedCards.list.size} cards!", Toast.LENGTH_SHORT).show()

        resetSelectedCards()
        updatePlayerInfo()
        cardManager.setCurrentCardInfo(binding.currentCardName!!)
        cardAdapter!!.notifyDataSetChanged()

        //==============================================
        // from previous commits

        /*if(cardGameLogic(cardItem,  cardManager.displayedCard)) {
            // delete card from visible list
            arrayList!!.remove(cardItem)
            // INFO: when a card is chosen, current card on-deck goes to the available cards...
            if (!(displayedCard.getCardType() == HouseType.none &&  cardManager.displayedCard.getCardValue() == CardValue.none)) {
                usedPlayingCards!!.add(displayedCard)
            }
             cardManager.displayedCard = cardItem
            cardItem.getCardId()?.let { binding.displayedCard.setImageResource(it) }
            // INFO: changing card quantity
            freeCardsQuantity++
            binding.currentFreeCards.text = freeCardsQuantity.toString()
            // TO_DELETE: INFO: changing card quantity
            //freeCardsQuantity++
            //binding.currentFreeCards.text = freeCardsQuantity.toString()
            Toast.makeText(requireContext(), "Card was played!", Toast.LENGTH_SHORT).show()
            // change current card info
            setCurrentCardInfo()
            Log.i("PLACE", "before penalty check")
            penaltyCheckerAlertDialog(displayedCard, arrayList!!)
            // at the end: reset displayed data
            cardAdapter!!.notifyDataSetChanged()
        } else{
            wrongCardAlertDialog(displayedCard)
            //Snackbar.make(view, "Card cannot be played!", Snackbar.LENGTH_SHORT).show()
            Toast.makeText(requireContext(), "Card cannot be played!", Toast.LENGTH_SHORT).show()
        }
        }*/
    }


    /** change "Play a card" button state **/
    private fun playCardsButtonUpdate(){
        when(selectedCards.list.size){
            0 -> {
                binding.playCardbutton!!.isEnabled = false
                binding.playCardbutton!!.text = "Play a card"}
            1 -> {
                binding.playCardbutton!!.isEnabled = true
                binding.playCardbutton!!.text = "Play a card"}
            2 -> {
                if(selectedCards.twosPlaying())
                    binding.playCardbutton!!.text = "Play 2 cards"
                else
                    binding.playCardbutton!!.isEnabled = false }
            3 -> {
                binding.playCardbutton!!.isEnabled = true
                binding.playCardbutton!!.text = "Play 3 cards"}
            4 -> {
                binding.playCardbutton!!.isEnabled = true
                binding.playCardbutton!!.text = "Play 4 cards"}
            else -> tooManySelected()
        }
    }

    private fun tooManySelected(){
        // TODO: tooManySelected function
        binding.playCardbutton!!.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TODO: card game logic
    private fun cardGameLogic(card: CardItem, onStackCard: CardItem): Boolean {
        // SIMPLE RULES
        //
        // 1. if there is an empty card (no cards on the table) - every card is allowed
        // 2. if there is a joker - FOR NOW: every card is allowed <-- there should be a prompt which card you want to play
        // 3. if player has a joker - can play for every card
        // 4. if player has a regular card - he only can play on cards of the same house or same value
        //
        if(onStackCard.getCardType() == HouseType.none && onStackCard.getCardValue() == CardValue.none)
            return true
        else if(card.getCardType() == HouseType.Joker || onStackCard.getCardType() == HouseType.Joker)
            return true
        else if(card.getCardType() == onStackCard.getCardType() || card.getCardValue() == onStackCard.getCardValue())
            return true
        return false
    }

    private fun wrongCardAlertDialog(wrongOnTop: Boolean = false, matchesHouseType: Boolean = false){
        val alert = AlertDialog.Builder(requireContext())
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

    private fun notifyMaxCardsSelected(){
        val alert = AlertDialog.Builder(requireContext())
        alert.setMessage("Maximum number (4) of cards was selected!")
        alert.setPositiveButton(
            "OK"
        ){ dialogInterface, i -> }
        alert.create().show()
    }

    ///////////////////////////////////
    // TODO: PENALTIES
    private fun penaltyCheckerAlertDialog(cardOnStack: CardItem, playerCards: ArrayList<CardItem>){
        val alert = AlertDialog.Builder(requireContext())

        var cardFound: Boolean = false

        // check for 2s
        if(cardOnStack.wasCardPlayed()){
             cardManager.displayedCard.resetCardPlayed()
            binding.drawCardButton!!.text = "Draw a card"
            penalty = 0
        }
        else {
            for (it in playerCards) {
                if (it.getCardValue() == CardValue.two && cardOnStack.getCardValue() == CardValue.two) {
                    Log.i("PLACE", "Found card - 2")
                    cardFound = true
                    break
                }
            }
        }

        if(cardFound) {
            penalty = penalty + 2
            // set an alert that there is an option to revenge..., or just draw cards
            alert.setMessage("You can play a 2 against the other 2 or draw $penalty cards.")
            alert.setPositiveButton(
                "I understand"
            ) { dialogInterface, i ->

            }
            alert.create().show()

            binding.drawCardButton!!.text = "Draw $penalty cards"
        }

    }

    ////////////////////////////////////////////////
    // OTHER updating functions

    private fun resetSelectedCards(){
        selectedCards.list.clear()
        selectedCards.selectionType = SelectedCardsStruct.SelectionType.one
        selectedCards.selectionValue = CardValue.none
    }

    private fun updatePlayerInfo(){
        when(thisPlayerNum){
            1 -> binding.player1CardsInfo!!.text = playerCards!!.size.toString()
            else -> Log.i("PLAYER_ERROR", "There is no such player!")
        }
    }

}