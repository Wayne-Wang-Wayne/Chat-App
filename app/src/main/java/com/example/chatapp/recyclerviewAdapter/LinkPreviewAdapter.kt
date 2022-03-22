package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.IntentUtil.intentToLink
import com.example.chatapp.util.PreviewLinkUtil

class LinkPreviewAdapter(
    val context: Context,
    private var previewLinkList: ArrayList<String>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class LinkPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val whole_link_preview_item: CardView = itemView.findViewById(R.id.whole_link_preview_item)
        val iv_link_picture_parent: RelativeLayout =
            itemView.findViewById(R.id.iv_link_picture_parent)
        val iv_link_picture: ImageView = itemView.findViewById(R.id.iv_link_picture)
        val link_text_parent: LinearLayout = itemView.findViewById(R.id.link_text_parent)
        val tv_link_title: TextView = itemView.findViewById(R.id.tv_link_title)
        val tv_link_description: TextView = itemView.findViewById(R.id.tv_link_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_preview_link, parent, false)
        return LinkPreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LinkPreviewViewHolder)
        PreviewLinkUtil().getLinkPreviewData(
            context,
            previewLinkList[position],
            holder.iv_link_picture,
            holder.tv_link_title,
            holder.tv_link_description
        )
        holder.itemView.setSafeOnClickListener {
            intentToLink(context, previewLinkList[position])
        }
    }

    override fun getItemCount(): Int {
        return previewLinkList.size
    }
}