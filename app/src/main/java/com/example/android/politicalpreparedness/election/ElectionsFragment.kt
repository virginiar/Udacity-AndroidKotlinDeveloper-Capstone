package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.network.models.Election

class ElectionsFragment: Fragment() {

    // DONE: Declare ViewModel
    private lateinit var viewModel: ElectionsViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // DONE: Add ViewModel values and create ViewModel
        val application = requireActivity().application
        val dataSource = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = ElectionsViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ElectionsViewModel::class.java)

        // DONE: Add binding values
        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.electionViewModel = viewModel

        // DONE: Link elections to voter info
        viewModel.navigateToVoterInfo.observe(viewLifecycleOwner, Observer { election ->
            election?.let {
                navigateToVoterInfo(election)
                viewModel.onElectionNavigated()
            }
        })

        // DONE: Initiate recycler adapters
        val electionListener = ElectionListener { election ->
            viewModel.onElectionClicked(election)
        }
        val upcomingElectionAdapter = ElectionListAdapter(
            electionListener
        )

        val savedElectionAdapter = ElectionListAdapter(
            electionListener
        )

        // DONE: Populate recycler adapters
        binding.upcomingElectionsRecyclerView.adapter = upcomingElectionAdapter
        viewModel.upcomingElections.observe(viewLifecycleOwner, Observer { elections ->
            upcomingElectionAdapter.submitList(elections)
        })
        binding.savedElectionsRecyclerView.adapter = savedElectionAdapter
        viewModel.followedElections.observe(viewLifecycleOwner, Observer { elections ->
            savedElectionAdapter.submitList(elections)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // DONE: Refresh adapters when fragment loads
        viewModel.getUpcomingElections()
        viewModel.getFollowedElections()
    }

    private fun navigateToVoterInfo(election: Election) {
        findNavController().navigate(
            ElectionsFragmentDirections
                .actionElectionsFragmentToVoterInfoFragment(
                    election.id,
                    election.division
                )
        )
    }
}