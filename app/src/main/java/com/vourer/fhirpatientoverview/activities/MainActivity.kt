package com.vourer.fhirpatientoverview.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.adapters.PatientSearchAdapter
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient


class MainActivity : AppCompatActivity() {
    private lateinit var patientNameInput: EditText
    private lateinit var searchResultsRecycler: RecyclerView
    private var patients = arrayListOf<Patient>()
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.title = "FHIR Patient Overview"

        patientNameInput = findViewById(R.id.patientNameInput)
        searchResultsRecycler = findViewById(R.id.recyclerView)
        searchResultsRecycler.layoutManager = LinearLayoutManager(this)
        searchResultsRecycler.adapter = PatientSearchAdapter(patients)

        reloadPatients()
    }

    override fun onResume() {
        super.onResume()
        reloadPatients()
    }

    override fun onRestart() {
        super.onRestart()
        reloadPatients()
    }

    fun searchPatientClicked(v: View) {
        val nameFromInput = patientNameInput.text.toString()
        Toast.makeText(this, "Searching for names like '$nameFromInput'", Toast.LENGTH_LONG).show()
        reloadPatients()
    }

    private fun reloadPatients() {
        patients.clear()
        searchResultsRecycler.adapter?.notifyDataSetChanged()
        val nameFromInput = patientNameInput.text.toString()
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                val patientResult = if (nameFromInput.isNotEmpty()) {
                    hapiHandler.getSearchedPatients(nameFromInput)
                } else {
                    hapiHandler.getAllPatients()
                }
                for (patient in patientResult) {
                    if (patient != null) {
                        patients.add(patient)
                    }
                }
            }
            job.join()
            searchResultsRecycler.adapter?.notifyDataSetChanged()
        }
    }
}