package com.example.devstreepra

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.devstreepra.RoomDb.DatabaseClient
import com.example.devstreepra.RoomDb.Task
import com.example.devstreepra.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    var autoCompleteTextView: AutoCompleteTextView? = null
    lateinit var adapter: AutoCompleteAdapter
    lateinit var responseView: TextView
    var placesClient: PlacesClient? = null

    var name = ""
    var address = ""
    var latitude = ""
    var longitude = ""

    private var isEdit: Boolean = false
    var savedTask: Task? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiKey = getString(R.string.api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        placesClient = Places.createClient(this)
        initAutoCompleteTextView()


        if (intent.hasExtra("task")) {
            isEdit = true
            savedTask = intent.getSerializableExtra("task") as Task?

            latitude = savedTask!!.latitude
            longitude = savedTask!!.longitude

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)


        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(latitude.toDouble(), longitude.toDouble())
        mMap.addMarker(MarkerOptions().position(sydney))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


    }

    fun showDialog() {
        val builder1: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder1.setMessage("Save Your Data")
        builder1.setCancelable(false)

        if (isEdit) {
            builder1.setPositiveButton(
                "Update"
            ) { dialog, id ->
                updateData()
                dialog.dismiss()
            }
        } else {
            builder1.setPositiveButton(
                "Save"
            ) { dialog, id ->
                saveData()
                dialog.dismiss()
            }
        }


        builder1.setNegativeButton(
            "No",
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        val alert11: android.app.AlertDialog? = builder1.create()
        alert11!!.show()
    }

    fun saveData() {
        Thread {
            //creating a task
            val task = Task()
            task.name = name
            task.address = address
            task.latitude = latitude
            task.longitude = longitude

            //adding to database

            //adding to database
            DatabaseClient.getInstance(applicationContext).getAppDatabase()
                .taskDao()
                .insert(task)

        }.start()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun updateData() {

        Thread {

            savedTask!!.name = name
            savedTask!!.address = address
            savedTask!!.latitude = latitude
            savedTask!!.longitude = longitude

            DatabaseClient.getInstance(applicationContext).appDatabase
                .taskDao()
                .update(savedTask!!)
        }.start()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }

    private fun initAutoCompleteTextView() {
        autoCompleteTextView = findViewById<View>(R.id.auto) as AutoCompleteTextView
        autoCompleteTextView?.setThreshold(1)
        autoCompleteTextView?.setOnItemClickListener(autocompleteClickListener)
        adapter = AutoCompleteAdapter(this, placesClient)
        autoCompleteTextView?.setAdapter(adapter)
    }

    private val autocompleteClickListener =
        AdapterView.OnItemClickListener { adapterView, view, i, l ->
            try {
                val item: AutocompletePrediction = adapter.getItem(i)!!
                var placeID: String? = null
                if (item != null) {
                    placeID = item.placeId
                }

                //                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
                //                Use only those fields which are required.
                val placeFields: List<Place.Field> = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
                var request: FetchPlaceRequest? = null
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                        .build()
                }
                if (request != null) {
                    placesClient!!.fetchPlace(request)
                        .addOnSuccessListener { task ->

                            name = task.place.name
                            address = task.place.address
                            latitude = task.place.latLng.latitude.toString()
                            longitude = task.place.latLng.longitude.toString()


                            val mapFragment = supportFragmentManager
                                .findFragmentById(R.id.map) as SupportMapFragment
                            mapFragment.getMapAsync(this)

                            showDialog()
                        }.addOnFailureListener(
                            OnFailureListener { e ->
                                e.printStackTrace()
                                responseView.setText(e.message)
                            })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}