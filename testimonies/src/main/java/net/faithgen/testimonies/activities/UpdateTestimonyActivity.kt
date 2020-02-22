package net.faithgen.testimonies.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.Request
import kotlinx.android.synthetic.main.activity_update_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Response
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Testimony
import net.faithgen.testimonies.utils.Constants
import net.faithgen.testimonies.utils.TestimonyCRUDViewUtil

/**
 * This updates the testimony
 */
final class UpdateTestimonyActivity : FaithGenActivity() {
    override fun getPageTitle() = Constants.UPDATE_TESTIMONY

    // gets testimony id from intent extras
    private val testimonyId: String by lazy { intent.getStringExtra(Constants.TESTIMONY_ID) }

    //handles API calls
    private val faithGenAPI: FaithGenAPI by lazy { FaithGenAPI(this) }

    // gets the testimony from intent extra and decode it back to an object
    private val testimony: Testimony by lazy {
        GSONSingleton.instance.gson.fromJson(
            intent.getStringExtra(
                Constants.TESTIMONY
            ), Testimony::class.java
        )
    }

    private var crudViewUtil: TestimonyCRUDViewUtil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_testimony)

        crudViewUtil = TestimonyCRUDViewUtil(view, testimony)

        updateTestimony.setOnClickListener { runUpdateRequest() }
    }

    /**
     * Updates the server with the given data
     */
    private fun runUpdateRequest() {
        crudViewUtil!!.getParams().put(Constants.TESTIMONY_ID, testimonyId)
        faithGenAPI
            .setParams(crudViewUtil!!.getParams() as HashMap<String, String>)
            .setProcess(Constants.UPDATING_TESTIMONY)
            .setMethod(Request.Method.POST)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val response: Response<*> =
                        GSONSingleton.instance.gson.fromJson(serverResponse, Response::class.java)
                    val resultIntent = Intent()
                    resultIntent.putExtra(Constants.SHOULD_REFRESH, true)
                    setResult(Activity.RESULT_OK, resultIntent)
                    Dialogs.showOkDialog(this@UpdateTestimonyActivity, response.message, true)
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(
                        this@UpdateTestimonyActivity,
                        errorResponse?.message,
                        false
                    )
                }
            })
         //   .request("${Constants.TESTIMONIES_URL}/update")
    }

    /**
     * Load view on start
     */
    override fun onStart() {
        super.onStart()
        crudViewUtil!!.initViews()
    }
}
