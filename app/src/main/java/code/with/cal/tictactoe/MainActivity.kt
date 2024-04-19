// Based on the materials from this repository:
// https://github.com/codeWithCal/TicTacToeAndroid/tree/master

// Idea to read input from the dialog box:
// https://www.geeksforgeeks.org/how-to-create-a-custom-alertdialog-in-android/

// Single item selection in dialog boxes:
// https://www.geeksforgeeks.org/alert-dialog-with-singleitemselection-in-android/

// Specific dialog box usage:
// https://developer.android.com/develop/ui/views/components/dialogs

package code.with.cal.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import code.with.cal.tictactoe.databinding.ActivityMainBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    // possible turn symbols (X or O) stored in enum as constants
    // to keep track of who's turn it is
    enum class Turn {
        CROSS,
        NOUGHT
    }

    // possible turn symbols stored in variables
    // for further display on the screen,
    // game outcomes required for checking game end logic
    companion object {
        const val CROSS = "X"
        const val NOUGHT = "O"
        const val CROSS_VICTORY = 1
        const val NOUGHT_VICTORY = -1
        const val DRAW = 0
        const val CONTINUATION = 2
    }

    // initialize the scores of players with 0
    private var crossesScore = 0
    private var noughtsScore = 0

    // in this game crosses always start
    private var currentTurn = Turn.CROSS

    // set the mode of the game to 'unknown'
    // until the user selects PvP or PvC
    private var mode = ""

    // initialize two lists with all the board fields
    // and the available ones for the move
    private var boardList = mutableListOf<Button>()
    private var freeFields = mutableListOf<Button>()

    // declare a list of possible game outcomes
    private val gameOutcomes = listOf(CROSS_VICTORY, NOUGHT_VICTORY, DRAW)

    // allows to interact with the views
    private lateinit var binding : ActivityMainBinding

    // initializes the board and pop-up messages to be able to play
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBoard()

        // asks user to enter its name
        val customLayout: View = layoutInflater.inflate(R.layout.text_box, null)
        AlertDialog.Builder(this)
            .setTitle("What's your name?")
            .setView(customLayout)
            .setPositiveButton("OK") {
                    _,_ ->
                val editText = customLayout.findViewById<EditText>(R.id.editText)
                val playerName = getPlayerName(editText.text.toString())

                // welcomes user
                AlertDialog.Builder(this)
                    .setTitle("Greetings!")
                    .setMessage("Welcome $playerName !")
                    .setPositiveButton("OK") {
                            _,_ ->
                        val items = arrayOf("PvP", "PvC")

                        // lets the user choose game mode
                        AlertDialog.Builder(this)
                            .setTitle("Choose your destiny")
                            .setSingleChoiceItems(items, -1) { dialog, which ->
                                mode = items[which]
                            }
                            .setPositiveButton("OK") { _,_ -> }
                            .setCancelable(false)
                            .show()
                    }
                    .setCancelable(false)
                    .show()
            }
            .setCancelable(false)
            .show()
    }

    // initializes the board to fill the list of
    // all the board fields and the ones that are available for the move
    // with possible clickable buttons
    private fun initBoard() {
        boardList = mutableListOf()
        freeFields = mutableListOf()

        val fields = listOf(
            binding.a1, binding.a2, binding.a3,
            binding.b1, binding.b2, binding.b3,
            binding.c1, binding.c2, binding.c3
        )

        for (field in fields) {
            boardList.add(field)
            freeFields.add(field)
        }

        // indicates that crosses start the game
        currentTurn = Turn.CROSS
    }

    // adds necessary symbols to the board
    private fun addToBoard(button: Button) {
        // tries to make a correct move
        if (button.text != "") return

        // adds X symbol to the board and passes the turn to noughts
        if (currentTurn == Turn.CROSS) {
            button.text = CROSS
            currentTurn = Turn.NOUGHT
        }

        // adds O symbol to the board and passes the turn to crosses
        else if (currentTurn == Turn.NOUGHT) {
            button.text = NOUGHT
            currentTurn = Turn.CROSS
        }
    }

    // checks if the board is already full
    private fun fullBoard(): Boolean {
        for (button in boardList)
            if (button.text == "")
                return false
        return true
    }

    // resets board to the initial state
    private fun resetBoard() {
        for (field in boardList)
            field.text = ""

        initBoard()
    }

    // covers board tapping logic
    fun boardTapped(view: View) {
        // first, user gets a turn

        // in case of a non-button object or unavailable field
        // nothing on the screen happens, user should tap again
        if (view !is Button || view !in freeFields) return

        // registers a turn and displays it on the screen
        var outcome = makeTurn(view)

        // verifies that it is the end of the game
        // and restricts computer making a move,
        // otherwise passes the turn to computer
        if (outcome in gameOutcomes) return

        // now, computer gets a turn
        if (mode == "PvC") {
            // generates an index and
            // chooses a random available field for the computer
            val index = Random.nextInt(0, freeFields.size)
            val freeField = freeFields[index]

            // registers a turn and displays it on the screen
            outcome = makeTurn(freeField)

            // verifies that it is the end of the game
            // and restricts user making a move,
            // otherwise passes the turn to user
            if (outcome in gameOutcomes) return
        }
    }

    // X or O makes a turn
    private fun makeTurn(freeField: Button): Int {
        // updates the board with computer's move
        addToBoard(freeField)
        freeFields.remove(freeField)

        // checks for the victory of crosses
        if (checkForVictory(CROSS)) {
            crossesScore++
            result("Crosses Win!")
            return CROSS_VICTORY
        }

        // checks for the victory of noughts
        else if (checkForVictory(NOUGHT)) {
            noughtsScore++
            result("Noughts Win!")
            return NOUGHT_VICTORY
        }

        // checks for the draw
        if (fullBoard()) {
            result("Draw")
            return DRAW
        }

        // otherwise indicates that the game is not over
        return CONTINUATION
    }

    // checks if a button already contains X or O symbol
    private fun match(button: Button, symbol : String): Boolean {
        return button.text == symbol
    }

    // checks if it is a victory for any of the players
    private fun checkForVictory(symbol: String): Boolean {
        // Horizontal Victory
        val row1 = match(binding.a1, symbol) && match(binding.a2, symbol) && match(binding.a3, symbol)
        val row2 = match(binding.b1, symbol) && match(binding.b2, symbol) && match(binding.b3, symbol)
        val row3 = match(binding.c1, symbol) && match(binding.c2, symbol) && match(binding.c3, symbol)

        // Vertical Victory
        val column1 = match(binding.a1, symbol) && match(binding.b1, symbol) && match(binding.c1, symbol)
        val column2 = match(binding.a2, symbol) && match(binding.b2, symbol) && match(binding.c2, symbol)
        val column3 = match(binding.a3, symbol) && match(binding.b3, symbol) && match(binding.c3, symbol)

        // Diagonal Victory
        val diagonal1 = match(binding.a1, symbol) && match(binding.b2, symbol) && match(binding.c3, symbol)
        val diagonal2 = match(binding.a3, symbol) && match(binding.b2, symbol) && match(binding.c1, symbol)

        return row1 || row2 || row3 || column1 || column2 || column3 || diagonal1 || diagonal2
    }

    // builds a game outcome message and displays it on the screen
    private fun result(title: String) {
        val message = "\nCrosses $crossesScore\n\nNoughts $noughtsScore"
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Reset") {
                    _,_ -> resetBoard()
            }
            .setCancelable(false)
            .show()
    }

    // gets the name of the player for the greetings message
    private fun getPlayerName(playerName: String) : String {
        return playerName
    }
}