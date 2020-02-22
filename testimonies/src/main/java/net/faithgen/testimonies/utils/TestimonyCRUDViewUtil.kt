package net.faithgen.testimonies.utils

import android.view.View
import android.widget.EditText
import net.faithgen.sdk.SDK
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Testimony
import net.innoflash.iosview.InputField

/**
 * This handles the testimony main content for create and update
 */
final class TestimonyCRUDViewUtil(private val view: View, private val testimony: Testimony?) {

    private val testimonyLink: InputField by lazy { view.findViewById<InputField>(R.id.testimonyLink) }

    private val testimonyTitle: InputField by lazy { view.findViewById<InputField>(R.id.testimonyTitle) }

    private val testimonyBody: EditText by lazy { view.findViewById<EditText>(R.id.testimonyBody) }

    /**
     * The parameters to send to the server when creating
     * or updating a testimony
     */
    private val params: MutableMap<String, String> = mutableMapOf()

    /**
     * This decides which items to show on the view judging
     * from ministry subscription
     */
    fun initViews() {
        if (SDK.getMinistry().account != Constants.PREMIUM_PLUS)
            testimonyLink.visibility = View.GONE

        if (testimony !== null) fillTestimony()
    }

    fun getParams(): MutableMap<String, String> {
        params.put(Constants.TITLE, testimonyTitle.value)
        if (testimonyLink.value.isNotEmpty())
            params.put(Constants.RESOURCE, testimonyLink.value)
        return params
    }

    /**
     * This loads the testimony into the views if its an update command
     */
    private fun fillTestimony() {
        testimonyTitle.value = testimony?.title
        testimonyLink.value = testimony?.resource
        testimonyBody.setText(testimony?.testimony)
    }
}