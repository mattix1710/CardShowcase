package com.example.cardshowcase

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cardshowcase.cardsHandling.*
import com.example.cardshowcase.databinding.GameLocalMultiplayerBinding
import com.example.cardshowcase.playerHandling.Player
import com.google.android.flexbox.*
import org.w3c.dom.Text


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GameLocalMultiplayer : Fragment(), CardListListener {

    private var _binding: GameLocalMultiplayerBinding? = null

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
    private var currentPlayerNum: Int = -1
    private var players = ArrayList<Player>()

    private var cardAdapter: CardAdapter? = null
    //
    //////////////////////////////////////////////

    private var recyclerView: RecyclerView? = null

    // onCreateView - trying to use only for view inflaters
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = GameLocalMultiplayerBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set initial resources
        cardManager = CardManager(requireContext())

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_GameLocalMultiplayer_to_MainFragment)
        }

        // initialize players and draw cards to their hands
        players.add(Player("Player 1", playerNumber = 0, requireContext(), cardManager))
        players.add(Player("Player 2", playerNumber = 1, requireContext(), cardManager))
        players.add(Player("Player 3", playerNumber = 2, requireContext(), cardManager))
        players.add(Player("Player 4", playerNumber = 3, requireContext(). cardManager))
        //players.get(0).initPlayerCards(cardManager.randomizeCardList())

        newPlayerAlert()
        //cardAdapter = CardAdapter(playerCards!!, this@GameLocalMultiplayer, requireContext())
        //cardAdapter = CardAdapter(players[0].playerCards, this@GameLocalMultiplayer, requireContext())

        recyclerView = binding.cardListView
        recyclerView!!.adapter = cardAdapter

        var layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        //layoutManager.alignContent = AlignContent.FLEX_START          // cannot be used in FlexboxLayoutManager
        layoutManager.alignItems = AlignItems.STRETCH
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.flexWrap = FlexWrap.WRAP

        recyclerView!!.layoutManager = layoutManager

        //////////////////////////////////////////////////
        // binding button with onClickListeners
        //
        binding.drawCardButton!!.setOnClickListener {
            cardManager.drawCardFromStack(players[currentPlayerNum].playerCards, cardAdapter!!)
            updatePlayerInfo()
        }
        binding.drawCard.setOnClickListener { newPlayerAlert() }//drawNewCard() }
        binding.playCardbutton!!.setOnClickListener{ playCards() }
        playCardsButtonUpdate()

        //draw a card for beginning a game
        cardManager?.setInitialDisplayedCard(binding.displayedCard, binding.currentCardName!!)
    }

    fun newPlayerAlert(){
        // set new current player index number
        currentPlayerNum = (currentPlayerNum + 1) % players.size        // player counting starts with 0

        var inflater = LayoutInflater.from(requireContext())
        // TODO: rethink the size of inflated message - too small to cover the other players cards
        var view = inflater.inflate(R.layout.new_player_dialog, null)
        //var currentPlayerInfo = findViewById(R.id.newPlayerInfo) as TextView

        var newPlayerDialog = AlertDialog.Builder(requireContext())
        var title = players[currentPlayerNum].getName() + " will play its turn!"
        newPlayerDialog.setTitle(title)
        newPlayerDialog.setView(view)
        newPlayerDialog.setPositiveButton("I'm ready!",
            DialogInterface.OnClickListener { dialogInterface, i ->
                // TODO: display new current players cards
                cardAdapter = CardAdapter(players[currentPlayerNum].getCards(), this@GameLocalMultiplayer, requireContext())
                cardAdapter!!.notifyDataSetChanged()
            })
        newPlayerDialog.create().show()
    }

    //////////////////////// onItemClick ///////////////////////

    override fun onItemClick(position: Int, view: View) {
        Log.i("Card_click", "card_${position}")

        players[currentPlayerNum]
        players[currentPlayerNum].selectedCard(position)

        playCardsButtonUpdate()
        cardAdapter!!.notifyDataSetChanged()
    }

    override fun onItemLongClick(position: Int, view: View) {

        players[currentPlayerNum].selectedCardLong(position)

        playCardsButtonUpdate()
        cardAdapter!!.notifyDataSetChanged()
    }

    // TODO: delete?? (deprecated fun - already used in CardManager)
    private fun drawNewCard() {

        // while new cards are drawed, the state of current cards is reset
        for(card in players[0].playerCards) card.resetSelected()
        players[0].resetSelectedCards()


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
                players[0].playerCards.add(randomizedCard)

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
        players[currentPlayerNum].playCards(binding.displayedCard, binding.currentCardName!!)

        updatePlayerInfo()
        newPlayerAlert()            // set alert about new players turn
        cardAdapter!!.notifyDataSetChanged()
    }

    /** changes "Play a card" button state **/
    private fun playCardsButtonUpdate(){
        players[currentPlayerNum].playCardsButtonUpdate(binding.playCardbutton!!)
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

    // TODO: updatePlayerInfo
    private fun updatePlayerInfo(){
        when(currentPlayerNum){
            0 -> binding.player1CardsInfo!!.text = players[currentPlayerNum].playerCards.size.toString()
            1 -> binding.player2CardsInfo!!.text = players[currentPlayerNum].playerCards.size.toString()
            2 -> binding.player3CardsInfo!!.text = players[currentPlayerNum].playerCards.size.toString()
            3 -> binding.player4CardsInfo!!.text = players[currentPlayerNum].playerCards.size.toString()
            else -> Log.i("PLAYER_ERROR", "There is no such player!")
        }
    }

}