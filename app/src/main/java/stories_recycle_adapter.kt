package com.ShayaanNofil.i210450

import Stories
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase
import com.m_abdullah.quickglance.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class stories_recycle_adapter(private val items: MutableList<Stories>): RecyclerView.Adapter<stories_recycle_adapter.ViewHolder>() {

    private lateinit var onClickListener: stories_recycle_adapter.OnClickListener

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val storyimg: ImageView = itemView.findViewById(R.id.sender_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.stories_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        FirebaseDatabase.getInstance().getReference("User").child(post.senderid).get().addOnSuccessListener {
            Glide.with(holder.itemView.context)
                .load(it.child("profilepic").value.toString())
                .apply(RequestOptions().transform(CenterCrop()))
                .into(holder.storyimg)
        }

        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        val storyupload = sdf.parse(post.time)
        val now = Date()

        if ((now.time - storyupload.time)/86400000 >= 1){
            FirebaseDatabase.getInstance().getReference("Stories").child(post.id).removeValue()
        }

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
        fun onClick(position: Int, model: Stories)
    }

}