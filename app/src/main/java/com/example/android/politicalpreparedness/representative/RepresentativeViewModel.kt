package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.election.adapter.ApiStatus
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import java.lang.Exception

class RepresentativeViewModel: ViewModel() {

    // DONE: Establish live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    // DONE: Create function to fetch representatives from API from a provided address
    /**
     *  The following code will prove helpful in constructing a representative from the API.
     *  This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data
    housing representatives
     */

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    fun getRepresentatives() {
        viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                val (offices, officials) =
                    CivicsApi.retrofitService
                        .getRepresentatives(_address.value!!.toFormattedString())
                _representatives.value = offices.flatMap { office ->
                    office.getRepresentatives(officials)
                }
                _status.value = ApiStatus.OK
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _representatives.value = ArrayList()
            }
        }
    }

    // DONE: Create function get address from geo location
    fun getAddressFromGeolocation(address: Address) {
        _address.value = address
    }

    // DONE: Create function to get address from individual fields
    fun getAddressFromFields(
        line1 : String,
        line2 : String?,
        city : String,
        state : String,
        zip : String
    ) {
        _address.value = Address(line1, line2, city, state, zip)
    }
}
