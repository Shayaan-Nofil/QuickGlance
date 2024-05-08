package com.m_abdullah.quickglance

import Memories
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import kotlin.random.Random

class Send_snaps : AppCompatActivity() {
    private var imageUri: String = ""
    private var videoUri: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_snaps)

        val imageviewer = findViewById<ImageView>(R.id.image_viewer)
        imageviewer.visibility = View.GONE
        val videoviewer = findViewById<VideoView>(R.id.video_viewer)
        videoviewer.visibility = View.GONE
        val imageUriExtra = intent.getStringExtra("imageUri")
        val videoUriExtra = intent.getStringExtra("videoUri")

        imageUri = imageUriExtra?.toString() ?: ""
        videoUri = videoUriExtra?.toString() ?: ""
        var contentUri = ""
        var tag = ""

        if (imageUri.isNotEmpty()){
            contentUri = imageUri
            tag = "image"
            imageviewer.visibility = View.VISIBLE
            Glide.with(this@Send_snaps)
                .load(imageUri)
                .centerCrop()
                .into(imageviewer)
        }
        else if(videoUri.isNotEmpty()){
            contentUri = videoUri
            tag = "video"
            videoviewer.visibility = View.VISIBLE
            videoviewer.setVideoURI(Uri.parse(videoUri))
            videoviewer.start()
            videoviewer.setZOrderOnTop(true);
            videoviewer.setOnCompletionListener {
                videoviewer.start() // start the video again when it completes, creating a loop
            }
        }

        val memorybutton = findViewById<Button>(R.id.memories_button)
        memorybutton.setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            Thread(Runnable {
                Log.w("TAG", "Uploading")
                var memory = Memories()
                var mAuth = Firebase.auth
                memory.time = Calendar.getInstance().time.toString()
                memory.tag = tag

                val storageref = FirebaseStorage.getInstance().reference

                storageref.child(mAuth.uid.toString()).child("Memories").child(mAuth.uid.toString() + Random.nextInt(0,100000).toString()).putFile(Uri.parse(contentUri)).addOnSuccessListener {
                    it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {task ->
                        memory.content = task.toString()
                        Log.w("TAG", "Adding to database")
                        memorybutton.setBackgroundResource(R.drawable.tick_icon_white)
                        val id = FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Memories").push().key.toString()
                        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Memories").child(id).setValue(memory)
                    }
                }.addOnFailureListener{
                    Log.w("TAG", "Upload failed")
                }
            }).start()
        }

        findViewById<Button>(R.id.back_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            finish()
        }

        findViewById<Button>(R.id.Sendto_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this@Send_snaps, Select_Senders_Snap::class.java)
            intent.putExtra("ContentUri", contentUri)
            intent.putExtra("TAG", tag)
            startActivity(intent)
        }
    }
}