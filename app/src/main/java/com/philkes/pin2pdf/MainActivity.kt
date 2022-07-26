package com.philkes.pin2pdf

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.fragment.boards.BoardFragment
import com.philkes.pin2pdf.storage.database.DBService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var dbService: DBService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Show menu item for editing the username + clearing all data
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options, menu)
        with(menu.findItem(R.id.option_username_edit)) {
            setOnMenuItemClickListener {
                Util.showUsernameInputDialog(this@MainActivity) { username: String? ->
                    // Reload Fragment to load new user
                    (supportFragmentManager.findFragmentById(R.id.boardFragment) as BoardFragment?)!!.loadUser(
                        username
                    )
                }
                true
            }
        }

        with(menu.findItem(R.id.option_clear_pin_data)) {
            setOnMenuItemClickListener {
                lifecycleScope.launch {
                    dbService.clearAll()
                    Util.showUsernameInputDialog(this@MainActivity) { username: String? ->
                        // Reload Fragment to load new user
                        (supportFragmentManager.findFragmentById(R.id.boardFragment) as BoardFragment?)!!.loadUser(
                            username
                        )
                    }
                }
                true
            }
        }

        return true
    }
}