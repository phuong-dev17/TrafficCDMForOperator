package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentLocationStateBinding
import org.deplide.application.android.trafficcdmforoperator.tcmf.TCMFMessage

class LocationStateFragment : Fragment() {
    private lateinit var binding: FragmentLocationStateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLocationStateBinding.inflate(inflater, container, false)
        return binding.root
    }


}