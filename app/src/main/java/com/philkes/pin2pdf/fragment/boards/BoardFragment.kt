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

/**
 * Fragment containing [ViewPager] with Tabs for each Boards
 */
class BoardFragment : Fragment() {
    var boardCollectionAdapter: BoardPagerAdapter? = null
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
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
        val sharedPref = activity!!.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        val username = sharedPref.getString(resources.getString(R.string.key_user_name), null)
        if (username == null) {
            showUsernameInputDialog(activity!!) { newUsername: String? -> loadUser(newUsername) }
        } else {
            loadUser(username)
        }
    }

    fun loadUser(username: String?) {
        val progress = ProgressDialog(context)
        progress.setTitle(getString(R.string.progress_title))
        progress.setMessage(getString(R.string.progress_wait))
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
        progress.show()
        val api: PinterestAPI = PinterestAPI.Companion.getInstance(context)
        api.requestBoardsOfUser(username, { boards: List<BoardResponse?> ->
            activity!!.runOnUiThread {
                boardCollectionAdapter = BoardPagerAdapter(
                    childFragmentManager,
                    boards
                )
                viewPager!!.adapter = boardCollectionAdapter
                viewPager!!.offscreenPageLimit = boards.size
                tabLayout!!.setupWithViewPager(viewPager)
                tabLayout!!.tabGravity = TabLayout.GRAVITY_CENTER
                tabLayout!!.tabMode = TabLayout.MODE_SCROLLABLE
                progress.dismiss()
            }
        }) { error: VolleyError? ->
            Log.e(TAG, String.format("nErrorResponse: Failed: %s", error))
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.alert_error_title)
            builder.setMessage(
                String.format(
                    getString(R.string.alert_error_msg_template),
                    username
                )
            )
            builder.setPositiveButton(R.string.alert_error_btn_positive) { dialogInterface: DialogInterface?, i: Int ->
                showUsernameInputDialog(
                    context!!
                ) { username: String? -> loadUser(username) }
            }
            builder.setCancelable(true)
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_bar, menu)
        val searchViewItem = menu.findItem(R.id.app_bar_search)
        val searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                /*   if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                }*/return false
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
    }
} // Since this is an object collection, use a FragmentStatePagerAdapter,

// and NOT a FragmentPagerAdapter.
class BoardPagerAdapter(fm: FragmentManager?, var boards: List<BoardResponse?>) :
    FragmentPagerAdapter(fm) {
    var fragments: MutableList<BoardObjectFragment?>
    fun setFilter(filter: String) {
        for (fragment in fragments) {
            fragment!!.setFilter(filter)
        }
    }

    override fun getItem(i: Int): Fragment {
        val fragment = BoardObjectFragment()
        val args = Bundle()
        args.putString(BoardObjectFragment.Companion.ARG_BOARD, boards[i]!!.name)
        args.putString(BoardObjectFragment.Companion.ARG_BOARD_ID, boards[i]!!.id)
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
        fragments = ArrayList()
        for (i in boards.indices) {
            fragments.add(null)
        }
    }
}