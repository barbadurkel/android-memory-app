package com.example.mymemory.models

data class MemoryCard(
    val identifier: Int,
    var isFaceUp: Boolean = false,   //  var mutable, val immutable
    var isMatched: Boolean = false
)