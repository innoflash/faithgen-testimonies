package net.faithgen.testimonies

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_testimonies.*
import net.faithgen.sdk.FaithGenActivity

class TestimoniesActivity : FaithGenActivity() {
    override fun getPageTitle(): String {
        return Constants.TESTIMONIES
    }

    override fun getPageIcon(): Int = R.drawable.ic_testimonies_50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testimonies)

    }

}
