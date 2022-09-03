package com.example.cardshowcase.cardsHandling

import java.lang.Error
import java.lang.Exception

class Penalty(){

    var type: PenaltyType = PenaltyType.none
    var drawSum: Int = 0
    var numOfRounds: Int = 0
    var demandedHouse: DemandHouse = DemandHouse.none
    var demandedFigure: DemandFigure = DemandFigure.none

    enum class PenaltyType{
        none, draw, drawBack, demandHouse, demandFigure, stop
    }

    enum class DemandHouse{
        none, Clubs, Diamonds, Hearts, Spades
    }

    enum class DemandFigure{
        none, Five, Six, Seven, Eight, Nine, Ten, Queen
    }

    fun reset(){
        type = PenaltyType.none
        drawSum = 0
        numOfRounds = 0
        demandedHouse = DemandHouse.none
        demandedFigure = DemandFigure.none
    }

    fun setDemandHouse(house: HouseType = HouseType.none){
        type = PenaltyType.demandHouse

        drawSum = 0
        demandedFigure = DemandFigure.none
        numOfRounds = 0

        demandedHouse = when(house){
            HouseType.Clubs -> DemandHouse.Clubs
            HouseType.Spades -> DemandHouse.Spades
            HouseType.Diamonds -> DemandHouse.Diamonds
            HouseType.Hearts -> DemandHouse.Hearts
            else -> DemandHouse.none
        }
    }

    fun setDemandFigure(figure: CardValue = CardValue.none){
        reset()
        type = PenaltyType.demandFigure
        demandedFigure = when(figure){
            CardValue.five -> DemandFigure.Five
            CardValue.six -> DemandFigure.Six
            CardValue.seven -> DemandFigure.Seven
            CardValue.eight -> DemandFigure.Eight
            CardValue.nine -> DemandFigure.Nine
            CardValue.ten -> DemandFigure.Ten
            CardValue.queen -> DemandFigure.Queen
            else -> DemandFigure.none
        }
    }

    fun setHaltPlayer(quantity: Int = 0){
        type = PenaltyType.stop
        numOfRounds += quantity
        drawSum = 1

        demandedFigure = DemandFigure.none
        demandedHouse = DemandHouse.none
    }

    fun setDrawCards(value: Int = 2, quantity: Int = 0){
        type = PenaltyType.draw
        drawSum += quantity * value

        demandedFigure = DemandFigure.none
        demandedHouse = DemandHouse.none
        numOfRounds = 0
    }

    fun setDrawBackCards(quantity: Int = 0){
        type = PenaltyType.drawBack
        drawSum += quantity

        demandedFigure = DemandFigure.none
        demandedHouse = DemandHouse.none
        numOfRounds = 0
    }

    fun addDrawedQuantity(quantity: Int = 0){
        if(type.equals(PenaltyType.draw) || type.equals(PenaltyType.drawBack))
            drawSum += quantity
        else{
            throw Exception("ERROR: cannot add draw penalty when in different penalty state!")
        }
    }

    fun enabled(): Boolean{
        if(type == PenaltyType.none)
            return false
        return true
    }

    fun getDemandedFigure(): CardValue{
        when(demandedFigure){
            DemandFigure.Five -> return CardValue.five
            DemandFigure.Six -> return CardValue.six
            DemandFigure.Seven -> return CardValue.seven
            DemandFigure.Eight -> return CardValue.eight
            DemandFigure.Nine -> return CardValue.nine
            DemandFigure.Ten -> return CardValue.ten
            DemandFigure.Queen -> return CardValue.queen
            else -> return CardValue.none
        }
    }

    fun getDemandedHouse(): HouseType{
        when(demandedHouse){
            DemandHouse.Hearts -> return HouseType.Hearts
            DemandHouse.Spades -> return HouseType.Spades
            DemandHouse.Diamonds -> return HouseType.Diamonds
            DemandHouse.Clubs -> return HouseType.Clubs
            else -> return HouseType.none
        }
    }

    fun check(card: CardItem, onStack: CardItem): Boolean{
        when(type){
            PenaltyType.draw -> {
                // TODO: not done for kings
                if(onStack.getCardValue() == card.getCardValue())
                    return true
                else if(onStack.getCardType() == card.getCardType()){
                    if(onStack.getCardValue() == CardValue.three
                        && card.getCardValue() == CardValue.two
                        || onStack.getCardValue() == CardValue.two
                        && card.getCardValue() == CardValue.three)
                        return true
                }
            }
            PenaltyType.demandHouse -> {
                if(getDemandedHouse() == card.getCardType())
                    return true
            }
            PenaltyType.demandFigure -> {
                if(getDemandedFigure() == card.getCardValue())
                    return true
            }
            else -> return false
        }

        return false
    }

}