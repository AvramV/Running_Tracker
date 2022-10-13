package com.avramhorseman.runningtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.avramhorseman.runningtracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogDeleteAllRuns: DialogFragment() {

    private var yesDeleteListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit){
        yesDeleteListener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_RunningTracker)
            .setIcon(R.drawable.ic_delete)
            .setTitle("Delete All Runs?")
            .setMessage("You will delete all saved runs, are you sure?")
            .setPositiveButton("Yes"){_, _ ->
                yesDeleteListener?.let {yes -> yes() }
            }
            .setNegativeButton("No", null)
            .create()
            }
    }
