package org.deplide.application.android.trafficcdmforoperator.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.deplide.application.android.trafficcdmforoperator.databinding.FragmentAdministrativeStateBinding

class AdministrativeStateFragment : BaseStateFragment() {
    private lateinit var binding: FragmentAdministrativeStateBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdministrativeStateBinding.inflate(inflater, container, false)
        return binding.root
    }
}