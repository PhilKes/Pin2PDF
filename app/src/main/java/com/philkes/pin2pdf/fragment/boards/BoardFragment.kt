package com.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.api.pinterest.PinterestAPI

/**
 * Fragment containing ViewPager with Tabs for each Boards */
class BoardFragment : Fragment() {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    lateinit var boardCollectionAdapter: BoardPagerAdapter
    lateinit var viewPager: ViewPager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.board_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val progress = ProgressDialog(context)
        progress.setTitle("Loading your Pinterest Boards")
        progress.setMessage("Please wait...")
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
        progress.show()
        val api = PinterestAPI.getInstance(context!!)!!
        api.requestBoardsOfUser(USER,{ boards: List<String> ->
            activity!!.runOnUiThread {
                boardCollectionAdapter = BoardPagerAdapter(
                    childFragmentManager,
                    boards
                )
                viewPager = view.findViewById(R.id.boardPager)
                viewPager.setAdapter(boardCollectionAdapter)
                viewPager.setOffscreenPageLimit(boards.size)
                val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
                tabLayout.setupWithViewPager(viewPager)
                tabLayout.tabGravity = TabLayout.GRAVITY_CENTER
                tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                progress.dismiss()
            }
        })
    }

    companion object {
        const val USER = "cryster0416"
    }
} // Since this is an object collection, use a FragmentStatePagerAdapter,

// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter(fm: FragmentManager?, var boards: List<String?>) :
    FragmentPagerAdapter(fm) {
    override fun getItem(i: Int): Fragment {
        val fragment: Fragment = BoardObjectFragment()
        val args = Bundle()
        // Our object is just an integer :-P
        args.putString(BoardObjectFragment.ARG_BOARD, boards[i])
        fragment.arguments = args
        return fragment
    }

    override fun getCount(): Int {
        return boards.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return boards[position]
    }
}