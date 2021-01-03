package com.example.mymemory

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemory.models.BoardSize
import com.example.mymemory.models.MemoryCard
import com.example.mymemory.models.MemoryGame
import com.example.mymemory.utils.DEFAULT_ICONS
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }
    // late init var means the vars will be set in the init method of onCreate
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh -> {
                // setup the game again
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?", null, View.OnClickListener {
                        setUpBoard()
                    })
                } else {
                    setUpBoard()
                }
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        var boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            // set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setUpBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun setUpBoard(){
        when (boardSize){
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4x2"
                tvNumPairs.text = "Pairs: 0/4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Easy: 6x3"
                tvNumPairs.text = "Pairs: 0/9"
            }
            // he does not like HARD apparently
           // BoardSize.HARD {
              //  tvNumMoves.text = "Easy: 6x4"
              //  tvNumPairs.text = "Pairs: 12"
            //}
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progoress_none))
        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        // Error checking aka "you're not allowed to do this"
        if(memoryGame.haveWonGame()){
            // Alert the user of invalid move
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            // Alert the user of invalid move
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_LONG).show()
            return
        }

        // Actually flip over the card
        if(memoryGame.flipCard(position)){
                Log.i(TAG, "Found a match! Number of pairs found ${memoryGame.numPairsFound}")
                var color = ArgbEvaluator().evaluate(
                    memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                    ContextCompat.getColor(this, R.color.color_progoress_none),
                    ContextCompat.getColor(this, R.color.color_progoress_full)
                ) as Int
                tvNumPairs.setTextColor(color)
                tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
                if (memoryGame.haveWonGame()){
                    Snackbar.make(clRoot, "You won! congratulations.", Snackbar.LENGTH_LONG).show()
                }
            }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}
