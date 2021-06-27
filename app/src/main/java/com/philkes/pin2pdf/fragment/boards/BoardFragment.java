package com.philkes.pin2pdf.fragment.boards;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philkes.pin2pdf.R;
import com.philkes.pin2pdf.api.pinterest.PinterestAPI;

import java.util.List;

import static com.philkes.pin2pdf.fragment.boards.BoardObjectFragment.ARG_BOARD;

/**
 * Fragment containing ViewPager with Tabs for each Boards*/
public class BoardFragment extends Fragment {
    public static final String USER="cryster0416";
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    BoardPagerAdapter demoCollectionPagerAdapter;
    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.board_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setTitle("Loading your Pinterest Boards");
        progress.setMessage("Please wait...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        PinterestAPI.requestBoardsOfUser(getContext(), USER, (boards) -> {
            getActivity().runOnUiThread(() -> {
                demoCollectionPagerAdapter=new BoardPagerAdapter(getChildFragmentManager(),
                        boards);
                viewPager=view.findViewById(R.id.boardPager);
                viewPager.setAdapter(demoCollectionPagerAdapter);
                TabLayout tabLayout = view.findViewById(R.id.tab_layout);
                tabLayout.setupWithViewPager(viewPager);
                progress.dismiss();
            });

        });
    }


}

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter extends FragmentStatePagerAdapter {
    List<String> boards;

    public BoardPagerAdapter(FragmentManager fm, List<String> boards) {
        super(fm);
        this.boards=boards;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment=new BoardObjectFragment();
        Bundle args=new Bundle();
        // Our object is just an integer :-P
        args.putString(ARG_BOARD, boards.get(i));
        fragment.setArguments(args);
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

