package com.vourer.fhirpatientoverview.control

import android.util.Log
import com.vourer.fhirpatientoverview.clients.RestClient
import org.hl7.fhir.r4.model.*
import java.io.Serializable
import java.util.stream.Collectors


class HapiFhirHandler: Serializable {

    fun getAllPatients(): List<Patient?> {
        val client = RestClient.getGenericClient()
        val bundle: Bundle = client.search<Bundle>()
                .forResource(Patient::class.java)
                .encodedJson()
                .returnBundle(Bundle::class.java)
                .execute()
        val patients = getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient::class.java::isInstance)
                .map(Patient::class.java::cast)
                .collect(Collectors.toList())
        patients.sortWith(Comparator<Patient?> { p1, p2 ->
            if (p1 != null && p2 != null && p1.name[0].family > p2.name[0].family) 1
            else if (p1 != null && p2 != null && p1.name[0].family == p2.name[0].family) 0
            else -1
        })
        return patients
    }

    fun getSearchedPatients(searchName: String): List<Patient?> {
        val client = RestClient.getGenericClient()
        val patients = client.search<Bundle>()
                .forResource(Patient::class.java)
                .count(100)
                .where(Patient.FAMILY.matches().value(searchName))
                .encodedJson()
                .returnBundle(Bundle::class.java)
                .execute()
                .entry
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient::class.java::isInstance)
                .map(Patient::class.java::cast)
                .collect(Collectors.toList())
        patients.sortWith(Comparator<Patient?> { p1, p2 ->
            if (p1 != null && p2 != null && p1.name[0].family > p2.name[0].family) 1
            else if (p1 != null && p2 != null && p1.name[0].family == p2.name[0].family) 0
            else -1
        })
        return patients
    }

    fun getPatientWithId(patientId: String): Patient {
        val client = RestClient.getGenericClient()
        return client.read().resource(Patient::class.java).withId(patientId).execute()
    }

    fun getPatientWithUrl(patientUrl: String): Patient {
        val client = RestClient.getGenericClient()
        return client.read().resource(Patient::class.java).withUrl(patientUrl).execute()
    }

    fun getPatientObservations(patient: Patient): List<Resource?> {
        val client = RestClient.getGenericClient()
        val bundle: Bundle = client.search<Bundle>()
                .forResource(Observation::class.java)
                .where(Observation.SUBJECT.hasId(patient.idElement.idPart))
                .returnBundle(Bundle::class.java)
                .execute()
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Observation::class.java::isInstance)
                .map(Observation::class.java::cast)
                .collect(Collectors.toList())
    }

    fun getPatientMedicationRequests(patient: Patient): List<Resource?> {
        val client = RestClient.getGenericClient()
        val bundle: Bundle = client.search<Bundle>()
                .forResource(MedicationRequest::class.java)
                .where(MedicationRequest.SUBJECT.hasId(patient.idElement.idPart))
                .returnBundle(Bundle::class.java)
                .execute()
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(MedicationRequest::class.java::isInstance)
                .map(MedicationRequest::class.java::cast)
                .collect(Collectors.toList())
    }

    fun getPatientHistory(url: String): List<Patient?> {
        val client = RestClient.getGenericClient()
        val bundle: Bundle = client.search<Bundle>()
            .byUrl(url)
            .encodedJson()
            .returnBundle(Bundle::class.java)
            .execute()
        val pHistory = getPagedEntries(bundle).stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(Patient::class.java::isInstance)
            .map(Patient::class.java::cast)
            .collect(Collectors.toList())
        pHistory.sortWith(Comparator<Patient?> { p1, p2 ->
            if (p1 != null && p2 != null && p1.meta.versionId < p2.meta.versionId) 1
            else if (p1 != null && p2 != null && p1.meta.versionId == p2.meta.versionId) 0
            else -1
        })
        return pHistory
    }

    fun getObservationWithId(resourceId: String): Resource {
        val client = RestClient.getGenericClient()
        return client.read().resource(Observation::class.java).withId(resourceId).execute()
    }

    fun getMedicationRequestWithId(resourceId: String): Resource {
        val client = RestClient.getGenericClient()
        return client.read().resource(MedicationRequest::class.java).withId(resourceId).execute()
    }

    private fun getPagedEntries(receivedBundle: Bundle): List<Bundle.BundleEntryComponent> {
        var bundle = receivedBundle
        val entries: MutableCollection<Bundle.BundleEntryComponent> = bundle.entry
        while (bundle.getLink(Bundle.LINK_NEXT) != null) {
            bundle = RestClient.getGenericClient()
                    .loadPage()
                    .next(bundle)
                    .execute()
            entries.addAll(bundle.entry)
        }
        val entryList = arrayListOf<Bundle.BundleEntryComponent>()
        for (entry in entries) {
            entryList.add(entry)
        }
        return entryList
    }

    fun editPatientInfo(updatedPatient: Patient) {
        val client = RestClient.getGenericClient()
        val response = client.update().resource(updatedPatient).execute()
        Log.i("Patient update response: ", "$response")
    }

    fun editObservationInfo(updatedObservation: Observation) {
        val client = RestClient.getGenericClient()
        val response = client.update().resource(updatedObservation).execute()
        Log.i("Observation update response: ", "$response")
    }

    fun editMedicationInfo(updatedMedicationRequest: MedicationRequest) {
        val client = RestClient.getGenericClient()
        val response = client.update().resource(updatedMedicationRequest).execute()
        Log.i("Medication Request update response: ", "$response")
    }
}