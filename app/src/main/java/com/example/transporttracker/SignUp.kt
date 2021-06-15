package com.example.transporttracker

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var textEmail: EditText
    private lateinit var textPassword: EditText
    private lateinit var signUP: Button
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var signUpProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        checkInternetStatus()
        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser != null){
            startActivity(Intent(this, TrackerActivity::class.java))
            finish()
        }

        initialize()
        signUP.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                checkInternetStatus()
                createAccount()
            }
        })
    }

    private fun createAccount(){
        email = textEmail.text.toString()
        password = textPassword.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter an email address", Toast.LENGTH_SHORT).show()
            return
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
            return
        }

        signUpProgressBar.visibility = View.VISIBLE
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    firebaseAuth.signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Account creation failed.", Toast.LENGTH_SHORT).show()
                }
                signUpProgressBar.visibility = View.INVISIBLE
            }
    }

    private fun checkInternetStatus(){
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        if(activeNetwork?.isConnected == null){
            alertForInternet()
        }
    }

    private fun alertForInternet(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This Application needs the Internet to be Enabled. Enable Wi-Fi or Mobile Data?").setCancelable(true)

        builder.setPositiveButton("Data"){dialog, which ->
            if(android.os.Build.VERSION.SDK_INT < 28){
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            } else {
                startActivity(Intent(Settings.ACTION_DATA_USAGE_SETTINGS))
            }
        }
        builder.setPositiveButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_data_usage_blue_16dp))



        builder.setNeutralButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_close_red_24dp))
        builder.setNeutralButton("Cancel"){ dialog, which ->
            dialog.cancel()
        }

        builder.setNegativeButton("Wi-Fi"){dialog, which ->
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        builder.setNegativeButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_wifi_green_16dp))

        val dialog = builder.create()
        dialog.show()
    }

    private fun initialize(){
        textEmail = findViewById(R.id.signUpEmail)
        textPassword = findViewById(R.id.signUpPassword)
        signUP = findViewById(R.id.signUP)
        signUpProgressBar = findViewById<ProgressBar>(R.id.signUpProgressBar)
    }
}
