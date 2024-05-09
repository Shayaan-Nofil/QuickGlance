package com.m_abdullah.quickglance

import Memories
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ShayaanNofil.i210450.memories_recycle_adapter
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

private lateinit var mAuth: FirebaseAuth
class Memories_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories)
        mAuth = Firebase.auth

        val memoryarray: MutableList<Memories> = mutableListOf()

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Memories").get().addOnSuccessListener {
            if (it.exists()){
                memoryarray.clear()
                for (data in it.children){
                    val myclass = data.getValue(Memories::class.java)
                    if (myclass != null) {
                        memoryarray.add(myclass)
                    }
                }

                if (memoryarray.isNotEmpty()){
                    findViewById<TextView>(R.id.no_memories).visibility = View.GONE
                    memoryarray.reverse()
                    val recycle_memories: RecyclerView = findViewById(R.id.memories_recycler)
                    recycle_memories.layoutManager = GridLayoutManager(this, 3)
                    val adapter = memories_recycle_adapter(memoryarray)
                    recycle_memories.adapter = adapter

                    adapter.setOnClickListener(object :
                        memories_recycle_adapter.OnClickListener {
                        override fun onClick(position: Int, model: Memories) {
                            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

                            val dialogView = LayoutInflater.from(this@Memories_Activity).inflate(R.layout.image_fullscreen_viewer, null)
                            val builder = AlertDialog.Builder(this@Memories_Activity).setView(dialogView)
                            val alertDialog = builder.show()

                            val window = alertDialog.window

                            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

                            val backbutton = dialogView.findViewById<Button>(R.id.back_button)
                            backbutton.setOnClickListener(View.OnClickListener {
                                alertDialog.dismiss()
                            })

                            val imageView = dialogView.findViewById<ImageView>(R.id.image_displayer)
                            Glide.with(this@Memories_Activity)
                                .load(model.content)
                                .into(imageView)
                        }
                    })
                }
                else{
                    findViewById<TextView>(R.id.no_memories).visibility = View.VISIBLE
                }
            }
        }

        findViewById<Button>(R.id.back_button).setOnClickListener{
            finish()
            overridePendingTransition(0, R.anim.slide_out_bottom)
        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, R.anim.slide_out_bottom)
    }
}