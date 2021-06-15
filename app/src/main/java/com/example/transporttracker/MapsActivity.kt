	package com.example.transporttracker

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Property
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.ref.Reference
import kotlin.collections.HashMap

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var childEventListener: ChildEventListener
    private lateinit var carId: String

    private lateinit var mMap: GoogleMap
    private val myPermissionRequestLocation:Int = 1

    private lateinit var showLoc: LatLng

    private val TAG = MapsActivity::class.java.simpleName
    private val mMarker = hashMapOf<String, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        checkInternetStatus()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkGPSStatus()
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null){
            startActivity(Intent(this, MainActivity::class.java))
        }
        carId = intent.getStringExtra("carId")
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        checkStatusAndCurrentLocation()

        mMap.setMaxZoomPreference(17.5f)
        mMap.maxZoomLevel
        showLocationUpdates(carId)
    }




    private fun setMarker(dataSnapshot: DataSnapshot) {
        val key = dataSnapshot.key
        val locationValue = dataSnapshot.value as HashMap<String, Object>
        val lat = locationValue["latitude"].toString().toDouble()
        val lng = locationValue["longitude"].toString().toDouble()

        val location = LatLng(lat, lng)
        if(!mMarker.containsKey(key)){
            mMarker.put(key!!, mMap.addMarker(MarkerOptions()
                                                .title(key)
                                                .position(location)
                                                .anchor(0.5f,0.5f)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                            )
                        )
        } else {
            mMarker.get(key)?.setPosition(location)
        }
        val builder = LatLngBounds.Builder()
        for ((marker , value) in mMarker){
            val v = value.position
            builder.include(v)
            showLoc = v
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 1))

        val showLocation = findViewById<TextView>(R.id.showCurrentLoc)
        showLocation.text = showLoc.toString()
    }


    private fun checkStatusAndCurrentLocation(){

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "Location permission is needed for this app, Please, enable!", Toast.LENGTH_LONG).show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), myPermissionRequestLocation)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mMap.isMyLocationEnabled = true

        }
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermissionRequestLocation -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    //  task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
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


    private fun showLocationUpdates(currentCarId: String) {
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseReference = firebaseDatabase.getReference("Car/Track/$currentCarId/Location/")

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                setMarker(p0)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                setMarker(p0)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Failed to read value!")
            }
        }
        firebaseReference.addChildEventListener(childEventListener)
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
