package com.m_abdullah.quickglance

import Chats
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
                                val adapter = chatsearch_recycle_adapter(chatarray)
                                recycle_chat.adapter = adapter

                                adapter.setOnAcceptClickListener(object :
                                    chatsearch_recycle_adapter.OnAcceptClickListener {
                                    override fun onAcceptClick(position: Int, model: Chats) {
                                        val intent = Intent(this@chatspage_Activity, Chat_Activity::class.java)
                                        intent.putExtra("object", model)
                                        startActivity(intent)
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

        findViewById<Button>(R.id.Camerapage_button).setOnClickListener(){
            finish()
            overridePendingTransition(0, R.anim.slide_out_left)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, R.anim.slide_out_left)
    }
}