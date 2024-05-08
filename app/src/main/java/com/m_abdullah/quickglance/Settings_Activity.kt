package com.m_abdullah.quickglance

import User
import android.app.ActivityManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.random.Random

private lateinit var mAuth: FirebaseAuth
class Settings_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        mAuth = Firebase.auth
        val nametext = findViewById<TextView>(R.id.name)
        val usernametext = findViewById<TextView>(R.id.username)
        val emailtext = findViewById<TextView>(R.id.email)
        val birthdaytext = findViewById<TextView>(R.id.birthday)
        val numbertext = findViewById<TextView>(R.id.number)

        FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var usr = snapshot.getValue(User::class.java)!!
                    nametext.text = usr.name
                    usernametext.text = usr.username
                    emailtext.text = usr.email
                    birthdaytext.text = usr.birthday
                    numbertext.text = usr.number
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        nametext.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Change Name")
            val input = EditText(this)
            input.setHint(nametext.text)
            builder.setView(input)
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("name").setValue(input.text.toString())
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()
        })

        birthdaytext.setOnClickListener(View.OnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in TextView
                val date = GregorianCalendar(year, monthOfYear, dayOfMonth).time
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = format.format(date)
                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("birthday").setValue(dateString)
            }, year, month, day)

            dpd.show()
        })

        numbertext.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Change Number")
            val input = EditText(this)
            input.setHint(numbertext.text)
            builder.setView(input)
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                if (input.length() < 10){
                    val text = "Please enter a valid number"
                    val duration = android.widget.Toast.LENGTH_SHORT
                    val toast = android.widget.Toast.makeText(this, text, duration) // in Activity
                    toast.show()
                    return@OnClickListener
                }
                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).child("number").setValue(input.text.toString())
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()
        })

        findViewById<Button>(R.id.back_arrow).setOnClickListener(){
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val taskInfo = am.getRunningTasks(1)[0]
            if (taskInfo.numActivities == 1) {
                // The current activity is the only activity in the stack, start Camera activity
                val intent = Intent(this, Camera_Activity::class.java)
                startActivity(intent)
            } else {
                // There are more activities in the stack, finish the current activity
                finish()
            }
        }

        findViewById<Button>(R.id.sign_out_button).setOnClickListener(){
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Login_Activity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}