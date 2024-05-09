package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.deplide.application.android.trafficcdmforoperator.R

class AdministrativeStateFragment : Fragment(), StateFragmentDataUpdater {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_administrative_state, container, false)
    }

    override fun addStateFragmentDataUpdateListener(listener: StateFragmentDataUpdateListener) {
    }
}