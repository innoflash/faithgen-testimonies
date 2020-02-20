package net.faithgen.testimonies.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esafirm.imagepicker.model.Image
import com.squareup.picasso.Picasso
import net.faithgen.testimonies.R
import net.faithgen.testimonies.viewholders.TestimonyImageHolder
import java.io.File

/**
 * This is a an adapter to display images for uploading
 */
final class TestimonyImagesAdapter(
    private val context: Context,
    private val images: List<Image>,
    private val imageListener: ImageListener?
) :
    RecyclerView.Adapter<TestimonyImageHolder>() {
    private val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    public interface ImageListener {
        fun onRemoved(position: Int, image: Image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestimonyImageHolder {
        return TestimonyImageHolder(layoutInflater.inflate(R.layout.grid_item_image, parent, false))
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: TestimonyImageHolder, position: Int) {
        val image = images.get(position)
        holder.removeImage.setOnClickListener {
            if (imageListener !== null)
                imageListener.onRemoved(position, image)
        }
        Picasso.get()
            .load(File(image.path))
            .placeholder(R.drawable.ef_image_placeholder)
            .error(R.drawable.ef_image_placeholder)
            .into(holder.imageView)
    }
}