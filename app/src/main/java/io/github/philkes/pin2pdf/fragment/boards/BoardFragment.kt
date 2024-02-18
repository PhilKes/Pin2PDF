package io.github.philkes.pin2pdf.fragment.boards

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.philkes.pin2pdf.R
import io.github.philkes.pin2pdf.Settings
import io.github.philkes.pin2pdf.api.pinterest.PinterestAPI
import io.github.philkes.pin2pdf.api.pinterest.model.BoardResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * Fragment containing [ViewPager2] with Tabs for each Boards
 */
@AndroidEntryPoint
class BoardFragment : Fragment() {
    @Inject
    lateinit var pinterestAPI: PinterestAPI

    @Inject
    lateinit var settings: Settings

    var boardCollectionAdapter: BoardPagerAdapter? = null
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2

    var isSearchActive: Boolean = false

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
        if (settings.username == null || settings.userBoards == null) {
            settings.showUserAndBoardInput(requireActivity()) { boards -> loadBoards(boards) }
        } else {
            loadBoards(settings.userBoards!!)
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                with(boardCollectionAdapter) {
                    if (this != null && position < boardFragments.size && boardFragments[position] != null) {
                        boardFragments[position]!!.checkPins()
                    }
                }

            }
        })
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
            boardCollectionAdapter = BoardPagerAdapter(
                requireContext(),
                this,
                allBoards
            )
            viewPager.adapter = boardCollectionAdapter
            tabLayout.apply {
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text =
                        if (position == allBoards.size) "Search" else allBoards[position].name
                }.attach()

                tabGravity = TabLayout.GRAVITY_CENTER
                tabMode = TabLayout.MODE_SCROLLABLE
            }
            (tabLayout.getTabAt(allBoards.size)!!.view as LinearLayout).visibility = View.GONE

            viewPager.offscreenPageLimit = allBoards.size
            if (allBoards.size > 1) {
                viewPager.currentItem = 1
                boardCollectionAdapter!!.initialItem = viewPager.currentItem
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
                val boards = boardCollectionAdapter!!.boardFragments.size
                val newIsSearchActive = newText.isNotEmpty()
                if (!isSearchActive && newIsSearchActive) {
                    toggleSearchTabVisibility(true, boards)
                } else if (isSearchActive && !newIsSearchActive) {
                    toggleSearchTabVisibility(false, boards)
                }
                boardCollectionAdapter!!.setSearchQuery(newText)
                isSearchActive = newIsSearchActive;
                return false
            }

            private fun toggleSearchTabVisibility(searchActive: Boolean, boards: Int) {
                val searchTab = tabLayout.getTabAt(boards)!!
                (searchTab.view as LinearLayout).visibility =
                    if (searchActive) View.VISIBLE else View.GONE
                searchTab.select()
                for (i in 0 until boards) {
                    (tabLayout.getTabAt(i)!!.view as LinearLayout).visibility =
                        if (searchActive) View.GONE else View.VISIBLE
                }
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        private const val TAG = "BoardFragment"
        val PIN2PDF_FAVORITES_BOARD_ID = "pin2pdf-favorites"
    }
}

class BoardPagerAdapter(
    val context: Context,
    fragment: Fragment,
    var boards: MutableList<BoardResponse>
) : FragmentStateAdapter(fragment) {
    public var boardFragments: MutableList<BoardObjectFragment?> = ArrayList()
    public var searchFragment: SearchObjectFragment? = null
    var initialItem: Int = -1

    fun setSearchQuery(query: String) {
        if (searchFragment != null) {
            searchFragment!!.updateQuery(query)
        }
    }

    init {
        for (i in boards.indices) {
            boardFragments.add(null)
        }
    }

    override fun getItemCount(): Int {
        return boards.size + 1
    }


    override fun createFragment(position: Int): Fragment {
        if (position == itemCount - 1) {
            searchFragment = SearchObjectFragment()
            return searchFragment!!
        }
        val fragment = BoardObjectFragment()
        val args = Bundle().apply {
            boards[position].let {
                putString(BoardObjectFragment.ARG_BOARD, it.name)
                putString(BoardObjectFragment.ARG_BOARD_ID, it.id)
            }
        }
        fragment.arguments = args
        boardFragments[position] = fragment
        return fragment
    }


}