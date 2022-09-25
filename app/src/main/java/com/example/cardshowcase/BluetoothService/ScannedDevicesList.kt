package com.example.cardshowcase.BluetoothService

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.cardshowcase.R
import com.google.android.material.snackbar.Snackbar

class ScannedDevicesList(private val deviceSet: ArrayList<Device>?, var mContext: Context) :
    ArrayAdapter<Device?>(
        mContext, R.layout.scanned_devices_list, deviceSet as List<Device?>
    ), View.OnClickListener {
    // View lookup cache
    private class ViewHolder {
        var deviceName: TextView? = null
        var deviceMACAddr: TextView? = null
    }

    override fun onClick(v: View) {
        val position = v.tag as Int
        val `object`: Any? = getItem(position)
        val dataModel = `object` as Device?
        when (v.id) {
            R.id.device_name -> Snackbar.make(
                v,
                "MAC " + dataModel!!.addressMAC,
                Snackbar.LENGTH_LONG
            )
                .setAction("No action", null).show()
        }
    }

    private var lastPosition = -1
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        var convertView = convertView
        val dataModel = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder // view lookup cache stored in tag
        val result: View?
        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.scanned_devices_list, parent, false)
            viewHolder.deviceName = convertView.findViewById<View>(R.id.device_name) as TextView
            viewHolder.deviceMACAddr =
                convertView.findViewById<View>(R.id.device_MAC_addr) as TextView
            result = convertView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            result = convertView
        }
        lastPosition = position
        viewHolder.deviceName!!.text = dataModel!!.name
        viewHolder.deviceMACAddr!!.text = dataModel.addressMAC
        // Return the completed view to render on screen
        return convertView!!
    }
}