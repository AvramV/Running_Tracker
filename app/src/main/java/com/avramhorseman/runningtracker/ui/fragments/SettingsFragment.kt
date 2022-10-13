package com.avramhorseman.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.avramhorseman.runningtracker.R
import com.avramhorseman.runningtracker.other.Constants
import com.avramhorseman.runningtracker.other.Constants.DELETE_ALL_RUNS_DIALOG_TAG
import com.avramhorseman.runningtracker.other.Constants.KEY_NAME
import com.avramhorseman.runningtracker.other.Constants.KEY_WEIGHT
import com.avramhorseman.runningtracker.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharePref: SharedPreferences

    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                DELETE_ALL_RUNS_DIALOG_TAG
            ) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener { deleteALlRuns() }
        }

        loadFieldsFromSharedPref()

        btnDeleteALlRuns.setOnClickListener{
            DialogDeleteAllRuns().apply {
                setYesListener {
                    deleteALlRuns()
                }
            }.show(parentFragmentManager, DELETE_ALL_RUNS_DIALOG_TAG)
        }

        btnApplyChanges.setOnClickListener{
            val success = applyChangesToSharePref()
            if (success){
                Snackbar.make(view, "Saved changes", LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out all the fields", LENGTH_LONG).show()
            }
        }
    }

    private fun deleteALlRuns() {
        viewModel.deleteAllRuns()
        Snackbar.make(requireView(), "All Runs deleted successfully", LENGTH_LONG).show()
    }

    private fun loadFieldsFromSharedPref(){
        val name = sharePref.getString(KEY_NAME, "")?: ""
        val weight = sharePref.getFloat(KEY_WEIGHT, 70f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharePref(): Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }

        sharePref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .apply()

        requireActivity().tvToolbarTitle.text = "Let`s go, $name!"
        return true

    }


}

