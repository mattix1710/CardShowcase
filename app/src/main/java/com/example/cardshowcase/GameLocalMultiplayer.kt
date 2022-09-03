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
    private var currentPlayerNum: Int = 2
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
        binding.onTopOfCardsGuard!!.background = ResourcesCompat.getDrawable(resources, R.drawable.background_rectangle, null)
        binding.onTopOfCardsGuard!!.bringToFront()

        cardAdapter = CardAdapter(players[currentPlayerNum].getCards(), this@GameLocalMultiplayer, requireContext())
        recyclerView!!.adapter = cardAdapter

//        var inflater = LayoutInflater.from(requireContext())
//        // TODO: rethink the size of inflated message - too small to cover the other players cards
//        var view = inflater.inflate(R.layout.new_player_dialog, null)

        val newPlayerDialog = AlertDialog.Builder(requireContext())
        val message = players[currentPlayerNum].getName() + " will play its turn!"
        //newPlayerDialog.setTitle(title)
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
                    // TODO: display penalty ALERT DIALOG for the new player
                    penaltyCheckerAlertDialog()
                }
            })
        newPlayerDialog.create().show()
    }

    //////////////////////// onItemClick ///////////////////////

    override fun onItemClick(position: Int, view: View) {
        Log.i("Card_click", "card_${position}")

        players[currentPlayerNum].selectedCard(position)

        playCardsButtonUpdate()
        // cannot use .notifyItemChanged - because we don't have an exact information, which other cards could be selected
        cardAdapter!!.notifyDataSetChanged()
    }

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
            if(players[currentPlayerNum].getCards().last() == cardManager.displayedCard)
                drawCardRegularAlertDialog()
            else {
                players[currentPlayerNum].resetSelectedCards()
                cardManager.currentPenalty.reset()
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
//        if(players[currentPlayerNum].playCards(binding.displayedCard,
//                binding.currentCardName!!,
//                binding.currentPenaltyInfo!!)) {      // if cards played were good - proceed to the next player
//            newPlayerAlert()            // set alert about new players turn
//            playCardsButtonUpdate()
//        }

//        playCardsButtonUpdate()
//        updatePlayerInfo()
//        cardAdapter!!.notifyDataSetChanged()
    }

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
            else -> "Save and exit"
        }

        dialogBuilder.setCancelable(false)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("Play this card",
            DialogInterface.OnClickListener{ dialogInterface, i ->
                // play the last card
                players[currentPlayerNum].resetSelectedCards()
                players[currentPlayerNum].selectedCard(players[currentPlayerNum].getCards().size-1)
                players[currentPlayerNum].setRevenge()
                playCards()
            })
        dialogBuilder.setNegativeButton(negButtonMessage,
            DialogInterface.OnClickListener { dialogInterface, i ->
                cardManager.currentPenalty.reset()
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
                cardManager.currentPenalty.reset()
                newPlayerAlert()
                updatePlayerInfo()
            })
    }

    private fun demandedFigureAlertDialog(){
        var singleItems = arrayOf("5", "6", "7", "8", "9", "10")
        if(cardManager.queenFunctional)
            singleItems.plus("Queen")
        var checkedItem = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.demand_figure_message)
            .setNeutralButton(R.string.demand_figure_cancel){
                    dialog, which ->
                // Returns Jack as a regular card
                cardManager.currentPenalty.demandedFigure = Penalty.DemandFigure.none
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
                cardManager.currentPenalty.demandedHouse = Penalty.DemandHouse.none
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