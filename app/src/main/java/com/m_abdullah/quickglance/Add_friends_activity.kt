package com.m_abdullah.quickglance

import Requests
import User
import adduser_recycle_adapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
class Add_friends_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)
        mAuth = Firebase.auth

        getfriends("")

        val searchbutton=findViewById<View>(R.id.search_button)
        searchbutton.setOnClickListener(View.OnClickListener {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val searchtext : EditText = findViewById(R.id.search_box)
            getfriends(searchtext.text.toString())
        })
        findViewById<Button>(R.id.back_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))


            finish()
            overridePendingTransition(0, R.anim.slide_out_top)
        }
    }
    private fun getfriends(username: String){
        val friendsid: MutableList<String> = mutableListOf()

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Friends").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var exists = false
                    for (data in snapshot.children){
                        val myclass = data.getValue(String::class.java)
                        if (myclass != null){
                            friendsid.add(myclass)
                        }
                    }
                    displayusers(friendsid, username)
                }
                else{
                    displayusers(friendsid, username)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

    }

    private fun displayusers(friendsid: MutableList<String>, username: String){
        val recycle_topmentor: RecyclerView = findViewById(R.id.addusers_recycle)
        recycle_topmentor.layoutManager = LinearLayoutManager(this)

        val userarray: MutableList<User> = mutableListOf()

        FirebaseDatabase.getInstance().getReference("User").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userarray.clear()
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(User::class.java)
                        if (myclass != null) {
                            if (username.isNotEmpty()){
                                if (myclass.name.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT)) && myclass.id != mAuth.uid.toString() && !friendsid.contains(myclass.id)){
                                    userarray.add(myclass)
                                }
                            }
                            else{
                                if (myclass.id != mAuth.uid.toString() && !friendsid.contains(myclass.id)){
                                    userarray.add(myclass)
                                }
                            }
                        }
                    }
                    Log.w("TAG", "Display recycle")
                    val adapter = adduser_recycle_adapter(userarray)
                    recycle_topmentor.adapter = adapter

                    adapter.setOnAcceptClickListener(object :
                        adduser_recycle_adapter.OnAcceptClickListener {
                        override fun onAcceptClick(position: Int, model: User) {
                            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

                            var req = Requests()
                            req.id = FirebaseDatabase.getInstance().getReference("Requests").push().key.toString()
                            req.senderid = mAuth.uid.toString()
                            req.recieverid = model.id

                            database = FirebaseDatabase.getInstance().getReference("Requests")
                            database.child(req.id).setValue(req).addOnCompleteListener {
                                Log.w("TAG", "Friend request sent")
                                //sendNotiftoRecepients(req)
                            }.addOnFailureListener{
                                Log.w("TAG", "Friend request not sent")
                            }
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, R.anim.slide_out_top)
    }
}