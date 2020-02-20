package net.faithgen.testimonies.viewholders

import android.view.View
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.smarteist.autoimageslider.SliderViewAdapter
import net.faithgen.testimonies.R

final class ImageSliderHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
    val commentImage: FloatingActionButton by lazy {
        itemView.findViewById<FloatingActionButton>(R.id.commentImage)
    }
    val deleteImage: FloatingActionButton by lazy {
        itemView.findViewById<FloatingActionButton>(R.id.deleteImage)
    }
    val imageView: ImageView by lazy {
        itemView.findViewById<ImageView>(R.id.image_view)
    }
}