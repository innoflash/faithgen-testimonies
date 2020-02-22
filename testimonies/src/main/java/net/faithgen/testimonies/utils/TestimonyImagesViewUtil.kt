package net.faithgen.testimonies.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import net.faithgen.sdk.SDK
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.TestimonyImagesAdapter
import java.util.ArrayList

/**
 * This class handles image picking from the gallery to upload
 */
final class TestimonyImagesViewUtil(
    private val view: View,
    private val context: Context,
    private val serverImagesCount: Int = 0
) {
    /**
     * Initial image count
     */
    private var imagesLeft: Int = 0

    /**
     * The list of images on the select transaction
     */
    private val images: MutableList<Image> = mutableListOf()

    /**
     * Text to show how many images should be selected
     */
    private val imagesTextView: TextView by lazy { view.findViewById<TextView>(R.id.tImages) }

    /**
     * The button to open image chooser
     */
    private val selectImages: Button by lazy { view.findViewById<Button>(R.id.selectImages) }

    /**
     * The view to load selected images
     */
    private val imagesList: RecyclerView by lazy { view.findViewById<RecyclerView>(R.id.imagesList) }

    /**
     * Maps maximum images to select for an upload per ministry subscription level
     */
    private val imageMaxs: HashMap<String, Int> = hashMapOf(
        Pair(Constants.PREMIUM, Constants.Numbers.PREMIUM_MAX),
        Pair(Constants.PREMIUM_PLUS, Constants.Numbers.PREMIUM_PLUS_MAX),
        Pair(Constants.FREE, Constants.Numbers.FREE_MAX)
    )

    /**
     * Fetches the maximum image number from the map
     */
    private val maxImages: Int by lazy { imageMaxs.get(SDK.getMinistry().account)!! - serverImagesCount }

    /**
     * This gives views event listeners
     */
    fun initViews() {
        if (SDK.getMinistry().account == Constants.FREE) {
            imagesTextView.visibility = View.GONE
            selectImages.visibility = View.GONE
        }
        imagesTextView.text = "${imagesTextView.text} ($maxImages max)"

        selectImages.setOnClickListener {
            imagesLeft = maxImages - images.size
            if (imagesLeft === 0)
                Dialogs.showOkDialog(context, Constants.IMAGES_FULL, false)
            else
                ImagePicker.create(context as Activity)
                    .multi()
                    .showCamera(false)
                    .toolbarImageTitle(Constants.TAP_TO_SELECT)
                    .limit(imagesLeft)
                    .exclude(images as ArrayList<Image>?)
                    .start()
        }

        imagesList.layoutManager = GridLayoutManager(context, 2)
    }

    /**
     * Gets the images selected from the image selector
     *
     * @return List<Image>
     */
    fun getImages(): List<Image> = images

    /**
     * This processes images received on activity result
     * in short after images are selected
     *
     * @param data The images data
     */
    fun processImages(data: Intent?) {
        images.addAll(ImagePicker.getImages(data))
        renderImages()
    }

    /**
     * Displays the images chosen from the image picker
     */
    private fun renderImages() {
        val adapter =
            TestimonyImagesAdapter(context, images, object : TestimonyImagesAdapter.ImageListener {
                override fun onRemoved(position: Int, image: Image?) {
                    images.removeAt(position)
                    renderImages()
                }
            })
        imagesList.adapter = adapter
    }
}