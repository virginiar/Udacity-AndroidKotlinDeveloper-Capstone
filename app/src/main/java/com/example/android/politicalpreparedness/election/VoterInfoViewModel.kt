package com.example.android.politicalpreparedness.election


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.election.adapter.ApiStatus
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch
import java.lang.Exception

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {

    // DONE: Add live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo : LiveData<VoterInfoResponse>
        get() = _voterInfo

    // DONE: Add var and methods to populate voter info
    private var _electionId = -1

    private val _correspondenceAddress = MutableLiveData<String>()
    val correspondenceAddress : LiveData<String>
        get() = _correspondenceAddress


    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    fun getVoterInfo(electionId : Int, division : Division) {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                _electionId = electionId
                findVoterInfo(division, electionId)
                findElectionInDatabase()
                votingLocationURL =
                    voterInfo.value?.state?.get(0)?.electionAdministrationBody?.electionInfoUrl
                    ?: ""
                ballotInformationURL =
                    voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
                        ?: ""
                _hasURL.value = votingLocationURL.isNotEmpty() || ballotInformationURL.isNotEmpty()
                _correspondenceAddress.value =
                    voterInfo.value?.state?.get(0)?.electionAdministrationBody?.correspondenceAddress?.toFormattedString()
                        ?: ""
                _status.value = ApiStatus.OK
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
            }
        }
    }

    private suspend fun findVoterInfo(
        division: Division,
        electionId: Int
    ) {
        val address = if (division.state.isNotEmpty()) {
            division.state + ", " + division.country
        } else {
            "California" + ", " + division.country
        }
        _voterInfo.value = CivicsApi.retrofitService
            .getVoterInfo(address, electionId)
    }

    // DONE: Add var and methods to support loading URLs
    lateinit var votingLocationURL : String
    lateinit var ballotInformationURL : String
    private val _hasURL = MutableLiveData<Boolean>()
    val hasURL : LiveData<Boolean>
        get() = _hasURL

    // DONE: Add var and methods to save and remove elections to local database
    // DONE: cont'd -- Populate initial state of save button to reflect proper action based on election saved status
    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    private val _isElectionFollowed = MutableLiveData<Boolean>()
    val isElectionFollowed : LiveData<Boolean>
        get() = _isElectionFollowed

    private suspend fun findElectionInDatabase() {
        val election = voterInfo.value?.election
        _isElectionFollowed.value = dataSource.getElectionById(election!!.id) != null
    }

    fun onFollowButtonClick() {
        if (_isElectionFollowed.value == true) {
            unfollowElection()
        } else {
            followElection()
        }
    }

    private fun unfollowElection() {
        viewModelScope.launch {
            _voterInfo.value?.let { dataSource.deleteElection(it.election.id) }
            _isElectionFollowed.value = false
        }
    }

    private fun followElection() {
        viewModelScope.launch {
            _voterInfo.value?.let { dataSource.insertElection(it.election) }
            _isElectionFollowed.value = true
        }
    }
}