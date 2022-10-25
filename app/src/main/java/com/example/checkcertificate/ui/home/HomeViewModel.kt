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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepo,
    application: Application
) : AndroidViewModel(application) {
    val appCert = MutableLiveData<Certificate?>()
    val localCACertificateList: ArrayList<Certificate> = arrayListOf()
    private lateinit var packageManager: PackageManager
     lateinit var myCert: Certificate
    lateinit var installedApplicationList: List<ApplicationInfo>

    init {
        getMyCertificate()
        getInstalledAppList()
        getLocalCACertificates()
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
        return false
    }

    private fun isMyCertificate(selectedCertificate: Certificate): Boolean {
        this.appCert.value = null
        return selectedCertificate.publicKey == myCert.publicKey
    }

    fun showResult(selectedApp:ApplicationInfo, view: View) {
        val text: String =
            if (installedFromPlayStore(selectedApp)) {
                "This application has trust certificate of google"
            } else if (isMyCertificate(getAppCertificate(selectedApp))) {
                "This application has custom certificate"
            } else {
                "Maybe this is system app or signed by unknown publisher"
            }
        Snackbar.make(
            view, text,
            Snackbar.LENGTH_LONG
        ).setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
            .setTextColor(Color.BLACK).setBackgroundTint(Color.YELLOW)
            .show()

    }

    private fun getLocalCACertificates() {
        try {
            val ks = KeyStore.getInstance("AndroidCAStore")
            if (ks != null) {
                ks.load(null, null)
                val aliases = ks.aliases()
                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    val cert = ks.getCertificate(alias)
                    localCACertificateList.add(cert)
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

      private fun installedFromPlayStore(app:ApplicationInfo):Boolean{
        // A list with valid installers package name
        val validInstallers: List<String> = ArrayList(listOf("com.android.vending", "com.google.android.feedback"))
        // The package name of the app that has installed your app
        val installer=packageManager.getInstallerPackageName(app.packageName)
            // true if your app has been downloaded from Play Store
       return installer != null && validInstallers.contains(installer)
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