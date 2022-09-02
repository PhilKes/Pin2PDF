package com.philkes.pin2pdf

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import com.philkes.pin2pdf.storage.database.DBService
import java.util.function.Consumer


class Settings(val context: Context, val dbService: DBService) {
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
            val preferenceEdit = sharedPreferences.edit()
            preferenceEdit.putString(context.resources.getString(R.string.key_user_name), value)
            preferenceEdit.apply()
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

    suspend fun resetPins(onSuccess: Consumer<Void?>?){
        dbService.clearAll()
        context.filesDir.listFiles()?.forEach { it.deleteRecursively() }
        onSuccess?.accept(null)
    }

    /**
     * Show Dialog to enter Pinterest Username and store it in SharedPreferences
     */
    fun showUsernameInputDialog(activity: Activity, onSuccess: Consumer<String?>?) {

        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.edit_username, null)
        val editUsernameText = view.findViewById<EditText>(R.id.edit_username_text)
        editUsernameText.setText(username)
        editUsernameText.setSelection(editUsernameText.text.length)
        editUsernameText.isFocusableInTouchMode= true
        with(AlertDialog.Builder(activity)) {
            setTitle("Please enter your Pinterest Username")
            setView(view)
            setCancelable(true)
            setNegativeButton("Cancel", null)
            setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
                username = editUsernameText.text.toString()
                // DBService.getInstance(context).clearAll();
                onSuccess?.accept(editUsernameText.text.toString())
            }
            show()
        }
        editUsernameText.requestFocus();
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }
}