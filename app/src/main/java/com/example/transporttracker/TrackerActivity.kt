package com.example.transporttracker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_tracker.*

class TrackerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        firebaseAuth = FirebaseAuth.getInstance()

        checkInternetStatus()
        checkGPSStatus()
        createCarList()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menus, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.logOut -> {
            // User chose the "Settings" item, show the app settings UI...
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            true

        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }



    private fun createCarList(){
        val carList = findViewById<Spinner>(R.id.carList)

        ArrayAdapter.createFromResource(this, R.array.carList, android.R.layout.simple_list_item_1)
            .also { arrayAdapter ->
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
                carList.adapter = arrayAdapter
            }
        carList.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        val carId = parent.getItemAtPosition(pos) as String
        if(pos > 0){
            Toast.makeText(this, "You selected $carId", Toast.LENGTH_SHORT).show()
//            showLocationUpdates(carId)
            val trackInMap = Intent(this, MapsActivity::class.java)
            trackInMap.putExtra("carId", carId)
            startActivity(trackInMap)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
        Toast.makeText(this, "Please, Select a Car by It's ID to track it!", Toast.LENGTH_SHORT).show()
    }





    private fun checkGPSStatus(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alertMessage()
        }
    }

    private fun alertMessage(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This Application need your GPS to be Enabled. Enable GPS?").setCancelable(false)

        builder.setPositiveButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_gps_fixed_green_24dp))
        builder.setPositiveButton("Enable GPS?"){dialog, which ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        builder.setNegativeButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_close_red_24dp))
        builder.setNegativeButton("Cancel "){dialog, which ->
            dialog.cancel()
        }
        val dialog = builder.create()
        dialog.show()
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

}
