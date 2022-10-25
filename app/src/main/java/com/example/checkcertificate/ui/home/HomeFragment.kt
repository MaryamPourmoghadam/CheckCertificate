package com.example.checkcertificate.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.checkcertificate.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.DERObjectIdentifier
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DLSequence
import java.io.IOException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException


const val OPEN_DOCUMENT_REQUEST_CODE = 2

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var appListAdapter: AppListAdapter
    var isTrusted = false
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
            homeViewModel.appCert.value = homeViewModel.getAppCertificate(it)
            homeViewModel.showResult(it,view)
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
        for (caCert in homeViewModel.localCACertificateList)
        Log.d("app cert", " \n $caCert")


        homeViewModel.appCert.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.d("app cert", " \n******* $it")
                //homeViewModel.showResult(it, view)

                for (caCert in homeViewModel.localCACertificateList) {
                   try {
                       it.verify(caCert.publicKey)
                       Toast.makeText(activity,"verified",Toast.LENGTH_LONG).show()
                       break
                   }catch (e:Exception){

                   }

                }
            }
        }


        /* for ((i,cert) in x509Certificates.withIndex()){
        Log.d("trust cert","$i \n $cert")
        }*/


//11:19 PM	Failed to commit install session 328805266 with command cmd package install-commit 328805266. Error: INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package com.example.checkcertificate signatures do not match the previously installed version; ignoring!

    }

    @Throws(
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        CertificateEncodingException::class,
        SignatureException::class,
        IOException::class,
        NoSuchAlgorithmException::class
    )

    private fun getCaCert(): Certificate {
        val inputstream= context?.assets?.open("ca.crt")
        val cf = CertificateFactory.getInstance("X509")
        return cf.generateCertificate(inputstream)
    }


}


