package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.election.adapter.ApiStatus
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import java.lang.Exception



// DONE: Construct ViewModel and provide election datasource
class ElectionsViewModel(private val dataSource: ElectionDao): ViewModel() {

    // DONE: Create live data val for upcoming elections
    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections : LiveData<List<Election>>
        get() = _upcomingElections

    // DONE: Create live data val for saved elections
    private val _followedElections = MutableLiveData<List<Election>>()
    val followedElections : LiveData<List<Election>>
        get() = _followedElections

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    fun getUpcomingElections() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                _upcomingElections.value = CivicsApi.retrofitService.getElections().elections
                _status.value = ApiStatus.OK
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _upcomingElections.value = ArrayList()
            }
        }
    }

    fun getFollowedElections() {
        viewModelScope.launch {
            _followedElections.value = dataSource.getAllElections()
        }
    }

    // DONE: Create functions to navigate to saved or upcoming election voter info
    private val _navigateToVoterInfo = MutableLiveData<Election>()
    val navigateToVoterInfo : LiveData<Election>
        get() = _navigateToVoterInfo

    fun onElectionClicked(election: Election) {
        _navigateToVoterInfo.value = election
    }

    fun onElectionNavigated() {
        _navigateToVoterInfo.value = null
    }
}