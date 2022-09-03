package com.example.cardshowcase

import android.app.Dialog
import android.content.DialogInterface

interface DialogActionButtonListener {
    fun onPosButtonClicked(dialog: DialogInterface)
    fun onNegButtonClicked(dialog: DialogInterface)
}