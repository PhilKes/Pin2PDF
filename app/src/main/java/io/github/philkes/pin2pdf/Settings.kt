package io.github.philkes.pin2pdf

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.content.SharedPreferences
import android.util.SparseBooleanArray
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import io.github.philkes.pin2pdf.api.pinterest.PinterestAPI
import io.github.philkes.pin2pdf.api.pinterest.model.BoardResponse
import io.github.philkes.pin2pdf.storage.database.DBService
import java.util.function.Consumer


class Settings(val context: Context, val dbService: DBService, val pinterestAPI: PinterestAPI) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.resources.getString(R.string.app_name),
        Context.MODE_PRIVATE
    )

    var username: String?
        get() {
            return sharedPreferences.getString(
                context.resources.getString(R.string.key_user_name),
                null
            )
        }
        set(value) {
            sharedPreferences.edit()
                .putString(context.resources.getString(R.string.key_user_name), value).apply()
        }

    private val NAME_ID_DELIMITER = "€€"
    private val BOARD_DELIMITER = "$§"
    var userBoards: List<BoardResponse>?
        get() {
            val boardsString = sharedPreferences.getString(
                context.resources.getString(R.string.key_user_boards),
                null
            )
            if (boardsString == null)
                return null
            // Encode Boards name + id in one String
            return boardsString.split(BOARD_DELIMITER).map {
                val split = it.split(NAME_ID_DELIMITER)
                BoardResponse(split[0], "", split[1])
            }
        }
        set(value) {
            val edit = sharedPreferences.edit()
            if (value == null) {
                edit.putString(context.resources.getString(R.string.key_user_boards), null).apply()
            } else {
                edit.putString(
                    context.resources.getString(R.string.key_user_boards),
                    value.joinToString(BOARD_DELIMITER) { "${it.name}${NAME_ID_DELIMITER}${it.id}" })
                    .apply()
            }
        }

    suspend fun resetUser(activity: Activity?) {
        username = null
        // Clear Database + local PDF Files
        dbService.clearAll()
        context.filesDir.listFiles()?.forEach { it.deleteRecursively() }
        val i = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity?.finish()
        context.startActivity(i)
    }

    suspend fun resetPins(onSuccess: Consumer<Void?>?) {
        dbService.clearAll()
        context.filesDir.listFiles()?.forEach { it.deleteRecursively() }
        onSuccess?.accept(null)
    }

    /**
     * Show Dialog to enter Pinterest Username and store it in SharedPreferences
     */
    fun showUserAndBoardInput(
        activity: Activity,
        onSuccess: Consumer<List<BoardResponse>>?,
    ) {

        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.edit_username, null)
        val editUsernameText = view.findViewById<EditText>(R.id.edit_username_text)
        editUsernameText.setText(username)
        editUsernameText.setSelection(editUsernameText.text.length)
        editUsernameText.isFocusableInTouchMode = true
        val dialog = with(AlertDialog.Builder(activity)) {
            setTitle("Please enter your Pinterest Username")
            setView(view)
            setCancelable(true)
            setNegativeButton("Cancel", null)
            setPositiveButton("OK", null)
            create()
        }
        dialog.setOnShowListener {
            val button: Button =
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val newUsername = editUsernameText.text.toString()
                pinterestAPI
                    .requestBoardsOfUser(newUsername, { boards: List<BoardResponse> ->
                        showBoardsSelection(activity, dialog, boards) {
                            username = newUsername
                            onSuccess?.accept(it)
                        }
                    }, { error ->
                        editUsernameText.error =
                            "Pinterest User not found, please enter an existing User!"
                    })

            }
            editUsernameText.setOnKeyListener { v, keyCode, event ->
                if (event.action === KeyEvent.ACTION_UP && keyCode === KeyEvent.KEYCODE_ENTER) {
                    dialog.getButton(BUTTON_POSITIVE).performClick()
                    return@setOnKeyListener true
                }
                false
            }
        }
        dialog.show()
        editUsernameText.requestFocus();
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }


    /**
     * Show Dialog to enter Pinterest Username and store it in SharedPreferences
     */
    fun showBoardsSelection(
        activity: Activity,
        dialog: AlertDialog,
        boards: List<BoardResponse>,
        onSuccess: Consumer<List<BoardResponse>>?
    ) {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.select_boards, null)
        val boardsList = view.findViewById<ListView>(R.id.boards_list).apply {
            adapter =
                ArrayAdapter(activity, android.R.layout.simple_list_item_multiple_choice, boards)
            choiceMode = ListView.CHOICE_MODE_MULTIPLE
            for (i in boards.indices) {
                setItemChecked(i, true)
            }
        }

        with(AlertDialog.Builder(activity)) {
            setTitle("Please select the boards containing recipes")
            setView(view)
            setCancelable(true)
            setNegativeButton("Cancel", null)
            setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
                val checkedBoards = mutableListOf<BoardResponse>()
                val len: Int = boardsList.count
                val checked: SparseBooleanArray = boardsList.checkedItemPositions
                for (i in 0 until len) if (checked[i]) {
                    checkedBoards.add(boards[i])
                }
                userBoards = checkedBoards
                onSuccess?.accept(checkedBoards)
            }
            dialog.dismiss()
            show()
        }
    }


}