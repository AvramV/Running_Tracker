package com.avramhorseman.runningtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.avramhorseman.runningtracker.R
import com.avramhorseman.runningtracker.adapters.RunAdapter
import com.avramhorseman.runningtracker.db.Run
import com.avramhorseman.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.avramhorseman.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.avramhorseman.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.avramhorseman.runningtracker.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.avramhorseman.runningtracker.other.Constants.MAP_ZOOM
import com.avramhorseman.runningtracker.other.Constants.POLYLINE_COLOR
import com.avramhorseman.runningtracker.other.Constants.POLYLINE_WIDTH
import com.avramhorseman.runningtracker.other.TrackingUtility
import com.avramhorseman.runningtracker.services.Polyline
import com.avramhorseman.runningtracker.services.TrackingService
import com.avramhorseman.runningtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    val latMax = 51.03894
    val latMin = 51.0387
    val longMax = 31.8805
    val longMin = 31.8802

    private var isTracking = false

    private var pathPoints = mutableListOf<Polyline>()

    private var currentTimeMillis = 0L

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null

    private var menu: Menu? = null

    @set: Inject
    var weight = 70f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener { cancelRun() }
        }

        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener{
            toggleRun()
        }

        btnZoomRun.setOnClickListener{
            moveCameraOnUser()
        }
        btnFinishRun.setOnClickListener {
            zoomToSeeAllTrack()
            endRunAndSaveToDB()
        }

        mapView.getMapAsync{
            map = it
            addAllPolylines()
        }

        subscribeToObserves()
    }

    private fun sendCommandToService(action: String){
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLong = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLong = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLong)
                .add(lastLatLong)

            checkSquare(lastLatLong)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun checkSquare(lastLatLong: LatLng) {
        val lat = lastLatLong.latitude
        val long = lastLatLong.longitude

        if (lat<latMax && lat>latMin && long<longMax && long>longMin){
            Toast.makeText(requireContext(), "you are at home, bro!", Toast.LENGTH_LONG).show()
        }
    }

    private fun zoomToSeeAllTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDB(){
        map?.snapshot { bmp ->
            var distanceImMeters = 0
            for (polyline in pathPoints){
                distanceImMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceImMeters / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10) /10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceImMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimestamp, avgSpeed, distanceImMeters, currentTimeMillis, caloriesBurned)
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            cancelRun()
        }

    }

    private fun addAllPolylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun subscribeToObserves(){
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner){
            currentTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedTime(currentTimeMillis, true)
            tvTimer.text = formattedTime
        }

    }

    private fun toggleRun(){
        if (isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
        CancelTrackingDialog().apply {
            setYesListener {
                cancelRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)

    }

    private fun cancelRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if (!isTracking && currentTimeMillis > 0L){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking){
            menu?.getItem(0)?.isVisible = true
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }

        moveCameraOnUser()
    }



    private fun moveCameraOnUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(),
                MAP_ZOOM
            ))
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    /*

     override fun onDestroy() {
         super.onDestroy()
         mapView?.onDestroy()
     }
 */

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}