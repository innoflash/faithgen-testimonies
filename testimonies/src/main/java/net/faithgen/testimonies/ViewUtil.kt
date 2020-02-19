package net.faithgen.testimonies

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.innoflash.iosview.recyclerview.RecyclerTouchListener
import net.innoflash.iosview.recyclerview.RecyclerViewClickListener
import net.innoflash.iosview.swipelib.SwipeRefreshLayout

class ViewUtil(val context: Context, private val view: View) : RecyclerViewClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private val noTestimonies: TextView by lazy {
        view.findViewById<TextView>(R.id.noTestimonies)
    }
    private val swipeRefresh: SwipeRefreshLayout by lazy {
        view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
    }

    private val testimoniesView: RecyclerView by lazy {
        view.findViewById<RecyclerView>(R.id.testimoniesView)
    }

    fun initViewsCallbacks(){
        testimoniesView.layoutManager = LinearLayoutManager(context)
        testimoniesView.addOnItemTouchListener(RecyclerTouchListener(context, testimoniesView, this))

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setRefreshDrawableStyle(SwipeRefreshLayout.ARROW)
        swipeRefresh.setPullPosition(Gravity.BOTTOM)
    }

    override fun onClick(view: View?, position: Int) {

    }

    override fun onLongClick(view: View?, position: Int) {
        //do nothing
    }

    override fun onRefresh() {

    }
}