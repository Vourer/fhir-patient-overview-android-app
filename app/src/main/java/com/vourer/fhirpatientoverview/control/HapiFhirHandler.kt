package com.vourer.fhirpatientoverview.control

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
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient::class.java::isInstance)
                .map(Patient::class.java::cast)
                //.sorted(Comparator.comparing{patient -> (patient!!.name[0].family).toString() })
                .collect(Collectors.toList())
    }

    fun getSearchedPatients(searchName: String): List<Patient?> {
        val client = RestClient.getGenericClient()
        return client.search<Bundle>()
                .forResource(Patient::class.java)
                .count(100)
                .where(Patient.FAMILY.matches().value(searchName))
                .encodedJson()
                .returnBundle(Bundle::class.java)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient::class.java::isInstance)
                .map(Patient::class.java::cast)
                //.sorted(Comparator.comparing{patient -> (patient!!.name[0].family).toString() })
                .collect(Collectors.toList())
    }

    fun getPatientWithId(patientId: String): Patient {
        val client = RestClient.getGenericClient()
        return client.read().resource(Patient::class.java).withId(patientId).execute()
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
}