package com.philkes.pin2pdf

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.fragment.boards.BoardFragment
import com.philkes.pin2pdf.storage.local.service.DBService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DBService.getInstance(this)
        PinterestAPI.getInstance(this)
    }

    /**
     * Show menu item for editing the username + clearing all data
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options, menu)
        val usernameEditItem = menu.findItem(R.id.option_username_edit)
        usernameEditItem.setOnMenuItemClickListener { item: MenuItem? ->
            Util.showUsernameInputDialog(this) { username: String? ->
                // Reload Fragment to load new user
                (supportFragmentManager.findFragmentById(R.id.boardFragment) as BoardFragment?)!!.loadUser(
                    username
                )
            }
            true
        }
        val clearPinItem = menu.findItem(R.id.option_clear_pin_data)
        clearPinItem.setOnMenuItemClickListener { item: MenuItem? ->
            DBService.getInstance(this).clearAll()
            Util.showUsernameInputDialog(this) { username: String? ->
                // Reload Fragment to load new user
                (supportFragmentManager.findFragmentById(R.id.boardFragment) as BoardFragment?)!!.loadUser(
                    username
                )
            }
            true
        }
        return true
    }
}