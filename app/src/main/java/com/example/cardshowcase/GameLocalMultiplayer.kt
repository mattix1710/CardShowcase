package com.example.cardshowcase

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cardshowcase.cardsHandling.*
import com.example.cardshowcase.databinding.GameLocalMultiplayerBinding
import com.example.cardshowcase.playerHandling.Player
import com.google.android.flexbox.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        cardManager = CardManager(requireContext(), binding.currentPenaltyInfo!!)

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

        val layoutManager = FlexboxLayoutManager(requireContext())
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

    @SuppressLint("NotifyDataSetChanged")
    fun newPlayerAlert(firstPlayer: Boolean = false){
        // set new current player index number
        if(!firstPlayer)
            currentPlayerNum = (currentPlayerNum + 1) % players.size        // player counting starts with 0

        // set a curtain in front of the newly chosen cards - for the other players not to see cards
        binding.onTopOfCardsGuard!!.background = ResourcesCompat.getDrawable(resources, R.drawable.background_rectangle, null)
        binding.onTopOfCardsGuard!!.bringToFront()

        cardAdapter = CardAdapter(players[currentPlayerNum].getCards(), this@GameLocalMultiplayer, requireContext())
        recyclerView!!.adapter = cardAdapter

        val newPlayerDialog = AlertDialog.Builder(requireContext())
        val message = players[currentPlayerNum].getName() + " will play its turn!"
        newPlayerDialog.setMessage(message)
        newPlayerDialog.setCancelable(false)
        newPlayerDialog.setPositiveButton("I'm ready!",
            DialogInterface.OnClickListener { dialogInterface, i ->
                cardAdapter!!.notifyDataSetChanged()
                updatePlayerInfo()

                // dismiss curtain and set a cute border with gradient
                binding.onTopOfCardsGuard!!.background = ResourcesCompat.getDrawable(resources, R.drawable.empty_drawable, null)
                binding.cardListView!!.bringToFront()

                if(cardManager.currentPenalty.enabled()){
                    if(cardManager.currentPenalty.toBeReset)
                        cardManager.currentPenalty.reset()
                    else {
                        if (cardManager.currentPenalty.penaltySetter == players[currentPlayerNum]) {
                            // set flag that after this round the penalty will be reset - the end of whole round
                            cardManager.currentPenalty.toBeReset = true
                        }
                        penaltyCheckerAlertDialog()
                    }
                }
            })
        newPlayerDialog.create().show()
    }

    //////////////////////// onItemClick ///////////////////////

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClick(position: Int, view: View) {
        Log.i("Card_click", "card_${position}")

        players[currentPlayerNum].selectedCard(position)

        playCardsButtonUpdate()
        // cannot use .notifyItemChanged - because we don't have an exact information, which other cards could be selected
        cardAdapter!!.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemLongClick(position: Int, view: View) {

        players[currentPlayerNum].selectedCardLong(position)

        playCardsButtonUpdate()
        // cannot use .notifyItemChanged - because we don't have an exact information, which other cards could be selected
        cardAdapter!!.notifyDataSetChanged()
    }

    //////////////////////// playing cards ///////////////////////

    // INFO: probably DONE
    private fun drawCards(numToDraw: Int = 1, fromPenalty: Boolean = false) {
        for(i in 1..numToDraw)
            cardManager.drawCardFromStack(players[currentPlayerNum].getCards(), cardAdapter!!)

        if (fromPenalty) {
            cardManager.currentPenalty.drawSum--
            drawCardPenaltyAlertDialog()
        } else {
            if(players[currentPlayerNum].getCards().last().matches(cardManager.displayedCard))      // if currently draw card could be played
                drawCardRegularAlertDialog()
            else {
                players[currentPlayerNum].resetSelectedCards()
                //cardManager.currentPenalty.reset()
                newPlayerAlert()
                updatePlayerInfo()
            }
        }
    }

    private fun playCards(){
        when(players[currentPlayerNum].playCards(binding.displayedCard,
            binding.currentCardName!!,
            binding.currentPenaltyInfo!!)){
            DemandedTypeSelector.Jack -> {
                demandedFigureAlertDialog()
            }
            DemandedTypeSelector.Ace -> {
                demandedHouseAlertDialog()
            }
            DemandedTypeSelector.wrongCards -> {
                wrongCardAlertDialog()
            }
            else -> {
                playCardsUpdate()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun playCardsUpdate(){
        newPlayerAlert()
        playCardsButtonUpdate()
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

    ///////////////////////////////////
    // TODO: PENALTIES
    private fun penaltyCheckerAlertDialog(){
        val penaltyDialog = AlertDialog.Builder(requireContext())

        var message = "You are forced to "
        var posButtonMess = ""
        var negButtonMess = "Draw cards"

        var canRevenge = false
        var needsContinue = false

        when(cardManager.currentPenalty.type){
            Penalty.PenaltyType.draw -> {
                message += "draw ${cardManager.currentPenalty.drawSum} cards"

                for(card in players[currentPlayerNum].getCards()){      // if current player has revenge card
                    if(cardManager.displayedCard.getCardValue() == CardValue.three){
                        if(card.getCardValue() == CardValue.three
                            || card.getCardValue() == CardValue.two && card.getCardType() == cardManager.displayedCard.getCardType()){
                            canRevenge = true
                            break
                        }
                    } else if(cardManager.displayedCard.getCardValue() == CardValue.two){
                        if(card.getCardValue() == CardValue.two
                            || card.getCardValue() == CardValue.three && card.getCardType() == cardManager.displayedCard.getCardType()){
                            canRevenge = true
                            break
                        }
                    } else if(cardManager.displayedCard.getCardValue() == CardValue.king
                        && cardManager.displayedCard.getCardType() == HouseType.Hearts){
                        if(card.getCardValue() == CardValue.king && card.getCardType() == HouseType.Spades){
                            canRevenge = true
                            // INFO: when playing THIS card - don't forget to set: currentPlayerNum--
                            break
                        }
                    }
                }
            }
            Penalty.PenaltyType.drawBack -> {
                // TODO: check if drawBack
            }
            Penalty.PenaltyType.demandFigure -> {
                message += "play ${cardManager.currentPenalty.demandedFigure}s"

                for(card in players[currentPlayerNum].getCards()){
                    // if current player has revenge card OR same action card
                    if(card.getCardValue() == cardManager.currentPenalty.getDemandedFigure()
                        || card.getCardValue() == CardValue.jack){
                        message += ",\nbut you can play in revenge!"
                        canRevenge = true
                        break
                    }
                }
            }
            Penalty.PenaltyType.demandHouse -> {
                message += "play a card from house of ${cardManager.currentPenalty.demandedHouse}"

                for(card in players[currentPlayerNum].getCards()){
                    // if current player has revenge card OR same action card
                    if(card.getCardType() == cardManager.currentPenalty.getDemandedHouse()
                        || card.getCardValue() == CardValue.ace){
                        message += ",\nbut you can play in revenge!"
                        canRevenge = true
                        break
                    }
                }
            }
            Penalty.PenaltyType.stop -> {
                message += "stop for ${cardManager.currentPenalty.numOfRounds} round/s"
                negButtonMess = "Wait ${cardManager.currentPenalty.numOfRounds} round/s"
                for(card in players[currentPlayerNum].getCards()){
                    if(card.getCardValue() == CardValue.four){
                        canRevenge = true
                        break
                    }else{
                        needsContinue = true
                        break
                    }
                }
            }
            else -> message = "ERROR"
        }

        penaltyDialog.setMessage(message)
        penaltyDialog.setCancelable(false)

        if(canRevenge){
            posButtonMess = "Play in revenge"
            penaltyDialog.setPositiveButton(posButtonMess,
                DialogInterface.OnClickListener{ dialogInterface, i ->
                    // returns to the cardList screen
                    // allow player to choose from his cards
                    players[currentPlayerNum].setRevenge()
                })
        }

        penaltyDialog.setNegativeButton(negButtonMess,
            DialogInterface.OnClickListener{ dialogInterface, i ->
                if(needsContinue){  //if STOP CARD met (4)
                    players[currentPlayerNum].setHaltRoundsPenalty(cardManager.currentPenalty.numOfRounds)
                    cardManager.currentPenalty.reset()
                    newPlayerAlert()
                    updatePlayerInfo()
                    playCardsButtonUpdate()
                }else {
                    drawCards(fromPenalty = true)
                }
            })

        penaltyDialog.create().show()
    }

    /**
     * Informs about the first card that was drawed from the stack
     * and possible options what to do with that.
     * **/
    private fun drawCardPenaltyAlertDialog(){

        val dialogBuilder = AlertDialog.Builder(requireContext())
        val drawedCard = players[currentPlayerNum].getCards().last()
        dialogBuilder.setTitle("You drawed ${drawedCard.getCardValueName()} of ${drawedCard.getCardType()}")

        val message = when(cardManager.currentPenalty.check(drawedCard, cardManager.displayedCard)){
            true -> "It's a match!"
            false -> "This card doesn't match the demand."
        }

        var negButtonMessage = when(cardManager.currentPenalty.type){
            Penalty.PenaltyType.draw -> "Save and draw the rest"
            else -> "Keep it"
        }

        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(message)
        if(cardManager.currentPenalty.check(drawedCard, cardManager.displayedCard)) {
            dialogBuilder.setPositiveButton("Play this card",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    // play the last card
                    players[currentPlayerNum].resetSelectedCards()
                    players[currentPlayerNum].selectedCard(players[currentPlayerNum].getCards().size - 1)
                    players[currentPlayerNum].setRevenge()
                    playCards()
                })
        }
        dialogBuilder.setNegativeButton(negButtonMessage,
            DialogInterface.OnClickListener { dialogInterface, i ->
                //cardManager.currentPenalty.reset()
                players[currentPlayerNum].resetRevenge()
                drawCards(cardManager.currentPenalty.drawSum)
            })

        val alert = dialogBuilder.create().show()
    }

    private fun drawCardRegularAlertDialog(){
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val drawedCard = players[currentPlayerNum].getCards().last()
        dialogBuilder.setTitle("You drawed ${drawedCard.getCardValueName()} of ${drawedCard.getCardType()}")

        val message = "It's a match! What would you like to do?"
        val posButtonMess = "Play it"
        val negButtonMess = "Keep it"

        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton(posButtonMess,
            DialogInterface.OnClickListener { dialogInterface, i ->
                players[currentPlayerNum].resetSelectedCards()
                players[currentPlayerNum].selectedCard(players[currentPlayerNum].getCards().size-1)
                playCards()
            })
        dialogBuilder.setNegativeButton(negButtonMess,
            DialogInterface.OnClickListener { dialogInterface, i ->
                players[currentPlayerNum].resetSelectedCards()
                //cardManager.currentPenalty.reset()
                newPlayerAlert()
                updatePlayerInfo()
            })
    }

    private fun demandedFigureAlertDialog(){
        val singleItems = arrayOf("5", "6", "7", "8", "9", "10")
        if(!cardManager.queenFunctional)
            singleItems.plus("Queen")
        var checkedItem = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.demand_figure_message)
            .setNeutralButton(R.string.demand_figure_cancel){
                    dialog, which ->
                // Returns Jack as a regular card
                cardManager.currentPenalty.reset()
                playCardsUpdate()
            }
            .setPositiveButton(R.string.demand_figure_ok){ dialog, which ->
                // Accept current choice
                when(checkedItem){
                    0 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Five
                    1 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Six
                    2 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Seven
                    3 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Eight
                    4 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Nine
                    5 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Ten
                    6 -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.Queen
                    else -> cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.none
                }
                cardManager.currentPenalty.type = Penalty.PenaltyType.demandFigure
                cardManager.currentPenalty.setPenalty(arrayListOf(cardManager.displayedCard))
                // set info which player demanded this figure - and then, when whole round will come to an end, reset the penalty
                cardManager.currentPenalty.penaltySetter = players[currentPlayerNum]

                playCardsUpdate()
            }
            .setSingleChoiceItems(singleItems, checkedItem){ dialog, which ->
                Log.i("DEMAND", which.toString())
                checkedItem = which
            }
            .setCancelable(false)
            .show()
    }

    private fun demandedHouseAlertDialog(){
        val singleItems = arrayOf(requireContext().resources.getString(R.string.heart_emoji),
            requireContext().resources.getString(R.string.clubs_emoji),
            requireContext().resources.getString(R.string.spades_emoji),
            requireContext().resources.getString(R.string.diamond_emoji))
        var checkedItem = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.demand_house_message)
            .setNeutralButton(R.string.demand_house_cancel){ dialog, which ->
                // Returns Ace as a regular card
                cardManager.currentPenalty.reset()
                playCardsUpdate()
            }
            .setPositiveButton(R.string.demand_house_ok){ dialog, which ->
                // Accept current choice
                when(checkedItem){
                    0 -> cardManager.currentPenalty.demandedHouse = Penalty.DemandHouse.Hearts
                    1 -> cardManager.currentPenalty.demandedHouse = Penalty.DemandHouse.Clubs
                    2 -> cardManager.currentPenalty.demandedHouse = Penalty.DemandHouse.Spades
                    3 -> cardManager.currentPenalty.demandedHouse = Penalty.DemandHouse.Diamonds
                    else -> cardManager.currentPenalty.demandedHouse = Penalty.DemandHouse.none
                }
                cardManager.currentPenalty.type = Penalty.PenaltyType.demandHouse
                // set info which player demanded this figure - and then, when whole round will come to an end, reset the penalty
                cardManager.currentPenalty.penaltySetter = players[currentPlayerNum]
                cardManager.currentPenalty.setPenalty(arrayListOf(cardManager.displayedCard))
                playCardsUpdate()
            }
            .setSingleChoiceItems(singleItems, checkedItem){ dialog, which ->
                checkedItem = which
            }
            .setCancelable(false)
            .show()
    }

    private fun wrongCardAlertDialog(){
        val onTop = cardManager.displayedCard
        var message = "Wrong cards were chosen to display!\n" +
                "Try ${onTop.getCardValueName()}s or ${onTop.getCardType()}"

        if(cardManager.currentPenalty.enabled()){
            message = "Demanded cards are ${cardManager.currentPenalty.whatDemanded(onTop)}"
        }

        val alert = AlertDialog.Builder(requireContext())
        alert.setMessage(message)
        alert.setPositiveButton("Choose another card", null)
        alert.create().show()
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