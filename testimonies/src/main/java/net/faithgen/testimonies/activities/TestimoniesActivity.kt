package net.faithgen.testimonies.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import br.com.liveo.searchliveo.SearchLiveo
import com.bvapp.arcmenulibrary.ArcMenu
import com.bvapp.arcmenulibrary.widget.FloatingActionButton

import kotlinx.android.synthetic.main.activity_testimonies.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.ViewUtil

class TestimoniesActivity : FaithGenActivity() {
    private var filter_text: String? = ""
    private var viewUtil: ViewUtil? = null
    private val menuItems: MutableList<Pair<Int, String>> = mutableListOf()

    override fun getPageTitle(): String {
        return Constants.TESTIMONIES
    }

    override fun getPageIcon(): Int =
        R.drawable.ic_testimonies_50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testimonies)

        viewUtil =
            ViewUtil(this@TestimoniesActivity, view)
        viewUtil?.initViewsCallbacks()

        initMenu()

        searchLiveo.with(this) { charSequence ->
            filter_text = charSequence as String
            viewUtil?.loadTestimonies(Constants.TESTIMONIES_URL, filter_text.orEmpty(), true)
        }.showVoice()
            .hideKeyboardAfterSearch()
            .hideSearch {
                toolbar.visibility = View.VISIBLE
                filter_text = ""
                searchLiveo.text("")
                viewUtil?.loadTestimonies(Constants.TESTIMONIES_URL, "", true)
            }.build()

        setOnOptionsClicked(R.drawable.ic_search_blue) { openSearch() }
    }

    /**
     * Opens the search bar
     */
    private fun openSearch() {
        searchLiveo.visibility = View.VISIBLE
        toolbar.visibility = View.GONE
        searchLiveo.show()
    }

    /**
     * Checkes if user is logged in
     */
    private fun isLoggedIn(): Boolean {
        if (SDK.getUser() !== null) return true
        Dialogs.showOkDialog(this@TestimoniesActivity, Constants.NOT_LOGGED_IN, false)
        return false
    }

    override fun onStop() {
        super.onStop()
        viewUtil?.faithGenAPI?.cancelRequests()
    }

    /**
     * Receives results for speech to text for search
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null)
            if (requestCode == SearchLiveo.REQUEST_CODE_SPEECH_INPUT)
                searchLiveo.resultVoice(requestCode, resultCode, data);
    }

    override fun onStart() {
        super.onStart()
        if (viewUtil?.testimonies?.size === 0)
            viewUtil?.loadTestimonies(Constants.TESTIMONIES_URL, filter_text!!, true)
    }

    /**
     * Initialize the menu for the page
     */
    private fun initMenu() {
        menuItems.add(Pair(R.drawable.ic_new_copy_100, Constants.CREATE_TESTIMONY))
        menuItems.add(Pair(R.drawable.ic_testimonies_100, Constants.MY_TESTIMONIES))
        menuItems.add(Pair(R.drawable.ic_search_100, Constants.SEARCH))
        menuItems.add(Pair(R.drawable.ic_exit_100, Constants.EXIT.toUpperCase()))

        arcMenu.attachToRecyclerView(viewUtil!!.testimoniesView)
        arcMenu.showTooltip(true)
        arcMenu.setToolTipBackColor(Color.GRAY)
        arcMenu.setToolTipCorner(6f)
        arcMenu.setToolTipPadding(5f)
        arcMenu.setToolTipTextColor(Color.WHITE)
        arcMenu.setAnim(
            300,
            300,
            ArcMenu.ANIM_MIDDLE_TO_RIGHT,
            ArcMenu.ANIM_MIDDLE_TO_RIGHT,
            ArcMenu.ANIM_INTERPOLATOR_ACCELERATE_DECLERATE,
            ArcMenu.ANIM_INTERPOLATOR_ACCELERATE_DECLERATE
        )

        menuItems.forEachIndexed { index: Int, pair: Pair<Int, String> ->
            val menuItem: FloatingActionButton by lazy { FloatingActionButton(this@TestimoniesActivity) }
            menuItem.setSize(FloatingActionButton.SIZE_MINI)
            menuItem.setIcon(pair.first)
            menuItem.setBackgroundColor(Color.LTGRAY)

            arcMenu.setChildSize(menuItem.intrinsicHeight)
            arcMenu.addItem(menuItem, pair.second) {
                when (index) {
                    0 -> if (!isLoggedIn())
                        startActivity(
                            Intent(
                                this@TestimoniesActivity,
                                CreateTestimonyActivity::class.java
                            )
                        )
                    1 -> if (isLoggedIn()) {
                        val intent =
                            Intent(this@TestimoniesActivity, UserTestimoniesActivity::class.java)
                        intent.putExtra(Constants.USER_ID, SDK.getUser().id)
                        intent.putExtra(Constants.USER_NAME, Constants.YOUR_TESTIMONIES)
                        startActivity(intent)
                    }
                    2 -> openSearch()
                    3 -> finish()
                }
            }
        }
    }
}
