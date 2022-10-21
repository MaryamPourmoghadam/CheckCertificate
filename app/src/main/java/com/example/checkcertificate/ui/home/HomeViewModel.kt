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
import java.io.*
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepo,
    application: Application
) : AndroidViewModel(application) {
    val appCert = MutableLiveData<Certificate?>()
    val localCertificateList: ArrayList<Certificate> = arrayListOf()
    private lateinit var packageManager: PackageManager
     lateinit var myCert: Certificate
    lateinit var installedApplicationList: List<ApplicationInfo>

    init {
        getMyCertificate()
        getInstalledAppList()
        getLocalCertificates()
    }

    private fun getMyCertificate() {
        viewModelScope.launch(Dispatchers.IO) {
            myCert = repository.getMyCert()
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

    private fun isTrustCertificate(selectedCertificate: Certificate): Boolean {
        this.appCert.value = null
        for (cert in localCertificateList)
            if (cert.publicKey == selectedCertificate.publicKey) return true
        return false
    }

    private fun isMyCertificate(selectedCertificate: Certificate): Boolean {
        this.appCert.value = null
        return selectedCertificate.publicKey == myCert.publicKey
    }

    fun showResult(selectedCertificate: Certificate, view: View) {
        val text: String =
            if (isTrustCertificate(selectedCertificate)) {
                "This application has trust certificate of google"
            } else if (isMyCertificate(selectedCertificate)) {
                "This application has custom certificate"
            } else {
                "This application doesn't have valid certificate"
            }
        Snackbar.make(
            view, text,
            Snackbar.LENGTH_LONG
        ).setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
            .setTextColor(Color.BLACK).setBackgroundTint(Color.YELLOW)
            .show()

    }

    private fun getLocalCertificates() {
        try {
            val ks = KeyStore.getInstance("AndroidCAStore")
            if (ks != null) {
                ks.load(null, null)
                val aliases = ks.aliases()
                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    val cert = ks.getCertificate(alias)
                    localCertificateList.add(cert)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
    }
    /*fun isApk(file: File?): Boolean {
        val fis: FileInputStream?
        val zipIs: ZipInputStream?
        var zEntry: ZipEntry?
        val dexFile = "classes.dex"
        val manifestFile = "AndroidManifest.xml"
        var hasDex = false
        var hasManifest = false
        if (file == null)
            return false
        try {
            fis = FileInputStream(file)
            zipIs = ZipInputStream(BufferedInputStream(fis))
            while (zipIs.nextEntry.also { zEntry = it } != null) {
                if (zEntry?.name.equals(dexFile, ignoreCase = true)) {
                    hasDex = true
                } else if (zEntry?.name.equals(manifestFile, ignoreCase = true)) {
                    hasManifest = true
                }
                if (hasDex && hasManifest) {
                    zipIs.close()
                    fis.close()
                    return true
                }
            }
            zipIs.close()
            fis.close()
        } catch (e: FileNotFoundException) {
            return false
        } catch (e: IOException) {
            return false
        }
        return false
    }*/

}