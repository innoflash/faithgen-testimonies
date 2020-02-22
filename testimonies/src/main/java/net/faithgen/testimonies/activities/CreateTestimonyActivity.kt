package net.faithgen.testimonies.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import kotlinx.android.synthetic.main.activity_create_testimony.*
import kotlinx.android.synthetic.main.layout_testimony_crud.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Response
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.TestimonyImagesAdapter
import net.faithgen.testimonies.tasks.EncodeImages
import java.util.ArrayList

/**
 * The activity to create a testimony
 */
final class CreateTestimonyActivity : FaithGenActivity() {
    private val images: MutableList<Image> = mutableListOf()
    private val params: MutableMap<String, String> = mutableMapOf()
    private var imagesLeft: Int = 0

    private val imageMaxs: HashMap<String, Int> = hashMapOf(
        Pair(Constants.PREMIUM, Constants.Numbers.PREMIUM_MAX),
        Pair(Constants.PREMIUM_PLUS, Constants.Numbers.PREMIUM_PLUS_MAX),
        Pair(Constants.FREE, Constants.Numbers.FREE_MAX)
    )

    private val faithGenAPI: FaithGenAPI by lazy { FaithGenAPI(this) }

    private val maxImages: Int by lazy { imageMaxs.get(SDK.getMinistry().account)!! }

    override fun getPageTitle() = Constants.CREATE_TESTIMONY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_testimony)

        if (SDK.getMinistry().account != Constants.PREMIUM_PLUS)
            testimonyLink.visibility = View.GONE
        if (SDK.getMinistry().account == Constants.FREE) {
            tImages.visibility = View.GONE
            selectImages.visibility = View.GONE
        }
        tImages.text = "${tImages.text} ($maxImages max)"
        selectImages.setOnClickListener {
            imagesLeft = maxImages - images.size
            if (imagesLeft === 0)
                Dialogs.showOkDialog(this@CreateTestimonyActivity, Constants.IMAGES_FULL, false)
            else
                ImagePicker.create(this@CreateTestimonyActivity)
                    .multi()
                    .showCamera(false)
                    .toolbarImageTitle(Constants.TAP_TO_SELECT)
                    .limit(imagesLeft)
                    .exclude(images as ArrayList<Image>?)
                    .start()
        }

        imagesList.layoutManager = GridLayoutManager(this, 2)
        createTestimony.setOnClickListener { prepareTestimony() }
    }

    /**
     * Prepares params for uploading
     *
     * If testimony has images it will encode first before uploading
     */
    private fun prepareTestimony() {
        params.put(Constants.TITLE, testimonyTitle.value)
        params.put(Constants.TESTIMONY, testimonyBody.text.toString())
        if (testimonyLink.value.isNotEmpty())
            params.put(Constants.RESOURCE, testimonyLink.value)
        if (images.size === 0)
            uploadTestimony()
        else encodeImages()
    }

    /**
     * This uploads the testimony to the server
     */
    private fun uploadTestimony() {
        faithGenAPI
            .setParams(params as HashMap<String, String>)
            .setMethod(Request.Method.POST)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val response: Response<*> =
                        GSONSingleton.instance.gson.fromJson(serverResponse, Response::class.java)
                    Dialogs.showOkDialog(this@CreateTestimonyActivity, response.message, false)
                    if (response.success)
                        openMyTestimonies()
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(
                        this@CreateTestimonyActivity,
                        errorResponse?.message,
                        false
                    )
                }
            })
            .request(Constants.TESTIMONIES_URL)
    }

    /**
     * Open the user`s testimonies after a successful upload
     */
    private fun openMyTestimonies() {
        val intent = Intent(this@CreateTestimonyActivity, UserTestimoniesActivity::class.java)
        intent.putExtra(Constants.USER_ID, SDK.getUser().id)
        intent.putExtra(Constants.USER_NAME, Constants.YOUR_TESTIMONIES)
        startActivity(intent)
    }

    /**
     * On exiting the page
     */
    override fun onStop() {
        super.onStop()
        faithGenAPI.cancelRequests()
    }

    /**
     * Encodes the images into base64 for uploading
     */
    private fun encodeImages() {
        EncodeImages(this@CreateTestimonyActivity, object : EncodeImages.EncodingListener {
            override fun onEncodeFinished(encodedImages: List<String>) {
                params.put(Constants.IMAGES, GSONSingleton.instance.gson.toJson(encodedImages))
                uploadTestimony()
            }
        }).execute(images)
    }

    /**
     * Displays the images chosen from the image picker
     */
    private fun renderImages() {
        val adapter =
            TestimonyImagesAdapter(this, images, object : TestimonyImagesAdapter.ImageListener {
                override fun onRemoved(position: Int, image: Image) {
                    images.removeAt(position)
                    renderImages()
                }
            })
        imagesList.adapter = adapter
    }

    /**
     * Receives images from the selector
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            images.addAll(ImagePicker.getImages(data))
            renderImages()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
