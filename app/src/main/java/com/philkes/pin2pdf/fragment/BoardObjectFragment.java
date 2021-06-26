package com.philkes.pin2pdf.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.adapter.PinAdapter;
import com.philkes.pin2pdf.model.Pin;
import com.philkes.pin2pdf.network.PinterestAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.philkes.pin2pdf.fragment.BoardFragment.USER;

/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)*/
public class BoardObjectFragment extends Fragment {
    public static final String ARG_BOARD="board";

    private RecyclerView pinListView;
    private RecyclerView.Adapter pinListViewAdapter;
    private List<Pin> pinsList;
    private String boardName;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.board_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args=getArguments();
        boardName=args.getString(ARG_BOARD);

        setupUI(view);
        loadPins();
    }

    private void setupUI(View view) {
        pinsList=new ArrayList<>();
        pinListView=view.findViewById(R.id.pins_list);
        pinListViewAdapter=new PinAdapter(pinsList);
        pinListView.setAdapter(pinListViewAdapter);
        pinListView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager listViewManager=new LinearLayoutManager(view.getContext());
        pinListView.setLayoutManager(listViewManager);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(pinListView.getContext(),
                listViewManager.getOrientation());
        pinListView.addItemDecoration(dividerItemDecoration);
    }

    private void loadPins() {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setTitle("Loading Pins of '"+boardName+"'");
        progress.setMessage("Please wait...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        PinterestAPI.requestPinsOfBoard(getContext(),USER,boardName,(pins) -> {
            pinsList.clear();
            pinsList.addAll(pins);
            getActivity().runOnUiThread(() -> {
                pinListViewAdapter.notifyDataSetChanged();
            });
            progress.dismiss();
        });

    }
}
