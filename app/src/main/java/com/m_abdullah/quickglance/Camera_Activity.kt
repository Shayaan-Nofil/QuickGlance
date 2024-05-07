package com.m_abdullah.quickglance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera_Activity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    private var imageCapture: ImageCapture? = null
    private var usingFrontCamera = false
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (allPermissionsGranted()) {
            val sharedPref = getSharedPreferences("CameraPrefs", MODE_PRIVATE)
            usingFrontCamera = sharedPref.getBoolean("usingFrontCamera", false)

            val cameraSelector = if (usingFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera(cameraSelector)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        val cameraCaptureButton: Button = findViewById(R.id.shutter_button)
        cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

//        var videobutton: Button = findViewById(androidx.camera.core.R.id.video_button)
//        videobutton.setOnClickListener{
//            val intent = Intent(this, camera_video_mode::class.java)
//            val bundle = Bundle()
//            bundle.putSerializable("chatdata", chat)
//            intent.putExtras(bundle)
//            intent.putExtra("chatdata", chat)
//            startActivity(intent)
//            finish()
//        }

        findViewById<Button>(R.id.flipcamera_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val sharedPref = getSharedPreferences("CameraPrefs", MODE_PRIVATE)

            usingFrontCamera = !usingFrontCamera
            val cameraSelector = if (usingFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera(cameraSelector)

            with (sharedPref.edit()) {
                putBoolean("usingFrontCamera", usingFrontCamera)
                apply()
            }
        }
        val flashbutton = findViewById<Button>(R.id.flash_button)
        flashbutton.setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            if (flashMode == ImageCapture.FLASH_MODE_OFF && !usingFrontCamera) {
                flashMode = ImageCapture.FLASH_MODE_ON
                flashbutton.setBackgroundResource(R.drawable.flash)
            } else {
                flashMode = ImageCapture.FLASH_MODE_OFF
                flashbutton.setBackgroundResource(R.drawable.flash_off)
            }

            imageCapture?.flashMode = flashMode
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
        findViewById<Button>(R.id.chats_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, chatspage_Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,0)
        }
        findViewById<Button>(R.id.Stories_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Stories_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.camera_preview).surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // Set aspect ratio to 9:16
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Upload the image to Firestore
                    val intent = Intent(this@Camera_Activity, Send_snaps::class.java)
                    intent.putExtra("imageUri", savedUri.toString())
                    startActivity(intent)
                    overridePendingTransition(R.anim.static_display, R.anim.slide_out_top)
                }
            })
    }

//    private fun uploadImageToFirestore(uri: Uri) {
//        Thread(Runnable {
//            var message = Messages()
//            var mAuth = Firebase.auth
//            message.senderid = mAuth.uid.toString()
//            message.time = Calendar.getInstance().time.toString()
//            message.tag = "image"
//
//            val storageref = FirebaseStorage.getInstance().reference
//
//            storageref.child("Chats").child(chat!!.id + Random.nextInt(0,100000).toString()).putFile(uri).addOnSuccessListener {
//                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener {task ->
//                    message.content = task.toString()
//
//                    FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object:
//                        ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (snapshot.exists()) {
//                                val user = snapshot.getValue(User::class.java)
//                                if (user != null) {
//                                    message.senderpic = user.profilepic
//
//                                    message.id = FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").push().key.toString()
//                                    FirebaseDatabase.getInstance().getReference("Chats").child(chat!!.id).child("Messages").child(message.id).setValue(message)
//                                    finish()
//                                }
//                            }
//                        }
//                        override fun onCancelled(error: DatabaseError) {}
//                    })
//                }
//            }.addOnFailureListener{
//                Log.w("TAG", "Upload failed")
//            }
//        }).start()
//
//        finish()
//    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    companion object {
        private const val TAG = "camera_picture_mode"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}