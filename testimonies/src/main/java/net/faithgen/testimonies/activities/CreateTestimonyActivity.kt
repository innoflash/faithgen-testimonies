package net.faithgen.testimonies.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import kotlinx.android.synthetic.main.activity_create_testimony.*
import net.faithgen.sdk.FaithGenActivity
import net.faithgen.sdk.SDK
import net.faithgen.sdk.utils.Dialogs
import net.faithgen.testimonies.Constants
import net.faithgen.testimonies.R
import net.faithgen.testimonies.adapters.TestimonyImagesAdapter
import java.util.ArrayList

final class CreateTestimonyActivity : FaithGenActivity() {
    private val images: MutableList<Image> = mutableListOf()
    private var imagesLeft: Int = 0
    private val imageMaxs: HashMap<String, Int> = hashMapOf(
        Pair(Constants.PREMIUM, Constants.Numbers.PREMIUM_MAX),
        Pair(Constants.PREMIUM_PLUS, Constants.Numbers.PREMIUM_PLUS_MAX),
        Pair(Constants.FREE, Constants.Numbers.FREE_MAX)
    )

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
    }

    private fun renderImages() {
        val adapter = TestimonyImagesAdapter(this, images, object : TestimonyImagesAdapter.ImageListener {
            override fun onRemoved(position: Int, image: Image) {
                images.removeAt(position)
                renderImages()
            }
        })
        imagesList.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            images.addAll(ImagePicker.getImages(data))
            renderImages()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
