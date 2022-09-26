package com.example.checkcertificate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.checkcertificate.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appListAdapter = AppListAdapter {
            homeViewModel.appCert.value = homeViewModel.getAppCertificate(it)
        }
        appListAdapter.submitList(homeViewModel.installedApplicationList)
        binding.rvApps.adapter = appListAdapter

        homeViewModel.appCert.observe(viewLifecycleOwner) {
            if (it!=null){
                val result = homeViewModel.areEqualCertificate(it, homeViewModel.myCert)
                homeViewModel.showResult(result, view)
            }
        }
    }

}