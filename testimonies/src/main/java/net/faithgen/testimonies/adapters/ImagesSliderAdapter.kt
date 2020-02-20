package net.faithgen.testimonies.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.Picasso
import net.faithgen.sdk.SDK
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Image
import net.faithgen.testimonies.models.Testimony
import net.faithgen.testimonies.viewholders.ImageSliderHolder

final class ImagesSliderAdapter(
    private val context: Context,
    private val testimony: Testimony,
    private val imageListener: ImageListener?
) :
    SliderViewAdapter<ImageSliderHolder>() {
    private val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    private val belongsToMe: Boolean by lazy {
        SDK.getUser() !== null && SDK.getUser().id.equals(testimony.user.id)
    }

    override fun onCreateViewHolder(parent: ViewGroup?): ImageSliderHolder {
        return ImageSliderHolder(layoutInflater.inflate(R.layout.list_item_slider, parent, false))
    }

    override fun getCount() = testimony.images.size

    override fun onBindViewHolder(viewHolder: ImageSliderHolder?, position: Int) {
        val image = testimony.images.get(position)
        Picasso.get()
            .load(image.avatar.original)
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)
            .into(viewHolder!!.imageView)

        if (!belongsToMe)
            viewHolder.deleteImage.visibility = View.GONE

        viewHolder.deleteImage.setOnClickListener {
            if (imageListener !== null) imageListener.deleteClicked(testimony, image)
        }
        viewHolder.commentImage.setOnClickListener {
            if (imageListener !== null) imageListener.commentClicked(testimony, image)
        }
    }

    public interface ImageListener {
        fun commentClicked(testimony: Testimony, image: Image)
        fun deleteClicked(testimony: Testimony, image: Image)
    }
}