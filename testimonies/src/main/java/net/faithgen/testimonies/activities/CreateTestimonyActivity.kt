package net.faithgen.testimonies.activities

import android.content.Intent
import android.os.Bundle
import com.android.volley.Request
import com.esafirm.imagepicker.features.ImagePicker
import kotlinx.android.synthetic.main.activity_create_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Response
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.utils.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.tasks.EncodeImages
import net.faithgen.testimonies.utils.TestimonyCRUDViewUtil
import net.faithgen.testimonies.utils.TestimonyImagesViewUtil

/**
 * The activity to create a testimony
 */
final class CreateTestimonyActivity : FaithGenActivity() {

    /**
     * Manipulate views and data from the basic parts of the
     * testimony creating/updating
     */
    private var crudViewUtil: TestimonyCRUDViewUtil? = null

    /**
     * Handles image picking
     */
    private var imagesViewUtil: TestimonyImagesViewUtil? = null

    //handles API calls
    private val faithGenAPI: FaithGenAPI by lazy { FaithGenAPI(this) }

    override fun getPageTitle() = Constants.CREATE_TESTIMONY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_testimony)

        crudViewUtil = TestimonyCRUDViewUtil(view, null)
        crudViewUtil!!.initViews()

        imagesViewUtil = TestimonyImagesViewUtil(view, this@CreateTestimonyActivity)
        imagesViewUtil!!.initViews()

        createTestimony.setOnClickListener { prepareTestimony() }
    }

    /**
     * Prepares params for uploading
     *
     * If testimony has images it will encode first before uploading
     */
    private fun prepareTestimony() {
        if (imagesViewUtil!!.getImages().isEmpty())
            uploadTestimony()
        else encodeImages()
    }

    /**
     * This uploads the testimony to the server
     */
    private fun uploadTestimony() {
        faithGenAPI
            .setParams(crudViewUtil?.getParams() as HashMap<String, String>)
            .setMethod(Request.Method.POST)
            .setProcess(Constants.CREATING_TESTIMONIES)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val response: Response<*> =
                        GSONSingleton.instance.gson.fromJson(serverResponse, Response::class.java)
                    Dialogs.showOkDialog(this@CreateTestimonyActivity, response.message) {
                        if (response.success)
                            openMyTestimonies()
                    }
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
                crudViewUtil!!.getParams()
                    .put(Constants.IMAGES, GSONSingleton.instance.gson.toJson(encodedImages))
                uploadTestimony()
            }
        }).execute(imagesViewUtil!!.getImages())
    }

    /**
     * Receives images from the selector
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data))
            imagesViewUtil!!.processImages(data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
