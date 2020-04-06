package net.faithgen.testimonies.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.faithgen.testimonies.viewholders.LI3Holder
import net.faithgen.testimonies.R
import net.faithgen.testimonies.models.Testimony
import net.innoflash.iosview.lists.ListItemView3

/**
 * This renders testimonies (adapter)
 */
final class LI3Adapter(private val context: Context, private val testimonies : List<Testimony>) : RecyclerView.Adapter<LI3Holder>(){
    private val inflater : LayoutInflater by lazy {
        LayoutInflater.from(context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LI3Holder {
        val li3View = inflater.inflate(R.layout.li3_item, parent, false) as ListItemView3
        return LI3Holder(li3View)
    }

    override fun getItemCount(): Int = testimonies.size

    override fun onBindViewHolder(holder: LI3Holder, position: Int) {
        val testimony = testimonies.get(position)
        Picasso.get()
            .load(testimony.user.avatar._50)
            .placeholder(R.drawable.ic_user_100)
            .error(R.drawable.ic_user_100)
            .into(holder.li3View.circleImageView)
        holder.li3View.itemHeading = testimony.user.name
        holder.li3View.itemContent = testimony.title
        holder.li3View.itemFooter = testimony.date.approx
    }

}