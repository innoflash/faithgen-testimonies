package net.faithgen.testimonies

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.innoflash.iosview.swipelib.SwipeRefreshLayout

class ViewUtil(val context: Context, private val view: View) {

    private val noTestimonies: TextView by lazy {
        view.findViewById<TextView>(R.id.noTestimonies)
    }
    private val swipeRefresh: SwipeRefreshLayout by lazy {
        view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
    }

    private val testimoniesView: RecyclerView by lazy {
        view.findViewById<RecyclerView>(R.id.testimoniesView)
    }
}