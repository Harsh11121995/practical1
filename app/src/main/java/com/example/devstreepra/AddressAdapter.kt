package com.example.devstreepra

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.devstreepra.RoomDb.Task
import com.example.devstreepra.databinding.AddressLayoutBinding

class AddressAdapter internal constructor(
    private val onItemClickListener: OnItemClickListenerData,
    private val items: ArrayList<Task>,
    val context: Activity,
) : RecyclerView.Adapter<AddressAdapter.MyViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) =
        MyViewHolder(
            AddressLayoutBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.binding.txtName.text = items[position].name
        holder.binding.txtAddress.text = items[position].address


        holder.binding.txtDelete.setOnClickListener {
            onItemClickListener.onItemClick(it,position)
        }

        holder.binding.txtEdit.setOnClickListener {
            onItemClickListener.onItemClick(it,position)
        }

    }

    inner class MyViewHolder(val binding: AddressLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


}