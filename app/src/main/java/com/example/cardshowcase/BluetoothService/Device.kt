package com.example.cardshowcase.BluetoothService

class Device(var name: String?, var addressMAC: String?) {
    @JvmName("getName1")
    fun getName(): String? {
        return name
    }

    fun getAddressMAC(): Any? {
        return addressMAC
    }
}