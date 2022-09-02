package com.example.cardshowcase.cardsHandling

import java.lang.Error
import java.lang.Exception

class Penalty(){

    var type: PenaltyType = PenaltyType.none
    var drawSum: Int = 0
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
        demandedHouse = DemandHouse.none
        demandedFigure = DemandFigure.none
    }

    fun setDemandHouse(house: HouseType = HouseType.none){
        type = PenaltyType.demandHouse

        drawSum = 0
        demandedFigure = DemandFigure.none

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

    fun setDrawCards(quantity: Int = 0){
        type = PenaltyType.draw
        drawSum += quantity

        demandedFigure = DemandFigure.none
        demandedHouse = DemandHouse.none
    }

    fun setDrawBackCards(quantity: Int = 0){
        type = PenaltyType.drawBack
        drawSum += quantity

        demandedFigure = DemandFigure.none
        demandedHouse = DemandHouse.none
    }

    fun addDrawedQuantity(quantity: Int = 0){
        if(type.equals(PenaltyType.draw) || type.equals(PenaltyType.drawBack))
            drawSum += quantity
        else{
            throw Exception("ERROR: cannot add draw penalty when in different penalty state!")
        }
    }

}