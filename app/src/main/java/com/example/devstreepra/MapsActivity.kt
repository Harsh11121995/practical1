package com.example.devstreepra

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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var autoCompleteTextView: AutoCompleteTextView? = null
    private lateinit var adapter: AutoCompleteAdapter
    private lateinit var responseView: TextView
    private var placesClient: PlacesClient? = null

    private var name = ""
    private var address = ""
    private var latitude = ""
    private var longitude = ""

    private var isEdit: Boolean = false
    private var savedTask: Task? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiKey = getString(R.string.api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
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
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sydney = LatLng(latitude.toDouble(), longitude.toDouble())
        mMap.addMarker(MarkerOptions().position(sydney))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


    }

    private fun showDialog() {
        val builder1: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder1.setMessage("Save Your Data")
        builder1.setCancelable(false)

        if (isEdit) {
            builder1.setPositiveButton(
                "Update"
            ) { dialog, _ ->
                updateData()
                dialog.dismiss()
            }
        } else {
            builder1.setPositiveButton(
                "Save"
            ) { dialog, _ ->
                saveData()
                dialog.dismiss()
            }
        }


        builder1.setNegativeButton(
            "No"
        ) { dialog, _ -> dialog.cancel() }

        val alert11: android.app.AlertDialog? = builder1.create()
        alert11!!.show()
    }

    private fun saveData() {
        Thread {
            //creating a task
            val task = Task()
            task.name = name
            task.address = address
            task.latitude = latitude
            task.longitude = longitude


            //adding to database
            DatabaseClient.getInstance(applicationContext).appDatabase
                .taskDao()
                .insert(task)

        }.start()

        finish()
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
        finish()

    }

    private fun initAutoCompleteTextView() {
        autoCompleteTextView = findViewById<View>(R.id.auto) as AutoCompleteTextView
        autoCompleteTextView?.threshold = 1
        autoCompleteTextView?.onItemClickListener = autocompleteClickListener
        adapter = AutoCompleteAdapter(this, placesClient)
        autoCompleteTextView?.setAdapter(adapter)
    }

    private val autocompleteClickListener =
        AdapterView.OnItemClickListener { _, _, i, _ ->
            try {
                val item: AutocompletePrediction = adapter.getItem(i)!!
                val placeID: String? = item.placeId

                val placeFields: List<Place.Field> = listOf(
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

                            name = task.place.name as String
                            address = task.place.address as String
                            latitude = task.place.latLng?.latitude.toString()
                            longitude = task.place.latLng?.longitude.toString()


                            val mapFragment = supportFragmentManager
                                .findFragmentById(R.id.map) as SupportMapFragment
                            mapFragment.getMapAsync(this)

                            showDialog()
                        }.addOnFailureListener { e ->
                            e.printStackTrace()
                            responseView.text = e.message
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}