package com.example.checkcertificate.ui.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appListAdapter = AppListAdapter {
            // homeViewModel.appCert.value = homeViewModel.getAppCertificate(it)
            homeViewModel.showResult(it, view)
        }
        appListAdapter.submitList(homeViewModel.installedApplicationList)
        binding.rvApps.adapter = appListAdapter


        //checked verify method by my ca
        /* val caCert=getCaCert()
        try {
            homeViewModel.myCert.verify(caCert.publicKey)
            Toast.makeText(activity,"verified",Toast.LENGTH_LONG).show()
        }catch (e:Exception){
            Toast.makeText(activity,"doesn't match",Toast.LENGTH_LONG).show()
        }*/
        //11:19 PM	Failed to commit install session 328805266 with command cmd package install-commit 328805266. Error: INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package com.example.checkcertificate signatures do not match the previously installed version; ignoring!
        /* private fun getCaCert(): Certificate {
        val inputstream= context?.assets?.open("ca.crt")
        val cf = CertificateFactory.getInstance("X509")
        return cf.generateCertificate(inputstream)
    }*/


    }
}



