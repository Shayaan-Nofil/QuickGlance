package com.m_abdullah.quickglance

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
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
    private var usingflash = false
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var isrec: Boolean = false
    private lateinit var cameraProvider: ProcessCameraProvider // Add this line
    private lateinit var cameraSelector: CameraSelector // Add this line
    private lateinit var preview: Preview // Add this line
    private var timer: CountDownTimer? = null
    private var secondsElapsed: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val sharedPref = getSharedPreferences("CameraPrefs", MODE_PRIVATE)
        usingFrontCamera = sharedPref.getBoolean("usingFrontCamera", false)

        if (allPermissionsGranted()) {
            val sharedPref = getSharedPreferences("CameraPrefs", MODE_PRIVATE)
            usingFrontCamera = sharedPref.getBoolean("usingFrontCamera", false)

            cameraSelector = if (usingFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera(cameraSelector)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        val videoduration: TextView = findViewById(R.id.video_duration)
        videoduration.visibility = View.GONE

        // Set up the listener for take photo button
        val cameraCaptureButton: Button = findViewById(R.id.shutter_button)
        cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        val handler = Handler()
        val longPressRunnable = Runnable {
            if (!isrec) {
                isrec = true
                startRecording()
                cameraCaptureButton.setBackgroundResource(R.drawable.recording_icon_red)
                cameraCaptureButton.scaleX = 1.3F
                cameraCaptureButton.scaleY = 1.3F
            }
        }

        cameraCaptureButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)
                    if (isrec) {
                        isrec = false
                        stopRecording()
                        cameraCaptureButton.setBackgroundResource(R.drawable.shutter)
                        cameraCaptureButton.scaleX = 1.0F
                        cameraCaptureButton.scaleY = 1.0F
                    } else {
                        takePhoto()
                    }
                }
            }
            true
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        val rootLayout = findViewById<PreviewView>(R.id.camera_preview)
        val doubletap = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                togglecamera()
                return super.onDoubleTap(e)
            }
        })

        val swiperight = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.x - e1!!.x
                if (diffX > 100) {
                    val intent = Intent(this@Camera_Activity, chatspage_Activity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left,R.anim.fade_out)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        val swipeleft = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.x - e1!!.x
                if (diffX < -100) {
                    val intent = Intent(this@Camera_Activity, Stories_Activity::class.java)
                    startActivity(intent)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        val swipeup = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.y - e1!!.y
                if (diffX < -300) {
                    val intent = Intent(this@Camera_Activity, Memories_Activity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_bottom,R.anim.fade_out)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        val swipedown = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.y - e1!!.y
                if (diffX > 300) {
                    val intent = Intent(this@Camera_Activity, Profile_Activity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_top, R.anim.fade_out)
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        //controls swipe gestures
        rootLayout.setOnTouchListener { _, event ->
            doubletap.onTouchEvent(event)
            swiperight.onTouchEvent(event)
            swipeleft.onTouchEvent(event)
            swipeup.onTouchEvent(event)
            swipedown.onTouchEvent(event)
            true
        }

        findViewById<Button>(R.id.flipcamera_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            togglecamera()
        }

        findViewById<Button>(R.id.flash_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            toggleflash()
        }

        findViewById<Button>(R.id.profile_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Profile_Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_top, R.anim.fade_out)
        }

        findViewById<Button>(R.id.addfriend_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Add_friends_activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_top, R.anim.fade_out)
        }
        findViewById<Button>(R.id.search_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Search_Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_top, R.anim.fade_out)
        }
        findViewById<Button>(R.id.chats_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, chatspage_Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.fade_out)
        }
        findViewById<Button>(R.id.Stories_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            val intent = Intent(this, Stories_Activity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.memories_button).setOnClickListener{
            val intent = Intent(this, Memories_Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom,R.anim.fade_out)
        }
    }
    private fun togglecamera(){
        Log.w("TAG", "switching camera")
        usingFrontCamera = !usingFrontCamera
        if (usingFrontCamera) {
            findViewById<Button>(R.id.flash_button).setBackgroundResource(R.drawable.flash_off)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
        else {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera(cameraSelector)

        val sharedPref = getSharedPreferences("CameraPrefs", MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean("usingFrontCamera", usingFrontCamera)
            apply()
        }
    }
    private fun toggleflash(){
        val cameraControl = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture).cameraControl
        val flashbutton = findViewById<Button>(R.id.flash_button)

        if (flashMode == ImageCapture.FLASH_MODE_OFF && !usingFrontCamera) {
            flashMode = ImageCapture.FLASH_MODE_ON
            flashbutton.setBackgroundResource(R.drawable.flash)
            cameraControl.enableTorch(true)
        } else {
            flashMode = ImageCapture.FLASH_MODE_OFF
            flashbutton.setBackgroundResource(R.drawable.flash_off)
            cameraControl.enableTorch(false)
        }

        imageCapture?.flashMode = flashMode
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.camera_preview).surfaceProvider)
                }

            // Initialize ImageCapture use case
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // Set aspect ratio to 9:16
                .build()

            // Initialize VideoCapture use case
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture
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


                    // Upload the image to Firestore
                    val intent = Intent(this@Camera_Activity, Send_snaps::class.java)
                    intent.putExtra("imageUri", savedUri.toString())
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, 0)
                }
            })
    }

    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return

        isrec = true
        secondsElapsed = 0
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed++
                val minutes = secondsElapsed / 60
                val seconds = secondsElapsed % 60
                val videoduration: TextView = findViewById(R.id.video_duration)
                videoduration.text = String.format("%02d:%02d", minutes, seconds)
                videoduration.visibility = View.VISIBLE
            }

            override fun onFinish() {}
        }.start()


        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@Camera_Activity,
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"

                            val intent = Intent(this@Camera_Activity, Send_snaps::class.java)
                            Log.w("TAG", recordEvent.outputResults.outputUri.toString())
                            intent.putExtra("videoUri", recordEvent.outputResults.outputUri.toString())
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, 0)

                            // Call uploadVideoToFirestore function here
                            //uploadVideoToFirestore(recordEvent.outputResults.outputUri)

                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            isrec = false
        }

        // Stop timer
        timer?.cancel()
        val videoduration: TextView = findViewById(R.id.video_duration)
        videoduration.visibility = View.GONE
    }

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

    override fun onPause() {
        super.onPause()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        startCamera(cameraSelector)
        findViewById<Button>(R.id.flash_button).setBackgroundResource(R.drawable.flash_off)
        flashMode = ImageCapture.FLASH_MODE_OFF
    }

    companion object {
        private const val TAG = "camera_picture_mode"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}