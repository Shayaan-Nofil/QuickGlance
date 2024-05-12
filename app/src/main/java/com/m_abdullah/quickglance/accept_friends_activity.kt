package com.m_abdullah.quickglance

import Chats
import Friend
import Requests
import Streak
import User
import acceptuser_recycle_adapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

private lateinit var mAuth: FirebaseAuth
private lateinit var database: DatabaseReference
class accept_friends_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_friends)
        mAuth = Firebase.auth

        displayrequests()
    }
    private fun displayrequests(){
        val requesarray: MutableList<Requests> = mutableListOf()
        val userarray: MutableList<User> = mutableListOf()

        FirebaseDatabase.getInstance().getReference("Requests").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requesarray.clear()
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(Requests::class.java)
                        if (myclass != null) {
                            if (myclass.recieverid == mAuth.uid.toString()){
                                requesarray.add(myclass)
                                Log.w("TAG", "Added Requests")
                            }
                            Log.w("TAG", "Found Requests")
                        }
                    }

                    FirebaseDatabase.getInstance().getReference("User").addValueEventListener(object:
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            userarray.clear()
                            if (snapshot.exists()){
                                for (data in snapshot.children){
                                    val myclass = data.getValue(User::class.java)
                                    if (myclass != null) {
                                        for (request in requesarray){
                                            if (request.senderid == myclass.id){
                                                userarray.add(myclass)
                                            }
                                        }
                                    }
                                }

                                val recycle_topmentor: RecyclerView = findViewById(R.id.addusers_recycle)
                                recycle_topmentor.layoutManager = LinearLayoutManager(this@accept_friends_activity, LinearLayoutManager.VERTICAL, false)
                                val adapter = acceptuser_recycle_adapter(userarray)
                                recycle_topmentor.adapter = adapter

                                adapter.setOnAcceptClickListener(object :
                                    acceptuser_recycle_adapter.OnAcceptClickListener {
                                    override fun onAcceptClick(position: Int, model: User) {
                                        FirebaseDatabase.getInstance().getReference("Requests").addValueEventListener(object:
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()){
                                                    for (data in snapshot.children){
                                                        val myclass = data.getValue(Requests::class.java)
                                                        if (myclass != null) {
                                                            if (myclass.senderid == model.id && myclass.recieverid == mAuth.uid.toString()){
                                                                val chat = Chats()
                                                                val streak = Streak()
                                                                streak.user1 = model.id
                                                                streak.user2 = mAuth.uid.toString()
                                                                val friend = Friend()
                                                                friend.friendid = model.id

                                                                chat.id = FirebaseDatabase.getInstance().getReference("Chats").push().key.toString()
                                                                FirebaseDatabase.getInstance().getReference("Chats").child(chat.id).setValue(chat)

                                                                FirebaseDatabase.getInstance().getReference("Requests").child(data.key.toString()).removeValue()

                                                                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Friends").child(model.id).setValue(friend)
                                                                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Chats").child(chat.id).setValue(chat.id)
                                                                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Chats").child(chat.id).setValue(chat.id)

                                                                friend.friendid = mAuth.uid.toString()
                                                                FirebaseDatabase.getInstance().getReference("User").child(model.id).child("Friends").child(mAuth.uid.toString()).setValue(friend)
                                                                FirebaseDatabase.getInstance().getReference("User").child(model.id).child("Chats").child(chat.id).setValue(chat.id)

                                                                streak.id = FirebaseDatabase.getInstance().getReference("Streaks").push().key.toString()
                                                                FirebaseDatabase.getInstance().getReference("Streaks").child(streak.id).setValue(streak)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }
                                        })
                                    }
                                })

                                adapter.setOnRejectClickListener(object :
                                    acceptuser_recycle_adapter.OnRejectClickListener {
                                    override fun onRejectClick(position: Int, model: User) {
                                        FirebaseDatabase.getInstance().getReference("Requests").addValueEventListener(object:
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()){
                                                    for (data in snapshot.children){
                                                        val myclass = data.getValue(Requests::class.java)
                                                        if (myclass != null) {
                                                            if (myclass.senderid == model.id && myclass.recieverid == mAuth.uid.toString()){
                                                                FirebaseDatabase.getInstance().getReference("Requests").child(data.key.toString()).removeValue()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }
                                        })
                                    }
                                })
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
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        finish()
//        overridePendingTransition(0, R.anim.slide_out_top)
//    }
}