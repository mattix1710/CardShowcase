package com.example.cardshowcase.BluetoothService

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cardshowcase.MainActivity
import com.example.cardshowcase.R
import com.google.android.material.snackbar.Snackbar


class BluetoothActivity : AppCompatActivity() {
    //declaration UI components
    lateinit var bluetoothAdapter //Declaration bluetoothAdapter which is required for any all Bluetooth activity. BluetoothAdapter represents the device's own Bluetooth adapter
            : BluetoothAdapter
    var pairedDevicesList: ListView? = null
    var scanDevicesList: ListView? = null
    var turnBluetooth: Switch? = null
    var scanDevicesBtn //Button turn on scan mode
            : Button? = null
    var editNameBtn //ImageButton to edit device name for Bluetooth
            : ImageButton? = null
    var availableDevicesTxt: TextView? = null
    var deviceNameTxt1: TextView? = null
    var deviceNameTxt2: TextView? = null
    var actualDeviceNameTxt: TextView? = null
    var MACAddressTxt: TextView? = null
    var pairedDeviceTxt: TextView? = null

    //Dialog components
    var editName_dialog: EditText? = null
    var save_Btn: Button? = null
    var cancel_Btn: Button? = null

    //variables
    var dataModels: ArrayList<Device>? = null

    @RequiresApi(api = Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetooth_activity)

        // WARNING!!!!!!!!!!! IMPORTANT!!!!!!!!!!!!!!!!!
        //If while click edittext the listView change position
        //If we wanna fix problem we must put this line
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
        // WARNING!!!!!!!!!!! IMPORTANT!!!!!!!!!!!!!!!!!
        val actionBar = supportActionBar //Hide action bar
        actionBar!!.hide()

        // function destined to setup App
        setupUI()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called.")
        super.onDestroy()
        unregisterReceiver(bluetoothStatus);
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this@BluetoothActivity, MainActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun setupUI() {
        //describe UI components
        pairedDevicesList = findViewById<View>(R.id.pairedDevicesList) as ListView
        scanDevicesList = findViewById<View>(R.id.availableDevices) as ListView
        turnBluetooth = findViewById<View>(R.id.turnBlth) as Switch
        scanDevicesBtn = findViewById<View>(R.id.refreshBtn) as Button
        editNameBtn = findViewById<View>(R.id.editNameBtn) as ImageButton
        availableDevicesTxt = findViewById<View>(R.id.availableDevicesTxt) as TextView
        deviceNameTxt1 = findViewById<View>(R.id.deviceNameTxt1) as TextView
        deviceNameTxt2 = findViewById<View>(R.id.deviceNameTxt2) as TextView
        actualDeviceNameTxt = findViewById<View>(R.id.actuallyDeviceNameTxt) as TextView
        MACAddressTxt = findViewById<View>(R.id.MACAddressTxt) as TextView
        pairedDeviceTxt = findViewById<View>(R.id.pairedDeviceTxt) as TextView

        //declaration default bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        dataModels = ArrayList<Device>()

        checkConnectPermission()

        //Getting actually device name and filled it to TextView
        val deviceName = bluetoothAdapter.getName()
        actualDeviceNameTxt!!.text = deviceName

        //checking while app is running whether being noticed bluetooth changed status
        val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStatus, BTIntent)

        //check bluetooth status after run app
        setBluetoothState()
        turnBluetooth!!.setOnClickListener { enableDisableBT() }
        scanDevicesBtn!!.setOnClickListener { startSearchingDevices() }
        editNameBtn!!.setOnClickListener { showDialog() }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun setBluetoothState() {
        if (bluetoothAdapter.isEnabled) {
            turnBluetooth!!.isChecked = true
            availableDevicesTxt!!.visibility = View.VISIBLE
            deviceNameTxt1!!.visibility = View.VISIBLE
            deviceNameTxt2!!.visibility = View.VISIBLE
            actualDeviceNameTxt!!.visibility = View.VISIBLE
            MACAddressTxt!!.visibility = View.VISIBLE
            pairedDeviceTxt!!.visibility = View.VISIBLE
            editNameBtn!!.visibility = View.VISIBLE
            scanDevicesBtn!!.visibility = View.VISIBLE
            scanDevicesBtn!!.isEnabled = false
            startSearchingDevices()
            showPairedDevices()
        } else {
            deviceNameTxt1!!.visibility = View.GONE
            deviceNameTxt2!!.visibility = View.GONE
            actualDeviceNameTxt!!.visibility = View.GONE
            MACAddressTxt!!.visibility = View.GONE
            pairedDeviceTxt!!.visibility = View.GONE
            availableDevicesTxt!!.visibility = View.GONE
            editNameBtn!!.visibility = View.GONE
            scanDevicesBtn!!.visibility = View.GONE
            turnBluetooth!!.isChecked = false
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun startSearchingDevices() {
        checkConnectPermission()
        scanDevicesList!!.adapter = null
        dataModels!!.clear()
        scanDevicesBtn!!.isEnabled = false
        bluetoothAdapter.startDiscovery()
        registerReceiver(bluetoothStatus, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(bluetoothStatus, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        registerReceiver(bluetoothStatus, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    fun showPairedDevices() {
        checkConnectPermission()
        pairedDevicesList!!.visibility = View.VISIBLE
        val pairedDevices = bluetoothAdapter.bondedDevices
        val strings = arrayOfNulls<String>(pairedDevices.size)
        var index = 0
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                strings[index] = device.name
                index++
            }
            val customBaseAdapter = PairedDevicesList(applicationContext, strings)
            pairedDevicesList!!.visibility = View.VISIBLE
            pairedDevicesList!!.adapter = customBaseAdapter
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    fun enableDisableBT() {
        checkConnectPermission()
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.")
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBTIntent)
            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(bluetoothStatus, BTIntent)
        }
        if (bluetoothAdapter.isEnabled) {
            bluetoothAdapter.disable()
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(bluetoothStatus, filter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.change_device_name_dialog)
        //WARNING!!!!!!!!! IMPORTANT!!!!!!!!!!!!!!!!!
        //To setup corner style we must set a background setup
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //WARNING!!!!!!!!! IMPORTANT!!!!!!!!!!!!!!!!!
        dialog.show()

        //If we wanna set text in dialog we must declare a element use "dialog" (e.g. dialog.findViewById(R.id.editName_dialog);)
        editName_dialog = dialog.findViewById<View>(R.id.editName_dialog) as EditText
        editName_dialog!!.setText(
            actualDeviceNameTxt!!.text,
            TextView.BufferType.EDITABLE
        ) //Took name device from deviceTextView without download it again

        //Setting the functionality of the Dialog Buttons
        //Declare dialog component
        save_Btn = dialog.findViewById<View>(R.id.save_Btn) as Button
        cancel_Btn = dialog.findViewById<View>(R.id.cancel_Btn) as Button
        save_Btn!!.setOnClickListener {
            checkConnectPermission()
            actualDeviceNameTxt!!.text = editName_dialog!!.text
            bluetoothAdapter.name = editName_dialog!!.text.toString()
            dialog.dismiss()
        }
        cancel_Btn!!.setOnClickListener { dialog.dismiss() }
    }

    private val bluetoothStatus: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action //String variable, which store actual intent status
            Log.i("INFO", action!!)
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                ) //Retrieve extended data from the intent or received
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "onReceive: STATE OFF")

                        //Set bluetooth switch in off position
                        turnBluetooth!!.isChecked = false
                        scanDevicesBtn!!.visibility = View.GONE
                        editNameBtn!!.visibility = View.GONE
                        availableDevicesTxt!!.visibility = View.GONE
                        deviceNameTxt1!!.visibility = View.GONE
                        deviceNameTxt2!!.visibility = View.GONE
                        actualDeviceNameTxt!!.visibility = View.GONE
                        MACAddressTxt!!.visibility = View.GONE
                        pairedDeviceTxt!!.visibility = View.GONE

                        //reset scanDevices adapter and delete all devices save on list
                        scanDevicesList!!.adapter = null
                        dataModels!!.clear()
                        scanDevicesList!!.visibility = View.GONE //Gone list of scanDevices
                        pairedDevicesList!!.visibility = View.GONE //Gone list of pairedDevices
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.d(
                        TAG,
                        "mBroadcastReceiver1: STATE TURNING OFF"
                    )
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON")

                        //Set bluetooth switch in on position
                        turnBluetooth!!.isChecked = true
                        scanDevicesBtn!!.visibility = View.VISIBLE
                        editNameBtn!!.visibility = View.VISIBLE
                        availableDevicesTxt!!.visibility = View.VISIBLE
                        deviceNameTxt1!!.visibility = View.VISIBLE
                        deviceNameTxt2!!.visibility = View.VISIBLE
                        actualDeviceNameTxt!!.visibility = View.VISIBLE
                        MACAddressTxt!!.visibility = View.VISIBLE
                        pairedDeviceTxt!!.visibility = View.VISIBLE
                        checkConnectPermission()

                        //Getting actually device name and filled it to TextView
                        val deviceName = bluetoothAdapter.name
                        actualDeviceNameTxt!!.text = deviceName

                        //reset scanDevices adapter and delete all devices save on list
                        startSearchingDevices()
                        scanDevicesList!!.visibility = View.VISIBLE //Visible list of scanDevices
                        scanDevicesBtn!!.isEnabled = false
                        showPairedDevices()
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> Log.d(
                        TAG,
                        "mBroadcastReceiver1: STATE TURNING ON"
                    )
                }
            }
            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                val modeValue =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)
                when (modeValue) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> Log.d(
                        TAG,
                        "onReceive: The device is not in discoverable mode but can still receive connection"
                    )
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> Log.d(
                        TAG,
                        "onReceive: The device is in discoverable mode"
                    )
                    BluetoothAdapter.SCAN_MODE_NONE -> Log.d(
                        TAG,
                        "onReceive: The device is not in discoverable mode and can not receive connection"
                    )
                    else -> Log.d(TAG, "onReceive: Error")
                }
            }
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    checkConnectPermission()
                }
                val deviceName = device!!.name
                val deviceHardwareAddress = device.address
                if (deviceName != null) {
                    dataModels!!.add(Device(deviceName, deviceHardwareAddress))
                }
                val adapter = ScannedDevicesList(
                    dataModels,
                    applicationContext
                )
                scanDevicesList!!.adapter = adapter
                scanDevicesList!!.visibility =
                    View.VISIBLE //Set visible mode for scanDevicesList element to show near accesses devices
                Log.i(
                    "WIADOMOSC", """
     ${device.name}
     ${device.address}
     """.trimIndent()
                )
                scanDevicesList!!.onItemClickListener =
                    OnItemClickListener { parent, view, position, id ->
                        val dataModel: Device = dataModels!![position]
                        Snackbar.make(
                            view,
                            dataModel.getName().toString() + "\n" + dataModel.getAddressMAC(),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("No action", null).show()
                    }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED")
                scanDevicesBtn!!.isEnabled = true
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    fun checkConnectPermission() {
        if (ContextCompat.checkSelfPermission(
                this@BluetoothActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this@BluetoothActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this@BluetoothActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this@BluetoothActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@BluetoothActivity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                2
            )
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}