package net.faithgen.testimonies.tasks

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import com.esafirm.imagepicker.model.Image
import net.faithgen.sdk.utils.Progress
import net.faithgen.testimonies.utils.Constants
import java.io.File
import java.util.*

/**
 * This encodes images to base64 images
 *
 * Takes in a list of images and encode
 */
final class EncodeImages(private val context: Context, private val encodingListener: EncodingListener?) :
    AsyncTask<List<Image>, Int, List<String>>() {
    private val encodedImages: MutableList<String> = mutableListOf()

    public interface EncodingListener {
        fun onEncodeFinished(encodedImages: List<String>)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Progress.showProgress(context, Constants.ENCODING_IMAGES)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doInBackground(vararg p0: List<Image>?): List<String> {
        val images: List<Image> = p0.get(0)!!
        images.forEach { image ->
            val file = File(image.path)
            val encodedImage = Base64.getEncoder().encodeToString(file.readBytes())
            encodedImages.add(encodedImage)
        }
        return encodedImages
    }

    override fun onPostExecute(result: List<String>?) {
        super.onPostExecute(result)
        Progress.dismissProgress()
        if (encodingListener !== null)
            encodingListener.onEncodeFinished(result!!)
    }
}