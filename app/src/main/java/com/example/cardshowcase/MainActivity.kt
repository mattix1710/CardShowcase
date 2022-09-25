package com.example.cardshowcase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.cardshowcase.BluetoothService.BluetoothActivity
import com.example.cardshowcase.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkConnectPermission()      //checkpermission need for bluetooth services

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> displayBTSettings()
            R.id.action_instructions -> displayInstructionsDialog()
            R.id.action_manual -> displayManualDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Displays the instructions for the Makao game **/
    private fun displayBTSettings(): Boolean {
        val intent = Intent(this, BluetoothActivity::class.java)
        startActivity(intent)

        return true
    }

    // TODO: fill and set Instructions for the game
    /** Displays the instructions for the Makao game **/
    private fun displayInstructionsDialog(): Boolean {
        var inflater = LayoutInflater.from(this)
        var view = inflater.inflate(R.layout.instructions_dialog, null)

        var instructionsDialog = AlertDialog.Builder(this)
        instructionsDialog.setTitle("Makao instructions")
        instructionsDialog.setView(view)
        instructionsDialog.setPositiveButton("I understand", null)
        instructionsDialog.create().show()

        return true
    }

    // TODO: fill and set proper manual view
    /** Displays manual for using this app **/
    private fun displayManualDialog(): Boolean {
        var inflater = LayoutInflater.from(this)
        var view = inflater.inflate(R.layout.manual_dialog, null)

        var manualDialog = AlertDialog.Builder(this)
        manualDialog.setTitle("App MANUAL")
        manualDialog.setView(view)
        manualDialog.setPositiveButton("I understand", null)
        manualDialog.create().show()

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    fun checkConnectPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                2
            )
        }
    }
}