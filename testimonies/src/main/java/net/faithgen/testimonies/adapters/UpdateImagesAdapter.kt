package net.faithgen.testimonies.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Image
import net.faithgen.testimonies.viewholders.TestimonyImageHolder
import java.io.File

/**
 * This class renders images from the server for a given testimony
 */
final class UpdateImagesAdapter(
    private val context: Context,
    private val images: List<Image>,
    private val imageListener: TestimonyImagesAdapter.ImageListener?
) : RecyclerView.Adapter<TestimonyImageHolder>() {

    private val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestimonyImageHolder {
        return TestimonyImageHolder(layoutInflater.inflate(R.layout.grid_item_image, parent, false))
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: TestimonyImageHolder, position: Int) {
        val image = images.get(position)
        holder.removeImage.setOnClickListener {
            if (imageListener !== null)
                imageListener.onRemoved(position, null)
        }
        Picasso.get()
            .load(image.avatar._100)
            .placeholder(R.drawable.ef_image_placeholder)
            .error(R.drawable.ef_image_placeholder)
            .into(holder.imageView)
    }
}