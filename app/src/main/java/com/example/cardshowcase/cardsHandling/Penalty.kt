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
        type = PenaltyType.demandFigure

        drawSum = 0
        demandedHouse = DemandHouse.none
        numOfRounds = 0

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

        demandedFigure = DemandFigure.none
        demandedHouse = DemandHouse.none
        drawSum = 0
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

}