package com.example.cardshowcase.cardshandling

enum class HouseType{
    none, kier, karo, trefl, pik, joker
}

enum class CardValue{
    none, two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace, joker
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
}