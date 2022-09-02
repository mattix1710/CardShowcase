package com.example.cardshowcase

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
    private var penaltyArise: Boolean = false
    //
    //////////////////////////////////////////////

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //////////////////////////////////////////////
    // private player card info
    //
    private var currentPlayerNum: Int = 0
    private var players = ArrayList<Player>()
    private var playerCardNum = ArrayList<TextView>()
    private var playerCardTile = ArrayList<CardView>()

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

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_GameLocalMultiplayer_to_MainFragment)
//        }

        // initialize players and draw cards to their hands
        players.add(Player("Player 1", playerNumber = 0, requireContext(), cardManager))
        players.add(Player("Player 2", playerNumber = 1, requireContext(), cardManager))
        players.add(Player("Player 3", playerNumber = 2, requireContext(), cardManager))
        players.add(Player("Player 4", playerNumber = 3, requireContext(), cardManager))

        playerCardNum.add(binding.player1CardsInfo!!)
        playerCardNum.add(binding.player2CardsInfo!!)
        playerCardNum.add(binding.player3CardsInfo!!)
        playerCardNum.add(binding.player4CardsInfo!!)

        playerCardTile.add(binding.cardViewTile1!!)
        playerCardTile.add(binding.cardViewTile2!!)
        playerCardTile.add(binding.cardViewTile3!!)
        playerCardTile.add(binding.cardViewTile4!!)

        //////////////////////////////////////////////////////

        recyclerView = binding.cardListView

        //cardAdapter = CardAdapter(players[currentPlayerNum].getCards(), this@GameLocalMultiplayer, requireContext())
        newPlayerAlert(true)
        //cardAdapter = CardAdapter(playerCards!!, this@GameLocalMultiplayer, requireContext())
        //cardAdapter = CardAdapter(players[0].playerCards, this@GameLocalMultiplayer, requireContext())
        //recyclerView!!.adapter = cardAdapter

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
        binding.drawCardButton!!.setOnClickListener { drawCards() }
        binding.playCardbutton!!.setOnClickListener{ playCards() }
        playCardsButtonUpdate()

        //draw a card for beginning a game
        cardManager?.setInitialDisplayedCard(binding.displayedCard, binding.currentCardName!!)
    }

    fun newPlayerAlert(firstPlayer: Boolean = false){
        // set new current player index number
        if(!firstPlayer)
            currentPlayerNum = (currentPlayerNum + 1) % players.size        // player counting starts with 0

        // set a curtain in front of the newly chosen cards - for the other players not to see cards
        binding.onTopOfCardsGuard!!.background = resources.getDrawable(R.drawable.background_rectangle)
        binding.onTopOfCardsGuard!!.bringToFront()

        cardAdapter = CardAdapter(players[currentPlayerNum].getCards(), this@GameLocalMultiplayer, requireContext())
        recyclerView!!.adapter = cardAdapter

        var inflater = LayoutInflater.from(requireContext())
        // TODO: rethink the size of inflated message - too small to cover the other players cards
        var view = inflater.inflate(R.layout.new_player_dialog, null)

        var newPlayerDialog = AlertDialog.Builder(requireContext())
        var title = players[currentPlayerNum].getName() + " will play its turn!"
        newPlayerDialog.setTitle(title)
        newPlayerDialog.setView(view)
        newPlayerDialog.setCancelable(false)
        newPlayerDialog.setPositiveButton("I'm ready!",
            DialogInterface.OnClickListener { dialogInterface, i ->
                cardAdapter!!.notifyDataSetChanged()
                // TODO: change view of new current players tile
                updatePlayerInfo()

                // dismiss curtain and set a cute border with gradient
                binding.onTopOfCardsGuard!!.background = resources.getDrawable(R.drawable.empty_drawable)
                binding.cardListView!!.bringToFront()
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

    //////////////////////// playing cards ///////////////////////

    private fun drawCards(){
        cardManager.drawCardFromStack(players[currentPlayerNum].getCards(), cardAdapter!!)
        players[currentPlayerNum].resetSelectedCards()
        newPlayerAlert()
        updatePlayerInfo()
    }

    private fun playCards(){
        if(players[currentPlayerNum].playCards(binding.displayedCard, binding.currentCardName!!))       // if cards played were good - proceed to the next player
            newPlayerAlert()            // set alert about new players turn

        updatePlayerInfo()
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

    private fun checkForPenalties(){
        if(penaltyArise){

        }else{

        }
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

        var currIndex = 0
        for(it in playerCardTile){
            if(currIndex == currentPlayerNum)
                it.setCardBackgroundColor(resources.getColor(R.color.player_chosen))
            else
                it.setCardBackgroundColor(resources.getColor(R.color.dark_green_tile_background))
            currIndex++
        }

        currIndex = 0
        for(it in playerCardNum){
            it.text = players[currIndex].getCards().size.toString()
            currIndex++
        }

    }

}