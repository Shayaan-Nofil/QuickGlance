package com.m_abdullah.quickglance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide

class Send_snaps : AppCompatActivity() {
    private var imageUri: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_snaps)

        val imageviewer = findViewById<ImageView>(R.id.image_viewer)
        imageUri = intent.getStringExtra("imageUri").toString()
        Glide.with(this@Send_snaps)
            .load(imageUri)
            .centerCrop()
            .into(imageviewer)


        findViewById<Button>(R.id.back_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            finish()
        }
    }
}