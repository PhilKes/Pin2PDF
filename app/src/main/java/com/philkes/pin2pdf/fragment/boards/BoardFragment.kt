package com.philkes.pin2pdf.fragment.boards

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.google.android.material.tabs.TabLayout
import com.philkes.pin2pdf.R
import com.philkes.pin2pdf.Util.showUsernameInputDialog
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

    lateinit var boardCollectionAdapter: BoardPagerAdapter
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
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        val username = sharedPref.getString(resources.getString(R.string.key_user_name), null)
        if (username == null) {
            showUsernameInputDialog(requireActivity()) { newUsername: String? ->
                loadUser(
                    newUsername
                )
            }
        } else {
            loadUser(username)
        }
    }

    fun loadUser(username: String?) {
        val progress = ProgressDialog(context).apply {
            setTitle(getString(R.string.progress_title))
            setMessage(getString(R.string.progress_wait))
            setCancelable(false) // disable dismiss by tapping outside of the dialog
            show()
        }
        pinterestAPI
            .requestBoardsOfUser(username, { boards: List<BoardResponse?> ->
                requireActivity().runOnUiThread {
                    boardCollectionAdapter = BoardPagerAdapter(
                        childFragmentManager,
                        boards
                    )
                    viewPager.apply {
                        adapter = boardCollectionAdapter
                        offscreenPageLimit = boards.size
                    }
                    tabLayout.apply {
                        setupWithViewPager(viewPager)
                        tabGravity = TabLayout.GRAVITY_CENTER
                        tabMode = TabLayout.MODE_SCROLLABLE
                    }
                    progress.dismiss()
                }
            }) { error: VolleyError? ->
                Log.e(TAG, String.format("nErrorResponse: Failed: %s", error))
                with(AlertDialog.Builder(context)) {
                    setTitle(R.string.alert_error_title)
                    setMessage(
                        String.format(
                            getString(R.string.alert_error_msg_template),
                            username
                        )
                    )
                    setPositiveButton(R.string.alert_error_btn_positive) { _: DialogInterface?, _: Int ->
                        showUsernameInputDialog(
                            requireContext()
                        ) { username: String? -> loadUser(username) }
                    }
                    setCancelable(true)
                    show()
                }

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
                boardCollectionAdapter.setFilter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        private const val TAG = "BoardFragment"
    }
} // Since this is an object collection, use a FragmentStatePagerAdapter,

// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter(fm: FragmentManager?, var boards: List<BoardResponse?>) :
    FragmentPagerAdapter(fm!!) {
    var fragments: MutableList<BoardObjectFragment?> = ArrayList()
    fun setFilter(filter: String) {
        for (fragment in fragments) {
            fragment!!.setFilter(filter)
        }
    }

    override fun getItem(i: Int): Fragment {
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

    init {
        for (i in boards.indices) {
            fragments.add(null)
        }
    }
}