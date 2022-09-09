package com.example.cardshowcase.cardsHandling

enum class HouseType{
    // none, kier, diamonds, trefl, spades, joker
    none, Hearts, Diamonds, Clubs, Spades, Joker
}

enum class CardValue{
    none, two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace, red, black
}

class CardItem {

    private var cardID: Int = 0
    private var type: HouseType = HouseType.none
    private var value: CardValue = CardValue.none

    private var isFunctional: Boolean = false               // informs about functionality of a card (2,3,4,J,Q,K_spades, K_hearts,A)
    private var wasPlayed: Boolean = false                  // variable connected to "isFunctional" one - tells if an action card was already used (there is no penalty)

    private var isSelected: Boolean = false
    private var isSelectedOnTop: Boolean = false

    constructor(imageCard: Int, type: HouseType = HouseType.none, value: CardValue = CardValue.none, function: Boolean = false){
        this.cardID = imageCard
        this.type = type
        this.value = value
        this.isFunctional = function
    }

    fun getCardId(): Int {
        return cardID
    }

    fun getCardType(): HouseType{
        return type
    }

    fun getCardValue(): CardValue{
        return value
    }

    fun getCardValueName(): String{
        when(value){
            CardValue.two -> return "2"
            CardValue.three -> return "3"
            CardValue.four -> return "4"
            CardValue.five -> return "5"
            CardValue.six -> return "6"
            CardValue.seven -> return "7"
            CardValue.eight -> return "8"
            CardValue.nine -> return "9"
            CardValue.ten -> return "10"
            CardValue.jack -> return "Jack"
            CardValue.queen -> return "Queen"
            CardValue.king -> return "King"
            CardValue.ace -> return "Ace"
            else -> return "None"
        }
    }

    fun setCardPlayed(){
        this.wasPlayed = true
    }

    fun resetCardPlayed(){
        this.wasPlayed = false
    }

    fun wasCardPlayed(): Boolean{
        return wasPlayed
    }

    fun setSelected(){
        this.isSelected = true
        this.isSelectedOnTop = false
    }

    fun resetSelected(){
        this.isSelected = false
        this.isSelectedOnTop = false
    }

    fun setSelectedOnTop(){
        this.isSelected = false
        this.isSelectedOnTop = true
    }

    fun isSelected(): Boolean{
        return this.isSelected
    }

    fun isSelectedOnTop(): Boolean{
        return this.isSelectedOnTop
    }

    fun isFunctional(): Boolean{
        return this.isFunctional
    }

    fun setFunctional(){
        isFunctional = true
    }

    fun matches(card: CardItem): Boolean{
        if(type == card.type || value == card.value) return true
        return false
    }

    fun sameAs(card: CardItem): Boolean{
        if(type == card.type && value == card.value) return true
        return false
    }

    fun display(): String{
        return "${getCardValueName()} of ${type}"
    }
}