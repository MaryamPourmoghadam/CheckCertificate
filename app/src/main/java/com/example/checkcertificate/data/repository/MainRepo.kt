package com.example.checkcertificate.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepo  @Inject constructor(@ApplicationContext val context: Context){
    fun getMyCert():Certificate{
        val inputstream= context.assets.open("mp.crt")
        val cf = CertificateFactory.getInstance("X509")
        return cf.generateCertificate(inputstream)
    }
}