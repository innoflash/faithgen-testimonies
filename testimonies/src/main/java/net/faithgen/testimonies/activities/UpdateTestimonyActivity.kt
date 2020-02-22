package net.faithgen.testimonies.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Testimony
import net.faithgen.testimonies.utils.Constants
import net.faithgen.testimonies.utils.TestimonyCRUDViewUtil

/**
 * This updates the testimony
 */
class UpdateTestimonyActivity : FaithGenActivity() {
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
    }

    /**
     * Load view on start
     */
    override fun onStart() {
        super.onStart()
        crudViewUtil!!.initViews()
    }
}
