package com.m_abdullah.quickglance

import User
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

private lateinit var mAuth: FirebaseAuth
private lateinit var database: DatabaseReference
class Registration_Activity : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var username: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_page)

        mAuth = Firebase.auth
        val loginbutton=findViewById<View>(R.id.LogIn)
        loginbutton.setOnClickListener(View.OnClickListener {
            val login = Intent(this, Login_Activity::class.java )
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(login)
            finish()
        })

        val signupbutton=findViewById<View>(R.id.SignUp)
        signupbutton.setOnClickListener(View.OnClickListener {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))

            username = findViewById(R.id.Username)
            email = findViewById(R.id.Email)
            password = findViewById(R.id.Password)

            if (email.text.isNotEmpty() && password.text.length >= 6 && username.text.isNotEmpty()){
                Log.w("TAG", email.text.toString())
                signup(email.text.toString(), password.text.toString())
            }else{
                val text = "Please fill all the fields"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this, text, duration) // in Activity
                toast.show()
            }
        })
    }

    fun signup(email:String,pass:String){
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var usr = User()
                    var userId = mAuth.uid;
                    usr.username = username.text.toString()
                    usr.email = email

                    database = FirebaseDatabase.getInstance().getReference("User")
                    usr.id = mAuth.uid.toString()
                    database.child(userId!!).setValue(usr).addOnCompleteListener {

                        FirebaseApp.initializeApp(this)
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                                    return@OnCompleteListener
                                }
                                // Get new FCM registration token
                                val token = task.result
                                Log.d("MyToken", token)

                                FirebaseDatabase.getInstance().getReference("User").child(mAuth.uid.toString()).addListenerForSingleValueEvent(object:
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            Log.w("TAG", "User is a user")
                                            var usr : User = snapshot.getValue(User::class.java)!!
                                            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                                                if (!task.isSuccessful) {
                                                    Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                                                    return@OnCompleteListener
                                                }
                                                // Get new FCM registration token
                                                val token = task.result
                                                Log.d("MyToken", token)
                                                usr.token = token
                                                FirebaseDatabase.getInstance().getReference("User").child(Firebase.auth.uid.toString()).setValue(usr)
                                            })

                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            })

                        var secondActivityIntent = Intent(this, Settings_Activity::class.java)
                        startActivity(secondActivityIntent)
                        finish()
                    }.addOnFailureListener{
                        Log.w("TAG", "Didnt Register", task.exception)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    val text = "Didnt work"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this, text, duration) // in Activity
                    toast.show()
                }
            }
    }
}