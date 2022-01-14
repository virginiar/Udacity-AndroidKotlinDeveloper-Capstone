package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // DONE: Add ViewModel values and create ViewModel
        val application = requireActivity().application
        val dataSource = ElectionDatabase.getInstance(application).electionDao
        val viewModelFactory = VoterInfoViewModelFactory(dataSource)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(VoterInfoViewModel::class.java)

        // DONE: Add binding values
        val binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.voterInfoViewModel = viewModel

        // DONE: Populate voter info -- hide views without provided data.
        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
        */
        val arguments = VoterInfoFragmentArgs.fromBundle(requireArguments())
        viewModel.getVoterInfo(
            arguments.argElectionId,
            arguments.argDivision
        )

        viewModel.correspondenceAddress.observe(viewLifecycleOwner, Observer { address ->
            if (address.isEmpty()) {
                binding.addressGroup.visibility = View.GONE
            }
        })

        viewModel.hasURL.observe(viewLifecycleOwner, Observer { hasURL ->
            if (!hasURL) {
                binding.stateHeader.visibility = View.GONE
                binding.stateLocations.visibility = View.GONE
                binding.stateBallot.visibility = View.GONE
            }
        })

        // DONE: Handle loading of URLs

        binding.stateBallot.setOnClickListener {
            loadUrl(viewModel.ballotInformationURL)
        }

        binding.stateLocations.setOnClickListener {
            loadUrl(viewModel.votingLocationURL)
        }

        // DONE: Handle save button UI state
        // DONE: cont'd Handle save button clicks
        viewModel.isElectionFollowed.observe(viewLifecycleOwner, Observer { isSaved ->
            if (isSaved) {
                binding.followElectionButton.text = getString(R.string.unfollow_election_button)
            } else {
                binding.followElectionButton.text = getString(R.string.follow_election_button)
            }
        })

        binding.followElectionButton.setOnClickListener {
            viewModel.onFollowButtonClick()
        }

        return binding.root
    }

    // DONE: Create method to load URL intents
    private fun loadUrl(url: String?) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}