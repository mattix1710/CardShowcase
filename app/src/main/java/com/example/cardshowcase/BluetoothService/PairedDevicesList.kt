package com.example.cardshowcase.BluetoothService

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.cardshowcase.R

class PairedDevicesList(var context: Context, var deviceList: Array<String?>) :
    BaseAdapter() {
    var inflater: LayoutInflater
    override fun getCount(): Int {
        return deviceList.size
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        convertView = inflater.inflate(R.layout.paired_devices_list, null)
        val name = convertView.findViewById<View>(R.id.name) as TextView
        name.text = deviceList[position]
        return convertView
    }

    init {
        inflater = LayoutInflater.from(context)
    }
}