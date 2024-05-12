package com.m_abdullah.quickglance

import Snap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class View_Snaps : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snaps)

        val user = intent.getStringExtra("object").toString()
        Log.w("TAG", user)
        val snaparray = mutableListOf<Snap>()
        val mAuth = Firebase.auth

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Snaps").get().addOnSuccessListener {
            for (snap in it.children){
                Log.w("TAG", "Populating array")
                val temp = snap.getValue(Snap::class.java)
                if (temp!!.senderid == user){
                    snaparray.add(temp)
                }
            }
            if (snaparray.isNotEmpty()){
                Log.w("TAG", snaparray.size.toString())
                displaysnap(snaparray.first())
                val temp = snaparray.first()
                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Snaps").child(temp.id).removeValue()
                snaparray.removeFirst()
            }
            else{
                finish()
            }
        }

        findViewById<View>(R.id.nextsnap_button).setOnClickListener(){
            if (snaparray.isEmpty()){
                finish()
            }
            else{
                displaysnap(snaparray.first())
                val temp = snaparray.first()
                Log.w("TAG", temp.id)
                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Snaps").child(temp.id).removeValue()
                snaparray.removeFirst()
            }
        }

        findViewById<Button>(R.id.back_button).setOnClickListener{
            finish()
        }
    }

    private fun displaysnap(snap: Snap){
        val imageviewer = findViewById<ImageView>(R.id.image_displayer)
        imageviewer.visibility = View.VISIBLE
        val videoviewer = findViewById<VideoView>(R.id.video_displayer)
        videoviewer.visibility = View.GONE

        if (snap.tag == "image"){
            Log.w("TAG", "Displaying image")
            videoviewer.visibility = View.GONE
            imageviewer.visibility = View.VISIBLE

            Glide.with(this@View_Snaps)
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
    }
}