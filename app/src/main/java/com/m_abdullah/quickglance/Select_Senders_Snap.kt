package com.m_abdullah.quickglance

import Friend
import Snap
import Streak
import User
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.Toast
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
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import senduser_recycle_adapter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private lateinit var mAuth: FirebaseAuth
private lateinit var storage: FirebaseStorage
class Select_Senders_Snap : AppCompatActivity() {
    private var contentUri = ""
    private var tag = ""
    private var username = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_senders_snap)
        mAuth = Firebase.auth

        contentUri = intent.getStringExtra("ContentUri")?:""
        tag = intent.getStringExtra("TAG")?:""
        Log.w("TAG", contentUri)
        Log.w("TAG", tag)
        displayusers()

        findViewById<Button>(R.id.back_button).setOnClickListener(){
            finish()
        }

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val myclass = snapshot.getValue(User::class.java)
                    if (myclass != null) {
                        username = myclass.username
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Error getting data", error.toException())
            }
        })

    }

    private fun displayusers(){
        val recycle_topmentor: RecyclerView = findViewById(R.id.addusers_recycle)
        recycle_topmentor.layoutManager = LinearLayoutManager(this)

        val userarray: MutableList<User> = mutableListOf()
        val friendarray: MutableList<String> = mutableListOf()
        var sendarray: MutableList<String> = mutableListOf()
        var adapter = senduser_recycle_adapter(userarray, sendarray)

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Friends").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendarray.clear()
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(Friend::class.java)
                        if (myclass != null) {
                           friendarray.add(myclass.friendid)
                        }
                    }
                    FirebaseDatabase.getInstance().getReference("User").addValueEventListener(object:
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            userarray.clear()
                            if (snapshot.exists()){
                                for (usr in snapshot.children){
                                    val myclass = usr.getValue(User::class.java)
                                    if (friendarray.contains(myclass!!.id) && myclass.id != mAuth.uid.toString()) {
                                        userarray.add(myclass)
                                    }
                                }

                                adapter = senduser_recycle_adapter(userarray, sendarray)
                                recycle_topmentor.adapter = adapter
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.w("TAG", "Error getting data", error.toException())
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        findViewById<Button>(R.id.send_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

                sendarray = adapter.getsendarray()
                var snap = Snap()
                snap.time = Calendar.getInstance().time.toString()
                snap.senderid = mAuth.uid.toString()
                snap.tag = tag
                snap.content = contentUri

                storage = FirebaseStorage.getInstance()
                val storageref = storage.reference
                val userId = mAuth.uid;

                contentUri.let{
                    storageref.child("Snaps").child(userId!! + Random.nextInt(0,100000).toString()).putFile(
                        Uri.parse(contentUri)).addOnSuccessListener {
                        it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {task ->
                            snap.content = task.toString()
                            Log.w("TAG", "Upload Success")

                            for (usr in sendarray){
                                snap.id = FirebaseDatabase.getInstance().getReference("User").child(usr).child("Snaps").push().key.toString()
                                FirebaseDatabase.getInstance().getReference("User").child(usr).child("Snaps").child(snap.id).setValue(snap)
                                val friend = Friend()
                                friend.lastpost = snap.time
                                friend.friendid = usr
                                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("Friends").child(usr).setValue(friend)

                                //updateStreaks(usr, snap)
                                Toast.makeText(this, "Snap Sent", Toast.LENGTH_SHORT).show()

                                FirebaseDatabase.getInstance().getReference("User").child(usr).get().addOnSuccessListener {
                                    val usr = it.getValue(User::class.java)
                                    if (usr != null && usr.id != mAuth.uid.toString()){
                                        sendPushNotification(usr.token, username+ " sent you a snap", "Description: ", "", mapOf("snapid" to snap.id))
                                    }
                                }
                            }
                        }
                    }.addOnFailureListener{
                        Log.w("TAG", "Upload failed")
                    }
                }

            val intent = Intent(this, Camera_Activity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

//    fun updateStreaks(usr: String, snap: Snap){
//        FirebaseDatabase.getInstance().getReference("Streaks").get().addOnSuccessListener{
//            if (it.exists()){
//                for (data in it.children){
//                    val myclass = data.getValue(Streak::class.java)
//                    if (myclass != null) {
//
//                        if (myclass.user1 == mAuth.uid.toString() && myclass.user2 == usr || myclass.user1 == usr && myclass.user2 == mAuth.uid.toString()){
//                            if (myclass.days == 0){
//                                myclass.time = snap.time
//                                myclass.days = 1
//                            }
//                            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
//                            val past = sdf.parse(myclass.time)
//                            val now = Date()
//
//                            val seconds = (now.time - past.time) / 1000
//                            val minutes = seconds / 60
//                            val hours = minutes / 60
//                            val days = hours / 24
//
//                            myclass.days = days.toInt()
//
//                            FirebaseDatabase.getInstance().getReference("Streaks").child(myclass.id).setValue(myclass)
//                        }
//                    }
//                }
//            }
//        }
//    }
    fun sendPushNotification(token: String, title: String, subtitle: String, body: String, data: Map<String, String> = emptyMap()) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val bodyJson = JSONObject()
        bodyJson.put("to", token)
        bodyJson.put("notification",
            JSONObject().also {
                it.put("title", title)
                it.put("subtitle", subtitle)
                it.put("body", body)
                it.put("sound", "social_notification_sound.wav")
            }
        )
        Log.d("TAG", "sendPushNotification: ${JSONObject(data)}")
        if (data.isNotEmpty()) {
            bodyJson.put("data", JSONObject(data))
        }

        var key="AAAAut-sZzQ:APA91bHEEIvHTZXQOqdhNiOQ114xLW02FAh_c_aBw-G46e2AWBPjkrEnYIpTA9r3yGAzi4vkQdGwJj8sYG4Bu08ibbd-xkkz99xQvm4qd44OkX-EbYxFPv41ndnhESiA92XJ_ytKWftn"
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "key=$key")
            .post(
                bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    println("Received data: ${response.body?.string()}")
                    Log.d("TAG", "onResponse: ${response}   ")
                    Log.d("TAG", "onResponse Message: ${response.message}   ")
                }

                override fun onFailure(call: Call, e: IOException) {
                    println(e.message.toString())
                    Log.d("TAG", "onFailure: ${e.message.toString()}")
                }
            }
        )
    }

}