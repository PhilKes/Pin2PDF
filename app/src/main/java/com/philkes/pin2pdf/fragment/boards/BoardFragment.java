package com.philkes.pin2pdf.fragment.boards;

import static com.philkes.pin2pdf.fragment.boards.BoardObjectFragment.ARG_BOARD;
import static com.philkes.pin2pdf.fragment.boards.BoardObjectFragment.ARG_BOARD_ID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.Util;
import com.philkes.pin2pdf.api.pinterest.PinterestAPI;
import com.philkes.pin2pdf.api.pinterest.model.BoardResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment containing {@link ViewPager} with Tabs for each Boards
 */
public class BoardFragment extends Fragment {
    private static final String TAG= "BoardFragment";
    BoardPagerAdapter boardCollectionAdapter;
    TabLayout tabLayout;
    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.board_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewPager=view.findViewById(R.id.boardPager);
        tabLayout=view.findViewById(R.id.tab_layout);

        SharedPreferences sharedPref=getActivity().getSharedPreferences(
                getString(R.string.app_name), Context.MODE_PRIVATE);
        String username=sharedPref.getString(getResources().getString(R.string.key_user_name), null);
        if(username==null) {
            Util.showUsernameInputDialog(getActivity(),(newUsername)-> loadUser(newUsername));
        }else {
            loadUser(username);
        }
    }

    public void loadUser(String username) {
        ProgressDialog progress=new ProgressDialog(getContext());
        progress.setTitle(getString(R.string.progress_title));
        progress.setMessage(getString(R.string.progress_wait));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        PinterestAPI api=PinterestAPI.getInstance(getContext());
        api.requestBoardsOfUser(username, (boards) -> {
            getActivity().runOnUiThread(() -> {
                boardCollectionAdapter=new BoardPagerAdapter(getChildFragmentManager(),
                        boards);
                viewPager.setAdapter(boardCollectionAdapter);
                viewPager.setOffscreenPageLimit(boards.size());
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                progress.dismiss();
            });
        }, (error)->{
            Log.e(TAG, String.format("nErrorResponse: Failed: %s", error));
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.alert_error_title);
            builder.setMessage(String.format(getString(R.string.alert_error_msg_template),username));
            builder.setPositiveButton(R.string.alert_error_btn_positive, (dialogInterface, i) -> Util.showUsernameInputDialog(getContext(), this::loadUser));
            builder.setCancelable(true);
            builder.show();
        });
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_bar, menu);
        MenuItem searchViewItem=menu.findItem(R.id.app_bar_search);
        final SearchView searchView=(SearchView) MenuItemCompat.getActionView(searchViewItem);
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
                boardCollectionAdapter.setFilter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter extends FragmentPagerAdapter {
    List<BoardResponse> boards;
    List<BoardObjectFragment> fragments;

    public BoardPagerAdapter(FragmentManager fm, List<BoardResponse> boards) {
        super(fm);
        this.boards=boards;
        this.fragments=new ArrayList<>();
        for(int i=0; i<boards.size(); i++) {
            this.fragments.add(null);
        }
    }

    public void setFilter(String filter) {
        for(BoardObjectFragment fragment : fragments) {
            fragment.setFilter(filter);
        }
    }

    @Override
    public Fragment getItem(int i) {
        BoardObjectFragment fragment=new BoardObjectFragment();
        Bundle args=new Bundle();
        args.putString(ARG_BOARD, boards.get(i).getName());
        args.putString(ARG_BOARD_ID, boards.get(i).getId());
        fragment.setArguments(args);
        fragments.set(i, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return boards.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return boards.get(position).getName();
    }
}

