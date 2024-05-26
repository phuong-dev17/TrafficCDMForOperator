package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.deplide.application.android.trafficcdmforoperator.R
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentAdministrativeStateBinding

class AdministrativeStateFragment : Fragment(), StateFragmentDataUpdater {
    private lateinit var binding: FragmentAdministrativeStateBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdministrativeStateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun addStateFragmentDataUpdateListener(listener: StateFragmentDataUpdateListener) {
    }
}