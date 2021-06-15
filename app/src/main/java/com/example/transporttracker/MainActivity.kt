package com.example.transporttracker

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var textEmail: EditText
    private lateinit var textPassword: EditText
    private lateinit var login: Button
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var loginProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        checkInternetStatus()


        // Initialize firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null ) {
            goToTrackerActivity()
            finish()
        }

        initialize()
        login.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                checkInternetStatus()
                emailSignIn()
            }
        })
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseAuth.currentUser
        if(currentUser != null){
            goToTrackerActivity()
        }
    }


    // Start of Email Password Sign In
    private fun emailSignIn(){
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

        loginProgressBar.visibility = View.VISIBLE
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val user = firebaseAuth.currentUser
                    if(user!=null){
                        loginProgressBar.visibility = View.INVISIBLE
                        goToTrackerActivity()
                        finish()
                    }
                }
                else {
                    Toast.makeText(this, "Email and Password must be wrong, Enter the correct Email and Password", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // email, password, regButton views initializer
    private fun initialize(){
        textEmail = findViewById(R.id.loginEmail)
        textPassword = findViewById(R.id.loginPassword)
        login = findViewById(R.id.login)
        loginProgressBar = findViewById(R.id.loginProgressBar)

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


    // Take to the tracker activity
    private fun goToTrackerActivity(){
        startActivity(Intent(this, TrackerActivity::class.java))
    }


    fun resetPassword(view: View){
        startActivity(Intent(this, ResetPassword::class.java))
    }

    fun createAnAccount(view: View){
        startActivity(Intent(this, SignUp::class.java))
    }

    private fun signOut(){
        firebaseAuth.signOut()
    }

}
