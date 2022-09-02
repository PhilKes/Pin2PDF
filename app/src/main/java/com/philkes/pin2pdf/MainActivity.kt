package com.philkes.pin2pdf

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.philkes.pin2pdf.fragment.boards.BoardFragment
import com.philkes.pin2pdf.storage.database.DBService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dbService: DBService

    @Inject
    lateinit var settings: Settings

    private lateinit var boardFragment: BoardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        boardFragment =
            supportFragmentManager.findFragmentById(R.id.boardFragment) as BoardFragment
    }

    /**
     * Show menu item for editing the username + clearing all data
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options, menu)
        with(menu.findItem(R.id.option_username_edit)) {
            setOnMenuItemClickListener {
                settings.showUsernameInputDialog(this@MainActivity) { username: String? ->
                    lifecycleScope.launch {
                        settings.resetPins{
                            // Reload Fragment to load new user
                            boardFragment.loadUser(username)
                        }

                    }
                }
                true
            }
        }

        with(menu.findItem(R.id.option_refresh_all_boards)) {
            setOnMenuItemClickListener {
                lifecycleScope.launch {
                    settings.resetPins{
                        // Refetch all boards and pins
                        boardFragment.loadUser(settings.username)
                    }
                }
                true
            }
        }

        return true
    }
}