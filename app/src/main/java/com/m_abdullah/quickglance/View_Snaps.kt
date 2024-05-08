package com.m_abdullah.quickglance

import Snap
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

class View_Snaps : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snaps)

        val model = intent.getSerializableExtra("object") as User
        val nextsnap = findViewById<View>(R.id.nextsnap_button)
        val snaparray = mutableListOf<Snap>()
        val mAuth = Firebase.auth

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Snaps").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    snaparray.clear()
                    for (data in snapshot.children){
                        val myclass = data.getValue(Snap::class.java)
                        if (myclass != null) {
                            snaparray.add(myclass)
                        }
                    }
                    displaysnap(snaparray.first())
                    val temp = snaparray.first()
                    FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Snaps").child(temp.id).removeValue()
                    snaparray.removeFirst()


                    nextsnap.setOnClickListener(){
                        if (snaparray.isEmpty()){
                            val intent = Intent(this@View_Snaps, chatspage_Activity::class.java)
                            intent.putExtra("object", model)
                            startActivity(intent)
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
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        findViewById<Button>(R.id.back_button).setOnClickListener{
            val intent = Intent(this@View_Snaps, chatspage_Activity::class.java)
            intent.putExtra("object", model)
            startActivity(intent)
            finish()
        }
    }

    fun displaysnap(snap: Snap){
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