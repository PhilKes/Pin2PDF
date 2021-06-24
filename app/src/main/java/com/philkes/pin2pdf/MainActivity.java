package com.philkes.pin2pdf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
            pinsList.addAll(new Tasks.GetPinsTask().execute("Test").get());
            pinListViewAdapter.notifyDataSetChanged();
        }
        catch(ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
