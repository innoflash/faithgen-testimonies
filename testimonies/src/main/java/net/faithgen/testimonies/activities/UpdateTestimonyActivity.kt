package net.faithgen.testimonies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import kotlinx.android.synthetic.main.activity_update_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.sdk.http.ErrorResponse
import net.faithgen.sdk.http.FaithGenAPI
import net.faithgen.sdk.http.Response
import net.faithgen.sdk.http.types.ServerResponse
import net.faithgen.sdk.interfaces.DialogListener
import net.faithgen.sdk.singletons.GSONSingleton
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.TestimonyImagesAdapter
import net.faithgen.testimonies.adapters.UpdateImagesAdapter
import net.faithgen.testimonies.models.Testimony
import net.faithgen.testimonies.tasks.EncodeImages
import net.faithgen.testimonies.utils.Constants
import net.faithgen.testimonies.utils.TestimonyCRUDViewUtil
import net.faithgen.testimonies.utils.TestimonyImagesViewUtil

/**
 * This updates the testimony
 */
final class UpdateTestimonyActivity : FaithGenActivity() {
    override fun getPageTitle() = Constants.UPDATE_TESTIMONY

    private var shouldRefresh: Boolean = false

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

    // serves the basic parts of the testimony
    private var crudViewUtil: TestimonyCRUDViewUtil? = null

    // serves the images section
    private var imagesViewUtil: TestimonyImagesViewUtil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_testimony)

        crudViewUtil = TestimonyCRUDViewUtil(view, testimony)
        imagesViewUtil =
            TestimonyImagesViewUtil(view, this@UpdateTestimonyActivity, testimony.images.size)

        imagesViewUtil?.initViews()

        updateTestimony.setOnClickListener { runUpdateRequest() }
        uploadImages.setOnClickListener { prepareImagesForUpload() }

        if (SDK.getMinistry().account == Constants.FREE)
            uploadImages.visibility = View.GONE

        if (testimony.images.isNullOrEmpty()) theImages.visibility = View.GONE

        renderTestimonyImages()
    }

    override fun onStop() {
        super.onStop()
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.SHOULD_REFRESH, shouldRefresh)
        setResult(Activity.RESULT_OK, resultIntent)
        faithGenAPI.cancelRequests()
    }

    /**
     * Encodes images for upload
     */
    private fun prepareImagesForUpload() {
        if (imagesViewUtil?.getImages().isNullOrEmpty())
            Dialogs.showOkDialog(this@UpdateTestimonyActivity, Constants.NO_IMAGES, false)
        else EncodeImages(this@UpdateTestimonyActivity, object : EncodeImages.EncodingListener {
            override fun onEncodeFinished(encodedImages: List<String>) {
                uploadImagesToServer(encodedImages)
            }
        }).execute(imagesViewUtil?.getImages())
    }

    /**
     * Sends the encoded images to the server
     */
    private fun uploadImagesToServer(encodedImages: List<String>) {
        val params: Map<String, String> = hashMapOf(
            Pair(Constants.TESTIMONY_ID, testimonyId),
            Pair(Constants.IMAGES, GSONSingleton.instance.gson.toJson(encodedImages))
        )
        faithGenAPI
            .setProcess(Constants.UPLOADING_IMAGES)
            .setParams(params as HashMap<String, String>)
            .setMethod(Request.Method.POST)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val response: Response<*> =
                        GSONSingleton.instance.gson.fromJson(serverResponse, Response::class.java)
                    Dialogs.showOkDialog(this@UpdateTestimonyActivity, response.message) {
                        if (response.success)
                            shouldRefresh = true
                        finish()
                    }
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(
                        this@UpdateTestimonyActivity,
                        errorResponse?.message,
                        false
                    )
                }
            })
            .request("${Constants.TESTIMONIES_URL}/add-image")
    }

    /**
     * This renders the current testimony images
     */
    private fun renderTestimonyImages() {
        if (testimony.images.isNotEmpty()) {
            testimonyImages.layoutManager = GridLayoutManager(this, 2)
            val adapter = UpdateImagesAdapter(
                this,
                testimony.images,
                object : TestimonyImagesAdapter.ImageListener {
                    override fun onRemoved(position: Int, image: Image?) {
                        Dialogs.confirmDialog(
                            this@UpdateTestimonyActivity,
                            Constants.WARNING,
                            Constants.CONFIRM_IMAGE_DELETE,
                            object : DialogListener() {
                                override fun onYes() {
                                    val deleteParams: Map<String, String> = mapOf(
                                        Pair(Constants.TESTIMONY_ID, testimonyId),
                                        Pair(Constants.IMAGE_ID, testimony.images.get(position).id)
                                    )
                                    deleteServerImage(deleteParams, position)
                                }
                            })
                    }
                })
            testimonyImages.adapter = adapter
        }
    }

    /**
     * This deletes an image from the server
     *
     * @param deleteParams The params to send to the server
     * @param position of the image on list
     */
    private fun deleteServerImage(deleteParams: Map<String, String>, position: Int) {
        faithGenAPI
            .setParams(deleteParams as HashMap<String, String>)
            .setProcess(Constants.DELETING_IMAGE)
            .setMethod(Request.Method.POST)
            .setServerResponse(object : ServerResponse() {
                override fun onServerResponse(serverResponse: String?) {
                    val response: Response<*> =
                        GSONSingleton.instance.gson.fromJson(serverResponse, Response::class.java)
                    Dialogs.showOkDialog(this@UpdateTestimonyActivity, response.message) {
                        if (response.success) {
                            shouldRefresh = true
                            testimony.images = testimony.images.filter { image ->
                                image.id !== testimony.images.get(position).id
                            }
                            renderTestimonyImages()
                        }
                    }
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(
                        this@UpdateTestimonyActivity,
                        errorResponse?.message,
                        false
                    )
                }
            })
            .request("${Constants.TESTIMONIES_URL}/delete-image")
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
                    Dialogs.showOkDialog(this@UpdateTestimonyActivity, response.message) {
                        if (response.success) {
                            shouldRefresh = true
                            finish()
                        }
                    }
                }

                override fun onError(errorResponse: ErrorResponse?) {
                    Dialogs.showOkDialog(
                        this@UpdateTestimonyActivity,
                        errorResponse?.message,
                        false
                    )
                }
            }).request("${Constants.TESTIMONIES_URL}/update")
    }

    /**
     * Load view on start
     */
    override fun onStart() {
        super.onStart()
        crudViewUtil!!.initViews()
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
