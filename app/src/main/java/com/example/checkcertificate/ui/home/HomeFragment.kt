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
import org.bouncycastle.asn1.x509.TBSCertificate
import java.io.IOException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.util.*
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
        }
        appListAdapter.submitList(homeViewModel.installedApplicationList)
        binding.rvApps.adapter = appListAdapter

        homeViewModel.appCert.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.d("app cert", " \n $it")
                //homeViewModel.showResult(it, view)

                for (cert in homeViewModel.localCertificateList) {
                    try {
                        //get tbs certificate
                          val input = ASN1InputStream((it as X509Certificate).tbsCertificate)
                          val certificate = TBSCertificate.getInstance(input.readObject())
  val certSignature=it.signature

                        if (verifyCertSignature(it as X509Certificate, cert as X509Certificate))
                            Toast.makeText(activity, "trusted", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {

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
    fun verifyCertSignature(lowerCert: X509Certificate, higherCert: X509Certificate): Boolean {
        // Compute certificate digest
        val hashAlgName = lowerCert.sigAlgName.split("with".toRegex()).toTypedArray()[0]
        val md: MessageDigest = MessageDigest.getInstance(hashAlgName)
        val tbsCertificate = lowerCert.tbsCertificate
        md.update(tbsCertificate)
        val computedDigest: ByteArray = md.digest()
        // Decode signature
        val cryptoAlgName = lowerCert.sigAlgName.split("with".toRegex()).toTypedArray()[1]
        val decCipher: Cipher = Cipher.getInstance(cryptoAlgName)
        decCipher.init(Cipher.DECRYPT_MODE, higherCert.publicKey)
        val decodedSignature: ByteArray = decCipher.doFinal(lowerCert.signature)
        val decodedDigest = extractAsn1EncodedSignature(decodedSignature)
        return computedDigest.contentEquals(decodedDigest)
    }

    @Throws(IOException::class)
    private fun extractAsn1EncodedSignature(bytes: ByteArray): ByteArray {
        val ais = ASN1InputStream(bytes)
        val superSeq = ais.readObject() as DLSequence
        // Extract signature bytes
        val e1 = superSeq.objects
        val subSeq = e1.nextElement() as DLSequence
        val octstr = e1.nextElement() as DEROctetString
        val octets = octstr.octets
        // Extract signature algorithm OID string (not used here, though)
        val e2 = subSeq.objects
        val algorithmIdentifier = e2.nextElement() as DERObjectIdentifier
        val oidString = algorithmIdentifier.toString()
        return octets
    }

}


