package com.m_abdullah.quickglance

import Friend
import Stories
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.camera.view.PreviewView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ShayaanNofil.i210450.stories_recycle_adapter
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var mAuth: FirebaseAuth
class Stories_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stories_page)
        mAuth = Firebase.auth

        val storyarray: MutableList<Stories> = mutableListOf()
        val useridarray: MutableList<String> = mutableListOf()

        val recycle_stories: RecyclerView = findViewById(R.id.stories_recycler)
        recycle_stories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Friends").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                useridarray.clear()
                useridarray.add(mAuth.uid.toString())
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(Friend::class.java)
                        if (myclass != null) {
                            useridarray.add(myclass.friendid)
                        }
                    }

                    FirebaseDatabase.getInstance().getReference("Stories").addValueEventListener(object:
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            storyarray.clear()
                            if (snapshot.exists()){

                                for (data in snapshot.children){
                                    val myclass = data.getValue(Stories::class.java)
                                    if (myclass != null) {
                                        if (useridarray.contains(myclass.senderid)){
                                            storyarray.add(myclass)
                                            useridarray.remove(myclass.senderid)
                                        }
                                    }
                                }

                                if (storyarray.isNotEmpty()){
                                    findViewById<TextView>(R.id.no_stories).visibility = View.GONE

                                    storyarray.reverse()
                                    val adapter = stories_recycle_adapter(storyarray)
                                    recycle_stories.adapter = adapter

                                    adapter.setOnClickListener(object :
                                        stories_recycle_adapter.OnClickListener {
                                        override fun onClick(position: Int, model: Stories) {
                                            val intent = Intent(this@Stories_Activity, View_Stories::class.java)
                                            intent.putExtra("object", model.senderid)
                                            startActivity(intent)
                                        }
                                    })
                                }
                                else{
                                    findViewById<TextView>(R.id.no_stories).visibility = View.VISIBLE
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val rootLayout = findViewById<LinearLayout>(R.id.root_layout)
        val swiperight = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.x - e1!!.x
                if (diffX > 100) {
                    finish()
                    overridePendingTransition(R.anim.slide_in_left,R.anim.fade_out)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        rootLayout.setOnTouchListener { _, event ->
            swiperight.onTouchEvent(event)
            true
        }

        findViewById<Button>(R.id.back_button).setOnClickListener{
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            finish()
        }
        findViewById<Button>(R.id.addfriend_button).setOnClickListener{
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this@Stories_Activity, Add_friends_activity::class.java)
            startActivity(intent)

            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top)
        }


    }
}