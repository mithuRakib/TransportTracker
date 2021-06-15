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

class ResetPassword : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var resetPasswordEmail: EditText
    private lateinit var resetPassword: Button
    private lateinit var resetPasswordProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        Initialize()
        firebaseAuth = FirebaseAuth.getInstance()
        resetPassword.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                checkInternetStatus()
                resetPassword()
            }
        })
    }


    private fun resetPassword(){
        val email = resetPasswordEmail.text.toString()
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter an email address", Toast.LENGTH_SHORT).show()
            return
        }
        resetPasswordProgressBar.visibility = View.VISIBLE
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if(task.isSuccessful){
                resetPasswordProgressBar.visibility = View.INVISIBLE
                resetPasswordMailSent()
            } else {
                Toast.makeText(this, "Failed to send password reset link.", Toast.LENGTH_SHORT).show()

            }
        }
    }


    fun goBackToLogin(view: View){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun Initialize(){
        resetPasswordEmail = findViewById(R.id.resetPasswordEmail)
        resetPassword = findViewById(R.id.resetPassword)
        resetPasswordProgressBar = findViewById(R.id.resetPasswordProgressBar)
    }

    private fun checkInternetStatus(){
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        if(activeNetwork?.isConnected == null){
            alertForInternet()
        }
    }

    private fun resetPasswordMailSent(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("An Email was sent to your Email address with the instruction to reset your password." +
                "\nPlease, check your Email and follow the instructions!").setCancelable(false)

        builder.setPositiveButton("Ok"){dialog, which ->
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        builder.setPositiveButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_done_green_24dp))


        val dialog = builder.create()
        dialog.show()
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
}
