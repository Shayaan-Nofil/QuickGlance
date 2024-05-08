package com.m_abdullah.quickglance

import Chats
import Messages
import User
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat_recycle_adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private lateinit var mAuth: FirebaseAuth
private lateinit var username: String
class Chat_Activity : AppCompatActivity() {
    private var messages: Messages? = null
    private var messageimg: Uri? = null
    private var uploadimgbt : Button? = null
    private var chat : Chats? = null
    private var screenshot: Boolean = false
    private var recording : Boolean = false
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_page)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        chat = intent.getSerializableExtra("object") as Chats

        //Screenshots
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.registerContentObserver(uri, true, screenshotObserver)
        askNotificationPermission()

        var chattername : TextView = findViewById(R.id.ChatterName)
        mAuth = Firebase.auth

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    if (user != null){
                        username = user.name
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        //Get the appropriate name of the chatter
        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot != null) {
                    for (data in snapshot.children) {
                        val user = data.getValue(User::class.java)
                        if (user != null) {
                            FirebaseDatabase.getInstance().getReference("User").child(user.id)
                                .child("Chats")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snap: DataSnapshot) {
                                        if (snap.exists()) {
                                            for (temp in snap.children) {
                                                val chats = temp.getValue(String::class.java)
                                                if (chats != null) {
                                                    if (chats == chat!!.id && user.id != mAuth.uid.toString()) {
                                                        chattername.text = user.name
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
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val recycle_topmentor: RecyclerView = findViewById(R.id.messages_recycler)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        recycle_topmentor.layoutManager = layoutManager

        val messagearray: MutableList<Messages> = mutableListOf()

        FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagearray.clear()
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val myclass = data.getValue(Messages::class.java)
                        if (myclass != null) {
                            messagearray.add(myclass)
                        }
                    }
                    val adapter = chat_recycle_adapter(messagearray)
                    recycle_topmentor.adapter = adapter

                    adapter.setOnClickListener(object :
                        chat_recycle_adapter.OnClickListener {
                        @SuppressLint("ServiceCast")
                        override fun onClick(position: Int, model: Messages) {
                            if (model.senderid == Firebase.auth.uid && model.tag != "image"){
                                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                                // For newer APIs
                                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                showOptionsDialog(model)
                            }
                            else if (model.tag == "image"){
                                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                                // For newer APIs
                                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                showOptionsDialogimage(model)
                            }
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val btsend: Button = findViewById(R.id.send_button)
        btsend.setOnClickListener(View.OnClickListener {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            uploadmessage()
            screenshot = false
        })

        val backbutton: Button = findViewById(R.id.back_arrow)
        backbutton.setOnClickListener(View.OnClickListener {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            finish()
        })

        val vcrecbutton: Button = findViewById(R.id.mic_button)
        vcrecbutton.setOnClickListener() {
            if (!recording){
                recording = true
                vcrecbutton.setBackgroundResource(R.drawable.microphone_red)
                startRecording()
            }
            else {
                recording = false
                vcrecbutton.setBackgroundResource(R.drawable.microphone_grey)
                stopRecording()
                uploadAudio()
                screenshot = false
            }
        }

        uploadimgbt=findViewById(R.id.gallery_button)
        uploadimgbt!!.setOnClickListener(View.OnClickListener {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            messages = Messages()
            openGalleryForImage()
        })
    }

    override fun onStop() {
        super.onStop()
        // Unregister the content observer
        contentResolver.unregisterContentObserver(screenshotObserver)
    }

    private val galleryprofileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null){
            messages!!.tag = "image"
            messageimg = uri

            Glide.with(this)
                .asBitmap()
                .load(messageimg)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val bitmapDrawable = BitmapDrawable(this@Chat_Activity.resources, resource)
                        uploadimgbt!!.background = bitmapDrawable
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle cleanup here
                    }
                })
        }
    }
    private fun openGalleryForImage() {
        galleryprofileLauncher.launch("image/*")
    }
    private fun uploadmessage(){
        var message: Messages = Messages()
        mAuth = Firebase.auth
        if (messages == null){ //Text Messages
            if (screenshot == true){
                message.content = "**User took a screenshot of chat**"
            }
            else{
                val messagecontent : EditText = findViewById(R.id.message_text)
                message.content = messagecontent.text.toString()
                messagecontent.setText("")
            }
            message.time = Calendar.getInstance().time.toString()
            message.senderid = mAuth.uid.toString()
            message.tag = "text"

            if (message.content == ""){
                return
            }

            FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            Log.w("TAG", "in user, getting url")
                            Log.w("TAG", user.profilepic.toString())
                            message.senderpic = user.profilepic.toString()

                            message.id = FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").push().key.toString()
                            FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id).setValue(message)

                            sendNotiftoRecepients(chat!!, message)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
        else{ //Other messages
            screenshot = false
            messages!!.senderid = mAuth.uid.toString()
            messages!!.time = Calendar.getInstance().time.toString()
            message = messages!!
            messages = null
            uploadimgbt!!.background = ContextCompat.getDrawable(this, R.drawable.gallery_grey)

            val storageref = FirebaseStorage.getInstance().reference

            storageref.child("Chats").child(chat!!.id + Random.nextInt(0,100000).toString()).putFile(messageimg!!).addOnSuccessListener {
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {task ->
                    message.content = task.toString()
                    Log.w("TAG", "Upload Success")

                    FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(User::class.java)
                                if (user != null) {
                                    Log.w("TAG", "in user, getting url")
                                    message.senderpic = user.profilepic.toString()

                                    message.id = FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").push().key.toString()
                                    FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id).setValue(message)
                                    sendNotiftoRecepients(chat!!, message)
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }.addOnFailureListener{
                Log.w("TAG", "Upload failed")
            }
        }
        Log.w("Message uRI", message.content)
    }

    private val screenshotObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?, userId: Int) {
            super.onChange(selfChange, uri, userId)
            if (!screenshot) {
                screenshot = true
                Log.d("ContentObserver", "Calling uploadmessage()")
                uploadmessage()
            }
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            audioFile = File.createTempFile("audio", ".mp3", externalCacheDir)
            setOutputFile(audioFile!!.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("TAG", "startRecording: ", e)
            }
        }
    }
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
    private fun uploadAudio() {
        val storageRef = FirebaseStorage.getInstance().reference.child("Chats").child("voice").child(chat!!.id + Random.nextInt(0,100000).toString())
        val uploadTask = audioFile?.let { storageRef.putFile(Uri.fromFile(it)) }

        uploadTask?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val message = Messages().apply {
                    content = uri.toString()
                    senderid = mAuth.uid.toString()
                    time = Calendar.getInstance().time.toString()
                    tag = "audio"
                    id = FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").push().key.toString()
                }
                FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id).setValue(message)
            }
        }?.addOnFailureListener {
            Log.e("TAG", "uploadAudio: ", it)
        }
    }
    private fun showOptionsDialog(message: Messages) {
        val options = arrayOf("Delete Message", "Edit Message")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Options")
        builder.setItems(options) { dialogInterface: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                    val messageTime: Date = originalFormat.parse(message.time)!!
                    val currentTime = Calendar.getInstance().time

                    val diff = currentTime.time - messageTime.time

                    if (diff < 300000) {
                        val ref = FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id)
                        ref.removeValue()
                    } else {
                        Toast.makeText(this, "You can only delete messages sent within the last 5 minutes", Toast.LENGTH_SHORT).show()
                    }

                }
                1 -> {
                    if (message.tag == "text"){
                        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                        val messageTime: Date = originalFormat.parse(message.time)!!
                        val currentTime = Calendar.getInstance().time

                        val diff = currentTime.time - messageTime.time

                        if (diff < 300000) {
                            var btsend: Button = findViewById(R.id.send_button)
                            btsend.isEnabled = false

                            val dialogView = LayoutInflater.from(this).inflate(R.layout.editmessage_layout, null)
                            val builder = AlertDialog.Builder(this).setView(dialogView)
                            val alertDialog = builder.show()

                            val editText = dialogView.findViewById<EditText>(R.id.dialog_edit_text)
                            editText.setText(message.content)

                            dialogView.findViewById<Button>(R.id.dialog_cancel_button).setOnClickListener {
                                alertDialog.dismiss()
                                btsend.isEnabled = true
                            }

                            dialogView.findViewById<Button>(R.id.dialog_done_button).setOnClickListener {
                                message.content = editText.text.toString()
                                FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id).setValue(message)
                                btsend.isEnabled = true
                                editText.setText("")
                                alertDialog.dismiss()
                            }
                        }
                        else{
                            Toast.makeText(this, "You can only edit messages sent within the last 5 minutes", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this, "You can only edit text messages", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.create().show()
    }
    private fun showOptionsDialogimage(message: Messages) {
        val options = arrayOf("Delete Message", "View Message")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Options")
        builder.setItems(options) { dialogInterface: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                    if (mAuth.uid.toString() == message.senderid){
                        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                        val messageTime: Date = originalFormat.parse(message.time)!!
                        val currentTime = Calendar.getInstance().time

                        val diff = currentTime.time - messageTime.time

                        if (diff < 300000) {
                            val ref = FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id)
                            ref.removeValue()
                        } else {
                            Toast.makeText(this, "You can only delete messages sent within the last 5 minutes", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this, "You can only delete your own messages", Toast.LENGTH_SHORT).show()
                    }
                }
                1 -> {
                    val dialogView = LayoutInflater.from(this).inflate(R.layout.image_fullscreen_viewer, null)
                    val builder = AlertDialog.Builder(this).setView(dialogView)
                    val alertDialog = builder.show()

                    val window = alertDialog.window

                    window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

                    val backbutton = dialogView.findViewById<Button>(R.id.back_button)
                    backbutton.setOnClickListener(View.OnClickListener {
                        alertDialog.dismiss()
                    })

                    val imageView = dialogView.findViewById<ImageView>(R.id.image_displayer)
                    Glide.with(this)
                        .load(message.content)
                        .into(imageView)
                }
            }
        }
        builder.create().show()
    }

    var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission(),) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            askNotificationPermission()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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

    fun sendNotiftoRecepients(chats: Chats, message: Messages){
        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        var usr : User = data.getValue(User::class.java)!!
                        FirebaseDatabase.getInstance().getReference("User").child(usr.id).child("Chats").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (data in snapshot.children) {
                                        val chats = data.getValue(String::class.java)
                                        if (chats != null) {
                                            if (chats == chat!!.id && usr.id != mAuth.uid.toString()) {
                                                sendPushNotification(usr.token, "New Message " + username, "New Message from ", message.content, mapOf("chatid" to chat!!.id))
                                            }
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })    }

}