package com.example.cardshowcase.cardshandling

enum class HouseType{
    // none, kier, diamonds, trefl, spades, joker
    none, Hearts, Diamonds, Clubs, Spades, Joker
}

enum class CardValue{
    none, two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace, red, black
}

class CardItem {

    private var cardID: Int? = 0
    private var type: HouseType = HouseType.none
    private var value: CardValue = CardValue.none

    constructor(cards: Int?, type: HouseType = HouseType.none, value: CardValue = CardValue.none){
        this.cardID = cards
        this.type = type
        this.value = value
    }

    fun getCardId(): Int? {
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
}