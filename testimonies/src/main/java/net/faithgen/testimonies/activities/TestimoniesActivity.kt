package net.faithgen.testimonies.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import br.com.liveo.searchliveo.SearchLiveo

import kotlinx.android.synthetic.main.activity_testimonies.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.ViewUtil

class TestimoniesActivity : FaithGenActivity() {
    private var filter_text: String? = ""
    private var viewUtil: ViewUtil? = null

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

        setOnOptionsClicked(R.drawable.ic_search_blue) {
            searchLiveo.visibility = View.VISIBLE
            toolbar.visibility = View.GONE
            searchLiveo.show()
        }
    }

    override fun onStop() {
        super.onStop()
        viewUtil?.faithGenAPI?.cancelRequests()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null)
            if (requestCode == SearchLiveo.REQUEST_CODE_SPEECH_INPUT)
                searchLiveo.resultVoice(requestCode, resultCode, data);
    }

    override fun onStart() {
        super.onStart()
        if(viewUtil?.testimonies?.size === 0)
            viewUtil?.loadTestimonies(Constants.TESTIMONIES_URL, filter_text!!, true)
    }
}
