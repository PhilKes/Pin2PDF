package com.philkes.pin2pdf

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.storage.local.service.DBService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DBService.getInstance(this)
        PinterestAPI.getInstance(this)
        //setupUI();
        // loadPins();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_bar, menu)
        val searchViewItem = menu.findItem(R.id.app_bar_search)
        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                /*   if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                }*/return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //TODO pinListViewAdapter.getFilter().filter(newText);
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}