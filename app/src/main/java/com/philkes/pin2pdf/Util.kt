package com.philkes.pin2pdf

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.EditText
import java.util.function.Consumer

object Util {
    /**
     * Returns the Domain name of the given URL
     */
    @JvmStatic
    fun getUrlDomainName(url: String): String {
        var domainName: String = url
        var index = domainName.indexOf("://")
        if (index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3)
        }
        index = domainName.indexOf('/')
        if (index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index)
        }

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        domainName = domainName.replaceFirst("^www.*?\\.".toRegex(), "")
        return domainName
    }

    /**
     * Show Dialog to enter Pinterest Username and store it in SharedPreference
     */
    @JvmStatic
    fun showUsernameInputDialog(context: Context, onSuccess: Consumer<String?>?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Please enter your Pinterest Username")
        val prefKey = context.resources.getString(R.string.app_name)
        val sharedPref = context.getSharedPreferences(prefKey, Context.MODE_PRIVATE)
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.edit_username, null)
        val editUsernameText = view.findViewById<EditText>(R.id.edit_username_text)
        editUsernameText.setText(sharedPref.getString(prefKey, ""))
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            val preferenceEdit = sharedPref.edit()
            val username = editUsernameText.text.toString()
            preferenceEdit.putString(context.resources.getString(R.string.key_user_name), username)
            preferenceEdit.apply()
            // DBService.getInstance(context).clearAll();
            onSuccess?.accept(username)
        }
        builder.setCancelable(false)
        builder.show()
    }
}