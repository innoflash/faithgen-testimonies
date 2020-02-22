package net.faithgen.testimonies.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import net.faithgen.sdk.SDK
import net.faithgen.sdk.comments.CommentsSettings
import net.faithgen.sdk.enums.CommentsDisplay
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Response
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.interfaces.DialogListener
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.utils.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.ImagesSliderAdapter
import net.faithgen.testimonies.models.Image
import net.faithgen.testimonies.models.Testimony
import net.innoflash.iosview.DialogFullScreen
import net.innoflash.iosview.DialogToolbar

/**
 * This is a dialog to display testimony images for viewing
 */
final class ImagesSliderDialog(
    private val testimony: Testimony,
    private val position: Int,
    private val context: AppCompatActivity
) : DialogFullScreen() {

    private var sliderView: SliderView? = null
    private val faithGenAPI: FaithGenAPI by lazy { FaithGenAPI(context) }
    private var errorListener: ErrorListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialogs_images_slider, container, false)
        val dialogToolbar = view.findViewById<DialogToolbar>(R.id.dialog_toolbar)
        sliderView = view.findViewById(R.id.sliderView)

        dialogToolbar.dialogFragment = this
        dialogToolbar.title = testimony.title
        return view
    }

    fun setErrorListener(errorListener: ErrorListener) {
        this.errorListener = errorListener
    }

    override fun onStop() {
        super.onStop()
        faithGenAPI.cancelRequests()
    }

    override fun onStart() {
        super.onStart()
        val animationsLength = SliderAnimations.values().size
        val animationPosition = (0..(animationsLength - 1)).shuffled().first()

        val sliderAdapter =
            ImagesSliderAdapter(context, testimony, object : ImagesSliderAdapter.ImageListener {
                override fun commentClicked(testimony: Testimony, image: Image) {
                    SDK.openComments(
                        context, CommentsSettings.Builder()
                            .setCategory(Constants.IMAGES)
                            .setItemId(image.id)
                            .setCommentsDisplay(CommentsDisplay.DIALOG)
                            .setTitle(testimony.title)
                            .build()
                    )
                }

                override fun deleteClicked(testimony: Testimony, image: Image) {
                    val params = hashMapOf<String, String>()
                    params.put(Constants.TESTIMONY_ID, testimony.id)
                    params.put(Constants.IMAGE_ID, image.id)

                    Dialogs.confirmDialog(
                        context,
                        Constants.WARNING,
                        Constants.CONFIRM_IMAGE_DELETE,
                        object : DialogListener() {
                            override fun onYes() {
                                faithGenAPI.setParams(params)
                                    .setMethod(Request.Method.POST)
                                    .setServerResponse(object : ServerResponse() {
                                        override fun onServerResponse(serverResponse: String?) {
                                            val response: Response<*> =
                                                GSONSingleton.instance.gson.fromJson(
                                                    serverResponse,
                                                    Response::class.java
                                                )
                                            if (response.success) {
                                                dismiss()
                                                if (errorListener !== null)
                                                    errorListener!!.onMessage(
                                                        response.message,
                                                        true
                                                    )
                                            }
                                        }

                                        override fun onError(errorResponse: ErrorResponse?) {
                                            if (errorListener !== null)
                                                errorListener!!.onMessage(errorResponse!!.message)
                                        }
                                    })
                                    .request("${Constants.TESTIMONIES_URL}/delete-image")
                            }
                        })
                }
            })
        sliderView!!.setSliderAdapter(sliderAdapter)
        sliderView!!.setIndicatorAnimation(IndicatorAnimations.DROP)
        sliderView!!.setSliderTransformAnimation(SliderAnimations.values()[animationPosition])

        sliderView!!.currentPagePosition = position
    }

    public interface ErrorListener {
        fun onMessage(message: String, closeActivity: Boolean = false)
    }
}