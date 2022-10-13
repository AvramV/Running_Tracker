package com.avramhorseman.runningtracker.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.avramhorseman.runningtracker.db.RunDAO
import com.avramhorseman.runningtracker.repos.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel(){

    val totalDistance = mainRepository.getTotalDistanceInMeters()
    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val allRunsSortedByDate = mainRepository.getAllRunsSortedByDate()



}