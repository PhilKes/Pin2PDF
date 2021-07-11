package com.philkes.pin2pdf.fragment.boards;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.adapter.PinAdapter;
import com.philkes.pin2pdf.model.PinModel;
import com.philkes.pin2pdf.api.pinterest.PinterestAPI;
import com.philkes.pin2pdf.storage.local.service.DBService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.philkes.pin2pdf.fragment.boards.BoardFragment.USER;

/**
 * Fragments inside of BoardFragment
 * A BoardObjectFragment represents 1 Pinterest Board with its Pins (1 Tab)
 */
public class BoardObjectFragment extends Fragment {
    public static final String ARG_BOARD="board";

    private RecyclerView pinListView;
    private RecyclerView.Adapter pinListViewAdapter;
    private List<PinModel> pinsList;
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
/*        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(pinListView.getContext(),
                listViewManager.getOrientation());
        pinListView.addItemDecoration(dividerItemDecoration);*/
    }

    private void loadPins() {
        ProgressDialog progress=new ProgressDialog(getContext());
        progress.setTitle("Loading Pins of '" + boardName + "'");
        progress.setMessage("Please wait...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        PinterestAPI api=PinterestAPI.getInstance(getContext());
        api.requestPinsOfBoard(USER, boardName, false, false, (pins) -> {
            System.out.println("All Pins: " + pins.size());
            DBService dbService=DBService.getInstance(getContext());
            // Try to load all Pins from local DB
            dbService.loadPins(
                    pins.stream().map(PinModel::getPinId).collect(Collectors.toList()),
                    (loadedPins) -> {
                        System.out.println("Loaded Pins: " + loadedPins.size());
                        // Check if any Pins weren't loaded from local DB
                        List<PinModel> missingPins=new ArrayList<>(pins);
                        missingPins.removeIf(pinModel -> loadedPins.stream().anyMatch(pin -> pin.getPinId().equals(pinModel.getPinId())));
                        System.out.println("Missing Pins: " + missingPins.size());
                        // Fetch missing Pins from Pinterest API/Scraper
                        if(!missingPins.isEmpty()) {
                            api.scrapePDFLinks(missingPins);
                            api.requestPinsInfos(missingPins, (fetchedPins) -> {
                                fetchedPins.forEach(pin-> pin.setBoard(boardName));
                                dbService.insertPins(fetchedPins, () -> {
                                    // Reload all Pins from Local DB
                                    dbService.loadPins(pins.stream().map(PinModel::getPinId).collect(Collectors.toList()),
                                            (allPins) -> {
                                                updatePinsList(allPins);
                                                progress.dismiss();
                                            });
                                });
                            }, null);
                        }
                        else {
                            updatePinsList(loadedPins);
                            progress.dismiss();
                        }
                    });

        });

    }

    private void updatePinsList(List<PinModel> pins) {
        pinsList.clear();
        pinsList.addAll(pins);
        getActivity().runOnUiThread(() -> {
            pinListViewAdapter.notifyDataSetChanged();
        });
    }
}
