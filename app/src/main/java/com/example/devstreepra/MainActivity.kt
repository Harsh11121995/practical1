package com.example.devstreepra


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devstreepra.RoomDb.DatabaseClient
import com.example.devstreepra.RoomDb.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity(), OnItemClickListenerData {

    private lateinit var adapter: AutoCompleteAdapter
    private lateinit var responseView: TextView
    private var rcvData: RecyclerView? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var btnAddPoi: Button? = null
    private var placesClient: PlacesClient? = null

    companion object {
        val taskListArray = ArrayList<Task>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        responseView = findViewById(R.id.response)
        rcvData = findViewById(R.id.rcvData)
        floatingActionButton = findViewById(R.id.add_person_fab)
        btnAddPoi = findViewById(R.id.btnAddPoi)

        floatingActionButton!!.setOnClickListener {
            val intent = Intent(this, RouteMapsActivity::class.java)
            startActivity(intent)
        }

        btnAddPoi!!.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }


        val apiKey = getString(R.string.api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        placesClient = Places.createClient(this)
        initAutoCompleteTextView()
        rcvData!!.layoutManager = LinearLayoutManager(this)

    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun initAutoCompleteTextView() {
        adapter = AutoCompleteAdapter(this, placesClient)
    }

    private fun getData() {
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
        if (view!!.id == R.id.txtDelete) {
            Thread {
                val task = taskListArray[position]
                DatabaseClient.getInstance(applicationContext).appDatabase
                    .taskDao()
                    .delete(task)
            }.start()

            getData()
        } else if (view.id == R.id.txtEdit) {
            val task = taskListArray[position]
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("task", task)
            startActivity(intent)
        }

    }
}