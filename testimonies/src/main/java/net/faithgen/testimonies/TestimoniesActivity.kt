package net.faithgen.testimonies

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import br.com.liveo.searchliveo.SearchLiveo

import kotlinx.android.synthetic.main.activity_testimonies.*
import net.faithgen.sdk.FaithGenActivity

class TestimoniesActivity : FaithGenActivity() {
    private var filter_text: String = ""
    override fun getPageTitle(): String {
        return Constants.TESTIMONIES
    }

    override fun getPageIcon(): Int = R.drawable.ic_testimonies_50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testimonies)

        searchLiveo.with(this) { charSequence ->
            filter_text = charSequence as String
            //loadTestimonies
        }.showVoice()
            .hideKeyboardAfterSearch()
            .hideSearch {
                toolbar.visibility = View.VISIBLE
                filter_text = ""
                searchLiveo.text(filter_text)
                //load testimonies with reload
            }.build()

        setOnOptionsClicked(R.drawable.ic_search_blue) {
            searchLiveo.visibility = View.VISIBLE
            toolbar.visibility = View.GONE
            searchLiveo.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null)
            if (requestCode == SearchLiveo.REQUEST_CODE_SPEECH_INPUT)
                searchLiveo.resultVoice(requestCode, resultCode, data);
    }
}
