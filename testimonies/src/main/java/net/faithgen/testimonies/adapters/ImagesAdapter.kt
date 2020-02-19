package net.faithgen.testimonies.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Image
import net.faithgen.testimonies.viewholders.ImageHolder

class ImagesAdapter(val context: Context, val images: List<Image>) :
    RecyclerView.Adapter<ImageHolder>() {
    private val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val imageView = layoutInflater.inflate(R.layout.list_item_image, parent, false) as ImageView
        return ImageHolder(imageView)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        Picasso.get()
            .load(images.get(position).avatar._100)
            .placeholder(R.drawable.ic_user_100)
            .error(R.drawable.ic_user_100)
            .into(holder.imageView)
    }
}