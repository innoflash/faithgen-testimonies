package net.faithgen.testimonies.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_create_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R

final class CreateTestimonyActivity : FaithGenActivity() {
    private val imageMaxs : HashMap<String, Int> = hashMapOf(
        Pair(Constants.PREMIUM, Constants.Numbers.PREMIUM_MAX),
        Pair(Constants.PREMIUM_PLUS, Constants.Numbers.PREMIUM_PLUS_MAX),
        Pair(Constants.FREE, Constants.Numbers.FREE_MAX)
    )

    private val maxImages : Int by lazy { imageMaxs.get(SDK.getMinistry().account)!! }

    override fun getPageTitle() = Constants.CREATE_TESTIMONY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_testimony)

        if (SDK.getMinistry().account != Constants.PREMIUM_PLUS)
            testimonyLink.visibility = View.GONE
        if (SDK.getMinistry().account == Constants.FREE) {
            tImages.visibility = View.GONE
        }
        tImages.text = "${tImages.text} ($maxImages max)"
    }
}
