package com.example.devstreepra


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devstreepra.RoomDb.DatabaseClient
import com.example.devstreepra.RoomDb.Task
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MainActivity : AppCompatActivity(), OnItemClickListenerData {


    var autoCompleteTextView: AutoCompleteTextView? = null
    var mAdapterList: AddressAdapter? = null
    lateinit var adapter: AutoCompleteAdapter
    lateinit var responseView: TextView
    var rcvData: RecyclerView? = null
    var add_person_fab: FloatingActionButton? = null
    var placesClient: PlacesClient? = null

    companion object{
        val taskListArray = ArrayList<Task>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        responseView = findViewById(R.id.response)
        rcvData = findViewById(R.id.rcvData)
        add_person_fab = findViewById(R.id.add_person_fab)

        add_person_fab!!.setOnClickListener {
            val intent = Intent(this,RouteMapsActivity::class.java)
            startActivity(intent)
        }


        val apiKey = getString(R.string.api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        placesClient = Places.createClient(this)
        initAutoCompleteTextView()

        rcvData!!.layoutManager = LinearLayoutManager(this)

        getData()


    }


    private fun initAutoCompleteTextView() {
        autoCompleteTextView = findViewById<View>(R.id.auto) as AutoCompleteTextView
        autoCompleteTextView?.threshold = 1
        autoCompleteTextView?.onItemClickListener = autocompleteClickListener
        adapter = AutoCompleteAdapter(this, placesClient)
        autoCompleteTextView?.setAdapter(adapter)
    }

    private val autocompleteClickListener =
        OnItemClickListener { adapterView, view, i, l ->
            try {
                val item: AutocompletePrediction = adapter.getItem(i)!!
                var placeID: String? = null
                if (item != null) {
                    placeID = item.placeId
                }

                //                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
                //                Use only those fields which are required.
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
                            responseView.text = """
                                                    ${task.place.name}
                                                    ${task.place.address}
                                                    """.trimIndent()

                            val intent = Intent(this, MapsActivity::class.java)
                            intent.putExtra("latitude", task.place.latLng.latitude.toString())
                            intent.putExtra("longitude", task.place.latLng.longitude.toString())
                            intent.putExtra("name", task.place.name)
                            intent.putExtra("address", task.place.address)
                            startActivity(intent)
                        }.addOnFailureListener(
                            OnFailureListener { e ->
                                e.printStackTrace()
                                responseView.text = e.message
                            })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    fun getData() {
        Thread {
            val taskList = DatabaseClient
                .getInstance(applicationContext)
                .appDatabase
                .taskDao()
                .all

            Log.e("taskList==>", taskList[0].address)
            taskListArray.clear()
            taskListArray.addAll(taskList)
        }.start()
        val mAdapterList = AddressAdapter(this, taskListArray, this)
        rcvData!!.adapter = mAdapterList

    }


    override fun onItemClick(view: View?, position: Int) {

        if (view!!.id == R.id.txtDelete){
            Thread {
                val task = taskListArray[position]
                DatabaseClient.getInstance(applicationContext).appDatabase
                    .taskDao()
                    .delete(task)
            }.start()

            getData()
        } else if (view!!.id == R.id.txtEdit){
            val task = taskListArray[position]
            val intent = Intent(this,MapsActivity::class.java)
            intent.putExtra("task", task)
            startActivity(intent)
        }

    }
}