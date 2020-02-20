package net.faithgen.testimonies.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.ViewUtil

/**
 * This loads testimonies for a specified user
 */
class UserTestimoniesActivity : FaithGenActivity() {
    private var viewUtil: ViewUtil? = null

    private val userId: String by lazy {
        intent.getStringExtra(Constants.USER_ID)
    }

    override fun getPageTitle(): String {
        return intent.getStringExtra(Constants.USER_NAME)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_testimonies)

        viewUtil = ViewUtil(this, view)
        viewUtil?.initViewsCallbacks()

        if (SDK.getUser() !== null && userId.equals(SDK.getUser().id))
            setOnOptionsClicked(R.drawable.ic_add_blue_18dp) {
                startActivity(
                    Intent(
                        this@UserTestimoniesActivity,
                        CreateTestimonyActivity::class.java
                    )
                )
            }
    }

    /**
     * Fetching the sermon takes place here
     */
    override fun onStart() {
        super.onStart()
        if (viewUtil!!.testimonies.size === 0)
            viewUtil?.loadTestimonies("${Constants.TESTIMONIES_URL}/user/$userId", "", true)
    }
}
