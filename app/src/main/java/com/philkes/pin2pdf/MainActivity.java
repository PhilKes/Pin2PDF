package com.philkes.pin2pdf;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.philkes.pin2pdf.adapter.PinAdapter;
import com.philkes.pin2pdf.mock.MockData;
import com.philkes.pin2pdf.model.Pin;
import com.philkes.pin2pdf.network.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private RecyclerView pinListView;
    private RecyclerView.Adapter pinListViewAdapter;
    private List<Pin> pinsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        loadPins();
    }

    private void setupUI() {
        pinsList=new ArrayList<>();
        pinListView= findViewById(R.id.pins_list);
        pinListViewAdapter= new PinAdapter(pinsList);
        pinListView.setAdapter(pinListViewAdapter);
        pinListView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager listViewManager=new LinearLayoutManager(this);
        pinListView.setLayoutManager(listViewManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pinListView.getContext(),
                listViewManager.getOrientation());
        pinListView.addItemDecoration(dividerItemDecoration);
    }

    private void loadPins() {
        pinsList.clear();
        try {
            pinsList.addAll(new Tasks.GetPinsTask().execute("https://www.pinterest.de/cryster0416/beilagen/").get());
            pinListViewAdapter.notifyDataSetChanged();
        }
        catch(ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_bar, menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
             /*   if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                }*/
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TODO pinListViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
