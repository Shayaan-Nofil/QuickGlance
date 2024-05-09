package com.ShayaanNofil.i210450

import Memories
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.m_abdullah.quickglance.R

class memories_recycle_adapter(private val items: MutableList<Memories>): RecyclerView.Adapter<memories_recycle_adapter.ViewHolder>() {

    private lateinit var onClickListener: memories_recycle_adapter.OnClickListener

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val memoryimg: ImageView = itemView.findViewById(R.id.memory_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.memories_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        var rqoptions: RequestOptions = RequestOptions()
        rqoptions= rqoptions.transform(CenterCrop(), RoundedCorners(40))
        Glide.with(holder.itemView)
            .load(post.content)
            .apply(rqoptions)
            .into(holder.memoryimg)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(position, post)
        }
    }

    // A function to bind the onclickListener.
    fun setOnClickListener(onClickListener: Any) {
        this.onClickListener = onClickListener as OnClickListener
    }

    // onClickListener Interface
    interface OnClickListener {
        fun onClick(position: Int, model: Memories)
    }

}