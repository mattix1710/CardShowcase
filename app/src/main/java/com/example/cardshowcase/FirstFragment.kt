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
import com.google.android.flexbox.*
import com.google.android.material.snackbar.Snackbar


//INFO: ignore red marks on certain cards... everything is alright
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
    R.drawable.card_joker_red,
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
    R.drawable.card_spades_k,
    R.drawable.card_joker_black
)

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), CardListListener {

    private var _binding: FragmentFirstBinding? = null

    //////////////////////////////////////////////
    // card info among the players
    //
    private var allPlayingCards = ArrayList<CardItem>()
    private var currentFreeCards = ArrayList<CardItem>()
    private var usedPlayingCards = ArrayList<CardItem>()
    private var displayedCard: CardItem = CardItem(R.drawable.card_empty)
    private var freeCardsQuantity: Int = 0
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
    private var cardAdapter:CardAdapter ? = null
    //
    //////////////////////////////////////////////

    private var recyclerView: RecyclerView? = null

    // onCreateView - trying to use only for view inflaters
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO: zabezpieczyć przed resetowanie podczas obracania ekranu
        // TODO: + utworzyć layout dla horyzontalnej pozycji

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set initial resources
        displayedCard.getCardId()?.let { binding.displayedCard.setImageResource(it) }

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
        }

        binding.drawCard.setOnClickListener { drawNewCard() }

        binding.playCardbutton!!.setOnClickListener{ playCards() }
        playCardsButtonUpdate()

        //////////////////////////////////////
        // for recyclerView
        populateCardDeck()
        allPlayingCards = currentFreeCards
        freeCardsQuantity = currentFreeCards.size

        // draw cards to players hand (view)
        playerCards = randomizeCardList()
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

        //////////////////////////////////////

        //draw a card for beginning a game
        displayedCard = currentFreeCards.get((0 until currentFreeCards.size).random())
        displayedCard.getCardId()?.let { binding.displayedCard.setImageResource(it) }
        currentFreeCards.remove(displayedCard)
        setCurrentCardInfo(wasFirst = true)
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

            if(card.getCardValue() == displayedCard.getCardValue())
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

            if(card.getCardValue() == displayedCard.getCardValue())
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.value
            else
                selectedCards.selectionType = SelectedCardsStruct.SelectionType.house
        } else{
            wrongCardAlertDialog()
        }

        playCardsButtonUpdate()
        cardAdapter!!.notifyDataSetChanged()
    }

    // wasFirst - variable indicating that it was the card played at the beginning of the game
    private fun setCurrentCardInfo(wasFirst: Boolean = false) {
        var value: String = displayedCard.getCardValueName()
        var house: String = displayedCard.getCardType().toString()

        if(displayedCard.getCardValue() == CardValue.two && !wasFirst)
            binding.currentCardName!!.setTextColor(resources.getColor(R.color.current_card_functional_info))
        else
            binding.currentCardName!!.setTextColor(resources.getColor(R.color.current_card_info))

        binding.currentCardName!!.text = "$value of $house"
    }

    private fun populateCardDeck() {

        // TREFLE
        currentFreeCards.add(CardItem(R.drawable.card_clubs_02, HouseType.Clubs, CardValue.two))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_03, HouseType.Clubs, CardValue.three))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_04, HouseType.Clubs, CardValue.four))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_05, HouseType.Clubs, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_06, HouseType.Clubs, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_07, HouseType.Clubs, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_08, HouseType.Clubs, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_09, HouseType.Clubs, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_10, HouseType.Clubs, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_j, HouseType.Clubs, CardValue.jack))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_q, HouseType.Clubs, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_k, HouseType.Clubs, CardValue.king))
        currentFreeCards.add(CardItem(R.drawable.card_clubs_a, HouseType.Clubs, CardValue.ace))
        // diamonds
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_02, HouseType.Diamonds, CardValue.two))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_03, HouseType.Diamonds, CardValue.three))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_04, HouseType.Diamonds, CardValue.four))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_05, HouseType.Diamonds, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_06, HouseType.Diamonds, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_07, HouseType.Diamonds, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_08, HouseType.Diamonds, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_09, HouseType.Diamonds, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_10, HouseType.Diamonds, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_j, HouseType.Diamonds, CardValue.jack))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_q, HouseType.Diamonds, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_k, HouseType.Diamonds, CardValue.king))
        currentFreeCards.add(CardItem(R.drawable.card_diamonds_a, HouseType.Diamonds, CardValue.ace))
        // spadesI
        currentFreeCards.add(CardItem(R.drawable.card_spades_02, HouseType.Spades, CardValue.two))
        currentFreeCards.add(CardItem(R.drawable.card_spades_03, HouseType.Spades, CardValue.three))
        currentFreeCards.add(CardItem(R.drawable.card_spades_04, HouseType.Spades, CardValue.four))
        currentFreeCards.add(CardItem(R.drawable.card_spades_05, HouseType.Spades, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_spades_06, HouseType.Spades, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_spades_07, HouseType.Spades, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_spades_08, HouseType.Spades, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_spades_09, HouseType.Spades, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_spades_10, HouseType.Spades, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_spades_j, HouseType.Spades, CardValue.jack))
        currentFreeCards.add(CardItem(R.drawable.card_spades_q, HouseType.Spades, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_spades_k, HouseType.Spades, CardValue.king))
        currentFreeCards.add(CardItem(R.drawable.card_spades_a, HouseType.Spades, CardValue.ace))
        // KIERY
        currentFreeCards.add(CardItem(R.drawable.card_hearts_02, HouseType.Hearts, CardValue.two))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_03, HouseType.Hearts, CardValue.three))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_04, HouseType.Hearts, CardValue.four))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_05, HouseType.Hearts, CardValue.five))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_06, HouseType.Hearts, CardValue.six))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_07, HouseType.Hearts, CardValue.seven))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_08, HouseType.Hearts, CardValue.eight))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_09, HouseType.Hearts, CardValue.nine))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_10, HouseType.Hearts, CardValue.ten))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_j, HouseType.Hearts, CardValue.jack))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_q, HouseType.Hearts, CardValue.queen))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_k, HouseType.Hearts, CardValue.king))
        currentFreeCards.add(CardItem(R.drawable.card_hearts_a, HouseType.Hearts, CardValue.ace))
        // JOKERY
        currentFreeCards.add(CardItem(R.drawable.card_joker_black, HouseType.Joker, CardValue.red))
        currentFreeCards.add(CardItem(R.drawable.card_joker_red, HouseType.Joker, CardValue.black))
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
            displayedCard.setCardPlayed()
        }

        for(it in 1..quantityToGet) {
            if(shuffleUsedCards()) {            // if there are cards available
                var randomizedCard: CardItem =
                    currentFreeCards.get((0 until currentFreeCards.size).random())
                playerCards!!.add(randomizedCard)

                //update cardAdapter
                freeCardsQuantity--
                binding.currentFreeCards.text = freeCardsQuantity.toString()
                currentFreeCards.remove(randomizedCard)
            }
        }

        // update the view
        updatePlayerInfo()
        cardAdapter!!.notifyDataSetChanged()
    }

    private fun playCards(){

        if(selectedCards.list.size == 1){
            var card = playerCards!![selectedCards.list.get(0)]
            if(displayedCard.getCardValue() == card.getCardValue()
                || displayedCard.getCardType() == card.getCardType()){
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
                if(playerCards!![it].getCardType() == displayedCard.getCardType())
                    matchesHouseType = true
                if(playerCards!![it].isSelectedOnTop()){
                    if(playerCards!![it].getCardType() == displayedCard.getCardType()){
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

    // INFO: DONE - probably
    private fun managePlayingCards(){
        var cardsChosen = ArrayList<CardItem>()

        for(it in selectedCards.list){
            cardsChosen.add(playerCards!![it])
        }

        // set card ON TOP as a displayedCard
        for(card in cardsChosen){
            if(card.isSelectedOnTop()){
                usedPlayingCards.add(displayedCard)
                card.resetSelected()
                displayedCard = card
                binding.displayedCard.setImageResource(displayedCard.getCardId())
                playerCards!!.remove(card)
                cardsChosen.remove(card)
                break
            }
        }

        // for rest of the cards
        for(card in cardsChosen){
            card.resetSelected()
            usedPlayingCards.add(card)
            playerCards!!.remove(card)
        }

        if(selectedCards.list.size == 1)
            Toast.makeText(requireContext(), "You played ${selectedCards.list.size} card!", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(requireContext(), "You played ${selectedCards.list.size} cards!", Toast.LENGTH_SHORT).show()

        resetSelectedCards()
        updatePlayerInfo()
        cardAdapter!!.notifyDataSetChanged()

        //==============================================
        // from previous commits

        /*if(cardGameLogic(cardItem, displayedCard)) {
            // delete card from visible list
            arrayList!!.remove(cardItem)
            // INFO: when a card is chosen, current card on-deck goes to the available cards...
            if (!(displayedCard.getCardType() == HouseType.none && displayedCard.getCardValue() == CardValue.none)) {
                usedPlayingCards!!.add(displayedCard)
            }
            displayedCard = cardItem
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

    private fun shuffleUsedCards(): Boolean{
        if(currentFreeCards.isEmpty()){
            //if all the cards have players
            if(usedPlayingCards.isEmpty()){
                Toast.makeText(requireContext(), "There are no cards left to draw!", Toast.LENGTH_SHORT).show()
                return false
            }
            //if there are some leftover cards on the stack - place them into non-used cards stack
            for(it in usedPlayingCards) currentFreeCards.add(it)
            usedPlayingCards.clear()
        }

        return true
    }

    private fun randomizeCardList(): ArrayList<CardItem>{
        var arrayList: ArrayList<CardItem> = ArrayList()

        for(i in 1..5){
            var randomizedCard: CardItem = currentFreeCards.get((0 until currentFreeCards.size).random())
            arrayList.add(randomizedCard)
            currentFreeCards.remove(randomizedCard)

            // INFO: changing card quantity
            freeCardsQuantity--
            binding.currentFreeCards.text = freeCardsQuantity.toString()
        }
        return arrayList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
        var cardOnStack = displayedCard

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
    // PENALTIES
    private fun penaltyCheckerAlertDialog(cardOnStack: CardItem, playerCards: ArrayList<CardItem>){
        val alert = AlertDialog.Builder(requireContext())

        var cardFound: Boolean = false

        // check for 2s
        if(cardOnStack.wasCardPlayed()){
            displayedCard.resetCardPlayed()
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

    /** returns true whether there are matching cards in hand
     * OR there is no card selected yet - but selected card matches the one onStack
     * **/
    private fun checkSelectedCardsCompatibility(current: CardItem, view: View, onTop: Boolean = false): Boolean{

        var ifAlreadySelected: Boolean = false

        for(card in playerCards!!){
            if(card.isSelected() || card.isSelectedOnTop()) {       // check if any card in Hand is selected
                ifAlreadySelected = true
                if (card.getCardValue() == current.getCardValue())  // and matches the value of selected card
                    continue
                else {                                              // if doesn't match the value
                    Snackbar.make(
                        view,
                        "This card cannot be selected among the currently chosen!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
        }

        // if there are other cards OR there is no card selected (in onClick functions there are assignments to ".one")
        if(current.getCardValue() == displayedCard.getCardValue())
            selectedCards.selectionType = SelectedCardsStruct.SelectionType.value
        else if(current.getCardType() == displayedCard.getCardType())
            selectedCards.selectionType = SelectedCardsStruct.SelectionType.house
        else{
            if(!ifAlreadySelected)      // if current card doesn't match displayedCard AND no card was selected before
                return false
        }

        return true
    }

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