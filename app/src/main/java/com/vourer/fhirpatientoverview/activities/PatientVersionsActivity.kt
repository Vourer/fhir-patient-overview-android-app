package com.vourer.fhirpatientoverview.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.adapters.PatientVersionsAdapter
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient


class PatientVersionsActivity : AppCompatActivity() {
    private lateinit var versionsRecycler: RecyclerView
    private lateinit var patient: Patient
    private var versions = arrayListOf<Patient>()
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_versions)

        versionsRecycler = findViewById(R.id.recyclerView)
        versionsRecycler.layoutManager = LinearLayoutManager(this)
        versionsRecycler.adapter = PatientVersionsAdapter(versions)

        val extras = intent.extras ?: return
        val patientId = extras.getString(ExtraCodes.PATIENT_ID)
        loadPatient(patientId!!)
    }

    private fun loadPatient(patientId: String) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                patient = hapiHandler.getPatientWithId(patientId)
            }
            job.join()
            val fullName = patient.name[0].nameAsSingleString
            supportActionBar!!.title = "Version history for $fullName"
            loadPatientVersions()
        }
    }

    private fun loadPatientVersions() {
        val versionsUrl = patient.idElement.baseUrl + "/Patient/" + patient.idElement.idPart + "/_history"
        var history = listOf<Patient?>()
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                history = hapiHandler.getPatientHistory(versionsUrl)
            }
            job.join()
            for (entry in history) {
                if (entry != null) {
                    versions.add(entry)
                }
            }
            versionsRecycler.adapter?.notifyDataSetChanged()
        }
    }

    fun goBackClicked(v: View) {
        finish()
    }
}