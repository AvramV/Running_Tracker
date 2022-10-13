package com.avramhorseman.runningtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.avramhorseman.runningtracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog: DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit){
         yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_RunningTracker)
            .setTitle("Cancel Run?")
            .setMessage("Are you sure to cancel the current run?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _, _ ->
                yesListener?.let { yes -> yes() }
            }
            .setNegativeButton("No", null)
            .create()

        
    }
}