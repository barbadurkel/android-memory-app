package com.example.mymemory.models

import com.example.mymemory.utils.DEFAULT_ICONS

class MemoryGame( private val boardSize: BoardSize ){


    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numFlips = 0
    private var indexOfSingleSelectedCard: Int? = null

    init {
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map{ MemoryCard(it) }
    }
    var foundMatch = false
    fun flipCard(position: Int): Boolean {
        numFlips++
        val card = cards[position]
        // three cases
        // state 1: 0 card previously flipped over => flip over the selected card
        // state 2: 1 card previously flipped over => flip over the selected card + check if the images match
        // state 3: 2 cards previously flipped over => restore cards + flip the selected card (identical to state 1!)
        if (indexOfSingleSelectedCard == null){
            // 0 or 2 previously flipped over
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            // exactly 1 card flipped over
            // !! => force to be not null
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(pos1: Int, pos2: Int): Boolean {
        if (cards[pos1].identifier != cards[pos2].identifier) return false
        cards[pos1].isMatched = true
        cards[pos2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCards() {
       for (card in cards){
           if(!card.isMatched)
             card.isFaceUp = false
       }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(pos: Int): Boolean {
        return cards[pos].isFaceUp
    }

    fun getNumMoves(): Int {
        return numFlips / 2
    }
}