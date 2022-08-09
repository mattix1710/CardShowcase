package com.example.cardshowcase

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cardshowcase.cardshandling.CardAdapter
import com.example.cardshowcase.cardshandling.CardItem
import com.example.cardshowcase.cardshandling.CardValue
import com.example.cardshowcase.cardshandling.HouseType
import com.example.cardshowcase.databinding.FragmentFirstBinding
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
class FirstFragment : Fragment(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private var _binding: FragmentFirstBinding? = null

    private var allPlayingCards = ArrayList<CardItem>()
    private var currentFreeCards = ArrayList<CardItem>()
    private var usedPlayingCards = ArrayList<CardItem>()
    private var displayedCard: CardItem = CardItem(R.drawable.card_empty)
    private var freeCardsQuantity: Int = 0
    private var penalty: Int = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var gridView:GridView ? = null
    private var arrayList: ArrayList<CardItem> ? = null
    private var cardAdapter:CardAdapter ? = null


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

        gridView = binding.cardList

        // populating arrays
        populateCardDeck()
        allPlayingCards = currentFreeCards
        //for(card in cardsIds) currentFreeCards.add(CardItem(card))

        freeCardsQuantity = currentFreeCards.size

        //draw cards to players hand (view)
        arrayList = randomizeCardList()
        cardAdapter = CardAdapter(requireContext(), arrayList!!)
        gridView?.adapter = cardAdapter
        gridView?.onItemClickListener = this

        //draw a card for beginning a game
        displayedCard = currentFreeCards.get((0 until currentFreeCards.size).random())
        displayedCard.getCardId()?.let { binding.displayedCard.setImageResource(it) }
        currentFreeCards.remove(displayedCard)
        setCurrentCardInfo(wasFirst = true)

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
        //INFO: ignore red marks on certain cards... everything is alright

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

        var quantityToGet: Int
        if(penalty == 0)
            quantityToGet = 1
        else {
            quantityToGet = penalty
            displayedCard.setCardPlayed()
        }

        for(it in 1..quantityToGet) {
            shuffleUsedCards()
            var randomizedCard: CardItem =
                currentFreeCards.get((0 until currentFreeCards.size).random())
            cardAdapter!!.arrayList.add(randomizedCard)

            //update cardAdapter
            freeCardsQuantity--
            binding.currentFreeCards.text = freeCardsQuantity.toString()
            currentFreeCards.remove(randomizedCard)
        }

        // update the view
        cardAdapter!!.notifyDataSetChanged()
    }

    private fun shuffleUsedCards(){
        if(currentFreeCards.isEmpty()){
            //if all the cards have players
            if(usedPlayingCards.isEmpty()){
                Toast.makeText(requireContext(), "There are no cards left to draw!", Toast.LENGTH_SHORT).show()
                return
            }
            //if there are some leftover cards on the stack - place them into non-used cards stack
            for(it in usedPlayingCards) currentFreeCards.add(it)
            usedPlayingCards.clear()
        }
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

    // INFO: WTF, adapterview overridden in CardAdapter class
    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        var card: CardItem = arrayList!!.get(position)

        Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} was selected!", Snackbar.LENGTH_SHORT).show()

/*        var backgroundSet: Boolean = (view!!.background == ContextCompat.getDrawable(requireContext(), R.drawable.card_background_selected)
                || view!!.background == ContextCompat.getDrawable(requireContext(), R.drawable.card_background_selected_on_top))

        if(backgroundSet){
            view!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background_transparent)
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} was unselected!", Snackbar.LENGTH_SHORT).show()
        } else{
            view!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background_selected)
            Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} was selected!", Snackbar.LENGTH_SHORT).show()
        }*/

//        view!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background_selected)
//        var scale: Float = requireContext().resources.displayMetrics.density
        //view!!.setPadding((3 * scale + 0.5f).toInt())
        //view!!.setElevation(24f)

        //view!!.setBackgroundColor(R.color.green_item_selected)

        //============================================

        /*view!!.setOnClickListener(object: View.OnClickListener{
            override fun onClick(view: View?) {
                view!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background_highlight)
                var scale: Float = requireContext().resources.displayMetrics.density
                view!!.setPadding((3 * scale + 0.5f).toInt())
                //Toast.makeText(context, "Card selected", Toast.LENGTH_SHORT).show()
            }
        })*/

        //==============================================




        /*if(view!!.background == resources.getDrawable(R.drawable.card_background_highlight))
            view!!.background = resources.getDrawable(R.drawable.card_background_transparent)
        else
            view!!.background = resources.getDrawable(R.drawable.card_background_highlight)

        parent!![position].background = resources.getDrawable(R.drawable.card_background_highlight)

        cardAdapter!!.notifyDataSetChanged()*/

        /*if(cardGameLogic(cardItem, displayedCard)) {

            // delete card from visible list
            arrayList!!.remove(cardItem)

            // INFO: when a card is chosen, current card on-deck goes to the available cards...
            if (!(displayedCard.getCardType() == HouseType.none && displayedCard.getCardValue() == CardValue.none)) {
                usedPlayingCards!!.add(displayedCard)
            }
            displayedCard = cardItem
            cardItem.getCardId()?.let { binding.displayedCard.setImageResource(it) }

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
        }*/

    }

    // TODO: longClick doesn't response
    override fun onItemLongClick(parent: AdapterView<*>?, view: View, position: Int, id: Long): Boolean {
        var card: CardItem = arrayList!!.get(position)

        //view!!.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_background_selected_on_top)
        Snackbar.make(view, "${card.getCardValueName()} of ${card.getCardType().toString()} will be ON TOP!", Snackbar.LENGTH_SHORT).show()

        return true
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

    private fun wrongCardAlertDialog(cardOnStack: CardItem){
        val alert = AlertDialog.Builder(requireContext())
        alert.setMessage("This card can't be played!\nTry ${cardOnStack.getCardValueName()}s or ${cardOnStack.getCardType()}")
        alert.setPositiveButton(
            "OK"
        ) { dialogInterface, i ->
            // What to do after clicking "positive" button
        }
        alert.create().show()
    }

    private fun playChosenCards(){

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

}