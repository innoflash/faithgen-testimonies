package net.faithgen.testimonies.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.sdk.utils.Utils
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.ImagesAdapter
import net.faithgen.testimonies.models.Testimony
import net.innoflash.iosview.recyclerview.RecyclerTouchListener
import net.innoflash.iosview.recyclerview.RecyclerViewClickListener

class TestimonyActivity : FaithGenActivity(), RecyclerViewClickListener {
    private var testimony: Testimony? = null

    private val faithGenAPI: FaithGenAPI by lazy {
        FaithGenAPI(this)
    }

    private val testimony_id: String by lazy {
        intent.getStringExtra(Constants.TESTIMONY_ID)
    }

    override fun getPageTitle(): String = "Loading"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testimony)
    }

    override fun onStart() {
        super.onStart()
        if (testimony === null)
            fetchTestimony()
    }

    override fun onStop() {
        super.onStop()
        faithGenAPI.cancelRequests()
    }

    private fun fetchTestimony() {
        faithGenAPI.setParams(null)
            .setFinishOnFail(true)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    testimony = GSONSingleton.instance.gson
                        .fromJson(serverResponse, Testimony::class.java)
                    renderTestimony()
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(this@TestimonyActivity, errorResponse?.message, true)
                }
            })
            .request("${Constants.TESTIMONIES_URL}/$testimony_id")
    }

    private fun renderTestimony() {
        Picasso.get()
            .load(testimony?.user?.picture)
            .placeholder(R.drawable.ic_user_100)
            .error(R.drawable.ic_user_100)
            .into(userImage)

        toolbar.pageTitle = testimony?.user?.name
        userName.text = testimony?.user?.name
        testimonyTitle.text = testimony?.title
        testimonySummary.text =
            "${testimony?.date?.formatted}\n${testimony?.date?.approx}\n${testimony?.comments_count} comments"
        testimonyBody.text = testimony?.testimony

        if (testimony?.resource === null)
            testimonyLink.visibility = View.GONE
        testimonyLink.setOnClickListener {
            Utils.openURL(
                this@TestimonyActivity,
                testimony?.resource
            )
        }

        if(testimony?.images?.size === 0)
            tImages.visibility = View.GONE
        val adapter = ImagesAdapter(this@TestimonyActivity, testimony!!.images)
        testimonyImages.layoutManager = GridLayoutManager(this@TestimonyActivity, 2)
        testimonyImages.adapter = adapter
        testimonyImages.addOnItemTouchListener(RecyclerTouchListener(this, testimonyImages, this))
    }

    override fun onClick(view: View?, position: Int) {

    }

    override fun onLongClick(view: View?, position: Int) {
        //leave as is
    }
}
