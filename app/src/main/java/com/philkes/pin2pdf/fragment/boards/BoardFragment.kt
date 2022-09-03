package com.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.Settings
import com.philkes.pin2pdf.api.pinterest.PinterestAPI
import com.philkes.pin2pdf.api.pinterest.model.BoardResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * Fragment containing [ViewPager] with Tabs for each Boards
 */
@AndroidEntryPoint
class BoardFragment : Fragment() {
    @Inject
    lateinit var pinterestAPI: PinterestAPI

    @Inject
    lateinit var settings: Settings

    var boardCollectionAdapter: BoardPagerAdapter? = null
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.board_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.boardPager)
        tabLayout = view.findViewById(R.id.tab_layout)

        boardCollectionAdapter = BoardPagerAdapter(
            requireContext(),
            childFragmentManager,
            mutableListOf()
        )
        viewPager.adapter = boardCollectionAdapter

        tabLayout.apply {
            setupWithViewPager(viewPager)
            tabGravity = TabLayout.GRAVITY_CENTER
            tabMode = TabLayout.MODE_SCROLLABLE
        }
        if (settings.username == null || settings.userBoards == null) {
            settings.showUserAndBoardInput(requireActivity()) { boards -> loadBoards(boards) }
        } else {
            loadBoards(settings.userBoards!!)
        }
    }

    fun reset(){
        if (boardCollectionAdapter != null) {
            with(boardCollectionAdapter!!){
                fragments.forEach {
                    it?.reset()
                }
                for (i in fragments.indices){
                    val item: Any = getItem(i)
                    destroyItem(viewPager,i, item)
                }
                boards.clear()
                fragments.clear()
                notifyDataSetChanged()
            }
        }

    }

    fun loadBoards(newBoards: List<BoardResponse>) {

        val progress = ProgressDialog(context).apply {
            setTitle(getString(R.string.progress_title))
            setMessage(getString(R.string.progress_wait))
            setCancelable(false) // disable dismiss by tapping outside of the dialog
            show()
        }
        Log.d(TAG, "Loading boards: ${newBoards.map { it.name }}")
        requireActivity().runOnUiThread {
            val allBoards = newBoards.toMutableList()
            allBoards.add(
                0,
                BoardResponse("Favorites", "", PIN2PDF_FAVORITES_BOARD_ID)
            )
            reset()
            boardCollectionAdapter!!.boards.addAll(allBoards)
            boardCollectionAdapter!!.init()
            boardCollectionAdapter!!.notifyDataSetChanged()
            viewPager.offscreenPageLimit = allBoards.size
            if (allBoards.size > 1) {
                viewPager.currentItem = 1
            }

            progress.dismiss()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar, menu)
        val searchViewItem = menu.findItem(R.id.app_bar_search)
        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                boardCollectionAdapter!!.setFilter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        private const val TAG = "BoardFragment"
        val PIN2PDF_FAVORITES_BOARD_ID = "pin2pdf-favorites"
    }
}

// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter(
    val context: Context,
    fm: FragmentManager?,
    var boards: MutableList<BoardResponse?>
) :
    FragmentPagerAdapter(fm!!) {
    var fragments: MutableList<BoardObjectFragment?> = ArrayList()
    fun setFilter(filter: String) {
        for (fragment in fragments) {
            fragment!!.setFilter(filter)
        }
    }

    override fun getItem(i: Int): BoardObjectFragment {
        val fragment = BoardObjectFragment()
        val args = Bundle().apply {
            boards[i]!!.let {
                putString(BoardObjectFragment.ARG_BOARD, it.name)
                putString(BoardObjectFragment.ARG_BOARD_ID, it.id)
            }
        }
        fragment.arguments = args
        fragments[i] = fragment
        return fragment
    }

    override fun getCount(): Int {
        return boards.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return boards[position]!!.name
    }

    fun init(){
        for (i in boards.indices) {
            fragments.add(null)
        }
    }

    init {
        init()
    }
}