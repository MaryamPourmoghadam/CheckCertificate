package com.example.checkcertificate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.checkcertificate.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.security.cert.Certificate

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var appListAdapter: AppListAdapter
    private var appCert: Certificate? = null

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
            appCert = homeViewModel.getAppCertificate(it)
            val result = homeViewModel.areEqualCertificate(appCert!!, homeViewModel.myCertificate)
            homeViewModel.showResult(result,view)

        }
        appListAdapter.submitList(homeViewModel.installedApplicationList)
        binding.rvApps.adapter = appListAdapter
        //doesnt observe
        homeViewModel.myCert.observe(viewLifecycleOwner) {
                if ( appCert!=null) {
                    val result = homeViewModel.areEqualCertificate(appCert!!, it)
                    homeViewModel.showResult(result, view)
                    appCert=null
                }


        }
    }

}