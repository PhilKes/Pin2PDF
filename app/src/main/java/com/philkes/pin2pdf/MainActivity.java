package com.philkes.pin2pdf;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.philkes.pin2pdf.api.pinterest.PinterestAPI;
import com.philkes.pin2pdf.fragment.boards.BoardFragment;
import com.philkes.pin2pdf.storage.local.service.DBService;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBService.getInstance(this);
        PinterestAPI.getInstance(this);
    }

    /**
     * Show menu item for editing the username + clearing all data
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        MenuItem usernameEditItem=menu.findItem(R.id.option_username_edit);
        usernameEditItem.setOnMenuItemClickListener((item) -> {
            Util.showUsernameInputDialog(this, (username) -> {
                // Reload Fragment to load new user
                ((BoardFragment) getSupportFragmentManager().findFragmentById(R.id.boardFragment)).loadUser(username);
            });
            return true;
        });
        MenuItem clearPinItem=menu.findItem(R.id.option_clear_pin_data);
        clearPinItem.setOnMenuItemClickListener((item) -> {
            DBService.getInstance(this).clearAll();
            Util.showUsernameInputDialog(this, (username) -> {
                // Reload Fragment to load new user
                ((BoardFragment) getSupportFragmentManager().findFragmentById(R.id.boardFragment)).loadUser(username);
            });
            return true;
        });
        return true;
    }
}
