package com.m_abdullah.quickglance

import Chats
import Snap
import User
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chatsearch_recycle_adapter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var mAuth: FirebaseAuth
private lateinit var userchats : MutableList<String>
class chatspage_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatspage)
        mAuth = Firebase.auth

        var snaparray = mutableListOf<Snap>()

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Snaps").addListenerForSingleValueEvent(object:
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
                    displaychats(snaparray)
                }
                else{
                    displaychats(snaparray)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        findViewById<Button>(R.id.Camerapage_button).setOnClickListener(){
            finish()
            overridePendingTransition(0, R.anim.slide_out_left)
        }
        findViewById<Button>(R.id.profile_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Profile_Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top)
        }

        findViewById<Button>(R.id.addfriend_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Add_friends_activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top)
        }
        findViewById<Button>(R.id.search_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Search_Activity::class.java)
            startActivity(intent)
        }
    }
    fun displaychats(snaparray: MutableList<Snap>){
        val recycle_chat: RecyclerView = findViewById(R.id.chatspage_recyclerview)
        recycle_chat.layoutManager = LinearLayoutManager(this@chatspage_Activity)
        var chatidarray = mutableListOf<String>()

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Chats").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(String::class.java)
                        if (myclass != null) {
                            chatidarray.add(myclass)
                        }
                    }
                    FirebaseDatabase.getInstance().getReference("Chats").addListenerForSingleValueEvent(object:
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val chatarray: MutableList<Chats> = mutableListOf()
                            if (snapshot.exists()){
                                for (data in snapshot.children){
                                    val myclass = data.getValue(Chats::class.java)
                                    if (myclass != null) {
                                        if (chatidarray.contains(myclass.id)){
                                            chatarray.add(myclass)
                                        }
                                    }
                                }
                                val adapter = chatsearch_recycle_adapter(chatarray, snaparray)
                                recycle_chat.adapter = adapter

                                adapter.setOnAcceptClickListener(object :
                                    chatsearch_recycle_adapter.OnAcceptClickListener {
                                    override fun onAcceptClick(position: Int, model: Chats) {
                                        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

                                        val intent = Intent(this@chatspage_Activity, Chat_Activity::class.java)
                                        intent.putExtra("object", model)
                                        startActivity(intent)
                                    }
                                })
                                adapter.setOnClickListener(object :
                                    chatsearch_recycle_adapter.OnClickListener {
                                    override fun onClick(model: User) {
                                        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

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
                                                    if (snaparray.isNotEmpty()){
                                                        val intent = Intent(this@chatspage_Activity, View_Snaps::class.java)
                                                        intent.putExtra("object", model)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                                Log.w("TAG", "Failed to read value.", error.toException())
                                            }
                                        })

                                    }
                                })

                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.w("TAG", "Failed to read value.", error.toException())
                        }
                    })

                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, R.anim.slide_out_left)
    }

}