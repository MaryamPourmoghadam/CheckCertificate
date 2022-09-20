package com.example.checkcertificate.ui.home

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.checkcertificate.R
import com.example.checkcertificate.data.repository.MainRepo
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepo,
    application: Application
) : AndroidViewModel(application) {
    val myCert = MutableLiveData<Certificate>()
    lateinit var myCertificate:Certificate
    private lateinit var packageManager: PackageManager
    lateinit var installedApplicationList: List<ApplicationInfo>

    init {
        getMyCertificate()
        getInstalledAppList()
    }

    private fun getMyCertificate() {
        viewModelScope.launch (Dispatchers.IO){
            myCert.postValue(repository.getMyCert())
            myCertificate=repository.getMyCert()
        }
    }



    private fun getInstalledAppList() {
        packageManager = getApplication<Application>().applicationContext.packageManager
        installedApplicationList =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    fun getAppCertificate(appInfo: ApplicationInfo): Certificate {
        lateinit var packageInfo: PackageInfo
        lateinit var certificateFactory: CertificateFactory
        lateinit var certificate: Certificate
        try {
            packageInfo =
                packageManager.getPackageInfo(appInfo.packageName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val signatures = packageInfo.signatures

        val cert = signatures[0]?.toByteArray()
        val input = ByteArrayInputStream(cert)
        try {
            certificateFactory = CertificateFactory.getInstance("X509")
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
        try {
            certificate = (certificateFactory.generateCertificate(input))
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
        return certificate
    }

    fun areEqualCertificate(appCert: Certificate, myCert: Certificate): Boolean {
        return appCert == myCert
    }

    fun showResult(result: Boolean, view: View) {
        val text =
            if (result) getApplication<Application>().getString(R.string.match_your_certificate) else getApplication<Application>().getString(
                R.string.dosnt_match_your_certificate
            )
        Snackbar.make(
            view, text,
            Snackbar.LENGTH_LONG
        ).setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
            .setTextColor(Color.BLACK)
            .apply {
                if (result) this.setBackgroundTint(Color.GREEN) else this.setBackgroundTint(Color.RED)
            }
            .show()
    }

}