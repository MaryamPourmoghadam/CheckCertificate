package com.example.checkcertificate.ui.home

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.View
import androidx.lifecycle.AndroidViewModel
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
    //val appCert = MutableLiveData<Certificate?>()
    // private val localCACertificateList: ArrayList<Certificate> = arrayListOf()
    private lateinit var packageManager: PackageManager
    private lateinit var myCert: Certificate
    lateinit var installedApplicationList: List<ApplicationInfo>

    init {
        getMyCertificate()
        getInstalledAppList()
        //getLocalCACertificates()
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

    private fun getAppCertificate(appInfo: ApplicationInfo): Certificate {
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


    private fun isMyCertificate(selectedCertificate: Certificate): Boolean {
        //this.appCert.value = null
        return selectedCertificate.publicKey == myCert.publicKey
    }

    fun showResult(selectedApp: ApplicationInfo, view: View) {
        val text: String = when (getAppInstaller(selectedApp)) {
            "play store" -> {
                getApplication<Application>().getString(R.string.trusted_in_play_store)
            }
            "galaxy store" -> {
                getApplication<Application>().getString(R.string.trusted_in_galaxy_store)
            }
            else -> {
                if (isMyCertificate(getAppCertificate(selectedApp))) {
                    getApplication<Application>().getString(R.string.trusted_in_custom_cert)
                } else {
                    getApplication<Application>().getString(R.string.system_app_or_not_trusted)
                }
            }
        }
        Snackbar.make(
            view, text,
            Snackbar.LENGTH_LONG
        ).setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
            .setTextColor(Color.BLACK).setBackgroundTint(Color.YELLOW)
            .show()

    }


    private fun getAppInstaller(app: ApplicationInfo): String {
        // A list with valid installers package name
        val playStoreInstallerPackageName = listOf(
            //for play store
            "com.android.vending",
            "com.google.android.feedback"
        )
        val galaxyStoreInstallerPackageName = "com.sec.android.app.samsungapps"
        val installer = packageManager.getInstallerPackageName(app.packageName)
        if (installer != null) {
            if (playStoreInstallerPackageName.contains(installer))
                return "play store"
            if (galaxyStoreInstallerPackageName.contains(installer))
                return "galaxy store"
        }
        return ""
    }


}
/* private fun getLocalCACertificates() {
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
*/