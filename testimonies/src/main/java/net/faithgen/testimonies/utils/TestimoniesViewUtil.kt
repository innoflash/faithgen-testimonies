package net.faithgen.testimonies.utils

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Pagination
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.activities.TestimonyActivity
import net.faithgen.testimonies.adapters.LI3Adapter
import net.faithgen.testimonies.models.TestimoniesResponse
import net.faithgen.testimonies.models.Testimony
import net.innoflash.iosview.recyclerview.RecyclerTouchListener
import net.innoflash.iosview.recyclerview.RecyclerViewClickListener
import net.innoflash.iosview.swipelib.SwipeRefreshLayout

/**
 * This util is used to :
 * Fetch testimonies
 * Render testimonies
 */
final class TestimoniesViewUtil(val context: Context, private val view: View) : RecyclerViewClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var filterText: String? = null
    private var params: HashMap<String, String>? = null
    private var pagination: Pagination? = null
    var testimonies: MutableList<Testimony> = mutableListOf()
    private var adapter: LI3Adapter? = null

    val faithGenAPI: FaithGenAPI by lazy {
        FaithGenAPI(context)
    }

    private val noTestimonies: TextView by lazy {
        view.findViewById<TextView>(R.id.noTestimonies)
    }

    private val swipeRefresh: SwipeRefreshLayout by lazy {
        view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
    }

    val testimoniesView: RecyclerView by lazy {
        view.findViewById<RecyclerView>(R.id.testimoniesView)
    }

    /**
     * Gives the recyclerview and swipe refresh their callbacks
     */
    fun initViewsCallbacks() {
        testimoniesView.layoutManager = LinearLayoutManager(context)
        testimoniesView.addOnItemTouchListener(
            RecyclerTouchListener(
                context,
                testimoniesView,
                this
            )
        )

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setRefreshDrawableStyle(SwipeRefreshLayout.ARROW)
        swipeRefresh.setPullPosition(Gravity.BOTTOM)
    }

    /**
     * This fetches the testimonies from the server
     *
     * @param url The url to fetch the testimonies
     * @param filterText The text to use on search
     * @param reload This decides whether or not you should reload recyclerview or append to
     * current data
     */
    fun loadTestimonies(url: String, filterText: String, reload: Boolean) {
        this.filterText = filterText
        if (filterText.length !== 0) {
            params = hashMapOf()
            params?.put(Constants.FILTER_TEXT, filterText)
        } else params = null

        faithGenAPI.setParams(params)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val testimoniesResponse = GSONSingleton.instance.gson.fromJson(
                        serverResponse,
                        TestimoniesResponse::class.java
                    )
                    pagination =
                        GSONSingleton.instance.gson.fromJson(
                            serverResponse,
                            Pagination::class.java
                        )

                    if (reload || testimonies!!.size === 0) {
                        testimonies.clear()
                        testimonies.addAll(testimoniesResponse.testimonies)
                        adapter = LI3Adapter(
                            context,
                            testimonies
                        )
                        testimoniesView.adapter = adapter
                    } else {
                        testimonies.addAll(testimoniesResponse.testimonies)
                        adapter!!.notifyDataSetChanged()
                    }

                    if (testimonies.size == 0) noTestimonies.visibility = View.VISIBLE
                    else noTestimonies.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    //super.onError(errorResponse)
                    //  Dialogs.showOkDialog(context, errorResponse?.message, pagination === null)
                    noTestimonies.text = errorResponse?.message
                }
            })
            .request(url)
    }

    /**
     * Handles testimony clicks
     * Opens the testimony to full details
     */
    override fun onClick(view: View?, position: Int) {
        val intent = Intent(context, TestimonyActivity::class.java)
        intent.putExtra(Constants.TESTIMONY_ID, testimonies.get(position).id)
        context.startActivity(intent)
    }

    override fun onLongClick(view: View?, position: Int) {
        //do nothing
    }

    /**
     * The swipe refresh callback
     *
     * Loads more testimonies when paginated
     */
    override fun onRefresh() {
        if (pagination === null || pagination?.links?.next === null)
            swipeRefresh.isRefreshing = false
        else loadTestimonies(pagination?.links?.next!!, filterText.orEmpty(), false)
    }
}