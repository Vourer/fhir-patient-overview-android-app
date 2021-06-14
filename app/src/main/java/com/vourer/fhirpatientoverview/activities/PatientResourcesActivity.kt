package com.vourer.fhirpatientoverview.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.adapters.PatientResourcesAdapter
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class PatientResourcesActivity : AppCompatActivity() {
    private lateinit var startingInput: EditText
    private lateinit var endingInput: EditText
    private lateinit var resourcesRecycler: RecyclerView

    private lateinit var patient: Patient
    private var resources = arrayListOf<Resource>()
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_resources)

        startingInput = findViewById(R.id.startingDateInput)
        endingInput = findViewById(R.id.endingDateInput)
        resourcesRecycler = findViewById(R.id.recyclerView)
        resourcesRecycler.layoutManager = LinearLayoutManager(this)
        resourcesRecycler.adapter = PatientResourcesAdapter(resources)

        val extras = intent.extras ?: return
        val patientId = extras.getString(ExtraCodes.PATIENT_ID)
        loadPatientResources(patientId!!)
    }

    private fun loadPatientResources(patientId: String) {
        resources.clear()
        val start = startingInput.text.toString()
        val end = endingInput.text.toString()
        val startingDate = if (start != "" && start != "null") {
             parseStringToDate(start)
        } else {
            LocalDate.of(1900, 1, 1)
        }
        val endingDate = if (end != "" && end != "null") {
            parseStringToDate(end)
        } else {
            LocalDate.of(2100, 12, 31)
        }
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                patient = hapiHandler.getPatientWithId(patientId)
                val observations = hapiHandler.getPatientObservations(patient)
                val medications =  hapiHandler.getPatientMedicationRequests(patient)
                for (o in observations) {
                    if (o is Observation) {
                        val obsDate = o.issued.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        if (obsDate.isAfter(startingDate) && obsDate.isBefore(endingDate)) {
                            resources.add(o)
                        }
                    }
                }
                for (m in medications) {
                    if (m is MedicationRequest) {
                        val medDate = m.authoredOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        if (medDate.isAfter(startingDate) && medDate.isBefore(endingDate)) {
                            resources.add(m)
                        }
                    }
                }
                resources.sortWith(Comparator<Resource> { r1, r2 ->
                    when {
                        getResourceSortingKey(r1) > getResourceSortingKey(r2) -> 1
                        getResourceSortingKey(r1) == getResourceSortingKey(r2) -> 0
                        else -> -1
                    }
                })
            }
            job.join()
            val name = patient.name[0].nameAsSingleString
            supportActionBar!!.title = "$name's Resources"
            resourcesRecycler.adapter?.notifyDataSetChanged()
        }
    }

    private fun getResourceSortingKey(resource: Resource): Date {
        if (resource is Observation) {
            return resource.issued
        } else if (resource is MedicationRequest) {
            return resource.authoredOn
        }
        return Calendar.getInstance().time
    }

    private fun parseStringToDate(dateString: String): LocalDate {
        val dateElements = dateString.split(".", "-", "/")
        return if (dateElements.size < 3) {
            LocalDate.now()
        } else {
            val date: LocalDate = try {
                val dateInts = dateElements.map { it.toInt() }
                LocalDate.of(dateInts[0], dateInts[1], dateInts[2])
            } catch (e: NumberFormatException) {
                LocalDate.now()
            }
            date
        }
    }

    fun showGraphClicked(v: View) {
        val i = Intent(this, GraphActivity::class.java)
        i.putExtra(ExtraCodes.PATIENT_ID, patient.idElement.idPart)
        startActivity(i)
    }

    fun refreshClicked(v: View) {
        loadPatientResources(patient.idElement.idPart)
    }
}