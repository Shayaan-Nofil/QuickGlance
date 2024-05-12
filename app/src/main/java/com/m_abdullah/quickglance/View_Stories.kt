package com.m_abdullah.quickglance

import Snap
import Stories
import User
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class View_Stories : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snaps)

        val user = intent.getStringExtra("object").toString()
        Log.w("TAG", user)
        val storiesarray = mutableListOf<Stories>()
        val mAuth = Firebase.auth

        FirebaseDatabase.getInstance().getReference("Stories").get().addOnSuccessListener {
            for (snap in it.children){
                Log.w("TAG", "Populating array")
                val temp = snap.getValue(Stories::class.java)
                if (temp!!.senderid == user){
                    storiesarray.add(temp)
                }
            }
            if (storiesarray.isNotEmpty()){
                Log.w("TAG", storiesarray.size.toString())
                displaysnap(storiesarray.first())
                val temp = storiesarray.first()
                storiesarray.removeFirst()
            }
            else{
                finish()
            }
        }

        findViewById<View>(R.id.nextsnap_button).setOnClickListener(){
            if (storiesarray.isEmpty()){
                finish()
            }
            else{
                displaysnap(storiesarray.first())
                val temp = storiesarray.first()
                Log.w("TAG", temp.id)
                storiesarray.removeFirst()
            }
        }

        findViewById<Button>(R.id.back_button).setOnClickListener{
            finish()
        }
    }

    private fun displaysnap(snap: Stories){
        val imageviewer = findViewById<ImageView>(R.id.image_displayer)
        imageviewer.visibility = View.VISIBLE
        val videoviewer = findViewById<VideoView>(R.id.video_displayer)
        videoviewer.visibility = View.GONE

        if (snap.tag == "image"){
            Log.w("TAG", "Displaying image")
            videoviewer.visibility = View.GONE
            imageviewer.visibility = View.VISIBLE

            Glide.with(this@View_Stories)
                .load(snap.content)
                .centerCrop()
                .into(imageviewer)

        }
        else if(snap.tag == "video"){
            Log.w("TAG", "Displaying video")
            imageviewer.visibility = View.GONE

            videoviewer.visibility = View.VISIBLE
            videoviewer.setVideoURI(Uri.parse(snap.content))
            videoviewer.start()
            videoviewer.setZOrderOnTop(true);
            videoviewer.setOnCompletionListener {
                videoviewer.start()
            }
        }
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        val storyupload = sdf.parse(snap.time)
        val now = Date()

        if ((now.time - storyupload.time)/86400000 >= 1){
            FirebaseDatabase.getInstance().getReference("Stories").child(snap.id).removeValue()
        }

    }
}