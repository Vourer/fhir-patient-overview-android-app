package com.vourer.fhirpatientoverview.clients

import android.util.Log;
import java.util.Collections;
import java.util.Optional;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;


object RestClient {
    private val TAG = "REST CLIENT"
    private val BASE_URL = "http://192.168.0.105:8080/baseR4"
    private var iGenericClient: IGenericClient? = null
    private var fhirContext: FhirContext? = null

    fun getFhirContext(): FhirContext {
        if (fhirContext == null) {
            fhirContext = FhirContext.forR4()
            val proxy: Map<String, String> = getProxyDetails()
            if (proxy.containsKey("server") && proxy.containsKey("port")) {
                fhirContext!!.restfulClientFactory.setProxy(
                        proxy.get("server"), Integer.parseInt(proxy.get("port"))
                    )
            }
        }
        return fhirContext!!
    }

    private fun getProxyDetails(): Map<String, String> {
        try {
            val proxyParams: HashMap<String, String> = HashMap()
            val server: Optional<String> = Optional.ofNullable(System.getProperty("http.proxyHost"))
            val port: Optional<String> = Optional.ofNullable(System.getProperty("http.proxyPort"))
            server.ifPresent { serverValue -> proxyParams["server"] = serverValue }
            port.ifPresent { portValue -> proxyParams["port"] = portValue }
            return proxyParams
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
        }
        return Collections.emptyMap()
    }

    fun getGenericClient(): IGenericClient {
        if (fhirContext == null) {
            fhirContext = getFhirContext()
        }
        if (iGenericClient == null) {
            iGenericClient = fhirContext!!.newRestfulGenericClient(BASE_URL)
        }
        return iGenericClient!!
    }

}