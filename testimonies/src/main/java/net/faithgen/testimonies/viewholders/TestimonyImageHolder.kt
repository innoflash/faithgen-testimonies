package net.faithgen.testimonies.viewholders

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.faithgen.testimonies.R

final class TestimonyImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView by lazy {
        itemView.findViewById<ImageView>(R.id.imageView)
    }

    val removeImage: FloatingActionButton by lazy {
        itemView.findViewById<FloatingActionButton>(R.id.removeImage)
    }
}