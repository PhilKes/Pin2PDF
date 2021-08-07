package com.philkes.pin2pdf.fragment.boards;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.api.pinterest.PinterestAPI;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.philkes.pin2pdf.fragment.boards.BoardObjectFragment.ARG_BOARD;

/**
 * Fragment containing ViewPager with Tabs for each Boards
 */
public class BoardFragment extends Fragment {
    public static final String USER="cryster0416";
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    BoardPagerAdapter boardCollectionAdapter;
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
        ProgressDialog progress=new ProgressDialog(getContext());
        progress.setTitle("Loading your Pinterest Boards");
        progress.setMessage("Please wait...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        PinterestAPI api=PinterestAPI.getInstance(getContext());
        api.requestBoardsOfUser(USER, (boards) -> {
            getActivity().runOnUiThread(() -> {
                boardCollectionAdapter=new BoardPagerAdapter(getChildFragmentManager(),
                        boards);
                viewPager=view.findViewById(R.id.boardPager);
                viewPager.setAdapter(boardCollectionAdapter);
                viewPager.setOffscreenPageLimit(boards.size());
                TabLayout tabLayout=view.findViewById(R.id.tab_layout);
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                progress.dismiss();
            });

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
        super.onCreateOptionsMenu(menu,inflater);
    }
}

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter extends FragmentPagerAdapter {
    List<String> boards;
    List<BoardObjectFragment> fragments;

    public BoardPagerAdapter(FragmentManager fm, List<String> boards) {
        super(fm);
        this.boards=boards;
        this.fragments= new ArrayList<>();
        for(int i=0; i<boards.size(); i++) {
            this.fragments.add(null);
        }
    }

    public void setFilter(String filter){
        for(BoardObjectFragment fragment :fragments) {
            fragment.setFilter(filter);
        }
    }

    @Override
    public Fragment getItem(int i) {
        BoardObjectFragment fragment=new BoardObjectFragment();
        Bundle args=new Bundle();
        args.putString(ARG_BOARD, boards.get(i));
        fragment.setArguments(args);
        fragments.set(i,fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return boards.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return boards.get(position);
    }
}

