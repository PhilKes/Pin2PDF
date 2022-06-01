package com.philkes.pin2pdf.fragment.boards;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.adapter.PinAdapter;
import com.philkes.pin2pdf.api.pinterest.PinterestAPI;
import com.philkes.pin2pdf.model.PinModel;
import com.philkes.pin2pdf.storage.local.service.DBService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)
 */
public class BoardObjectFragment extends Fragment {
    private static final String TAG="BoardObjectFragment";

    public static final String ARG_BOARD="board";
    public static final String ARG_BOARD_ID="boardId";

    private SwipeRefreshLayout refreshLayout;
    private TextView amountPinsTextView;
    private RecyclerView pinListView;
    private RecyclerView.Adapter pinListViewAdapter;
    private List<PinModel> currentPins=new ArrayList<>();
    private List<PinModel> allPins=new ArrayList<>();
    private String boardName;
    private String boardId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.board_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args=getArguments();
        boardName=args.getString(ARG_BOARD);
        boardId=args.getString(ARG_BOARD_ID);

        setupUI(view);
        loadPins();
    }


    private void setupUI(View view) {
        currentPins=new ArrayList<>();
        amountPinsTextView=view.findViewById(R.id.amount_pins_text);
        pinListView=view.findViewById(R.id.pins_list);
        pinListViewAdapter=new PinAdapter(currentPins);
        pinListView.setAdapter(pinListViewAdapter);
        pinListView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager listViewManager=new LinearLayoutManager(view.getContext());
        pinListView.setLayoutManager(listViewManager);
        refreshLayout=view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            this.loadPins();
            refreshLayout.setRefreshing(false);
        });
    }

    private void loadPins() {
        ProgressDialog progress=new ProgressDialog(getContext());
        progress.setTitle(String.format(getString(R.string.progress_pins_title), boardName));
        progress.setMessage(getString(R.string.progress_pins_wait));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        PinterestAPI api=PinterestAPI.getInstance(getContext());
        api.requestPinsOfBoard(boardId, false, (pins) -> {
            Log.d(TAG, String.format("All Pins: %d", pins.size()));
            DBService dbService=DBService.getInstance(getContext());
            // Try to load all Pins from local DB
            dbService.loadPins(
                    pins.stream().map(PinModel::getPinId).collect(Collectors.toList()),
                    (loadedPins) -> {
                        Log.d(TAG, String.format("Loaded Pins: %d", loadedPins.size()));
                        // Check if any Pins weren't loaded from local DB
                        List<PinModel> missingPins=new ArrayList<>(pins);
                        missingPins.removeIf(pinModel -> loadedPins.stream().anyMatch(pin -> pin.getPinId().equals(pinModel.getPinId())));
                        Log.d(TAG, String.format("Missing Pins: %d", missingPins.size()));
                        // Fetch missing Pins from Pinterest API/Scraper
                        if(!missingPins.isEmpty()) {
                            api.scrapePDFLinks(missingPins);
                            dbService.insertPins(missingPins, () -> {
                                // Reload all Pins from Local DB
                                dbService.loadPins(pins.stream().map(PinModel::getPinId).collect(Collectors.toList()),
                                        (allPins) -> {
                                            updatePinsList(allPins);
                                            // Store all available Pins separately for filtering
                                            this.allPins=allPins;
                                            progress.dismiss();
                                        });
                            });
                        }
                        else {
                            updatePinsList(loadedPins);
                            this.allPins=loadedPins;
                            progress.dismiss();
                        }
                    });
        });

    }

    private void updatePinsList(List<PinModel> pins) {
        currentPins.clear();
        currentPins.addAll(pins);
        getActivity().runOnUiThread(() -> {
            this.amountPinsTextView.setText(String.format(getString(R.string.amount_pins_text_template), allPins.size()));
            pinListViewAdapter.notifyDataSetChanged();
        });
    }

    public void setFilter(String filter) {
        List<PinModel> filteredPins=new ArrayList<>();
        // Find all Pins that match Filter from allPins
        for(PinModel pin : allPins) {
            if(pin.getTitle().toLowerCase().contains(filter.toLowerCase())) {
                filteredPins.add(pin);
            }
        }
        updatePinsList(filteredPins);
    }
}
