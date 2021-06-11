package com.vourer.fhirpatientoverview.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient


class PatientDetailsActivity : AppCompatActivity() {
    // views for patient attributes
    lateinit var idOutput: TextView
    lateinit var givenOutput: TextView
    lateinit var familyOutput: TextView
    lateinit var genderOutput: TextView
    lateinit var birthDateOutput: TextView
    lateinit var historyRecycler: RecyclerView

    lateinit var patient: Patient
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

        idOutput = findViewById(R.id.idValue)
        givenOutput = findViewById(R.id.givenValue)
        familyOutput = findViewById(R.id.familyValue)
        genderOutput = findViewById(R.id.genderValue)
        birthDateOutput = findViewById(R.id.birthDateValue)
//        historyRecycler = findViewById(R.id.recyclerView)
//        historyRecycler.layoutManager = LinearLayoutManager(this)
        // historyRecycler.adapter = PatientHistoryAdapter(historyItems)

        val extras = intent.extras ?: return
        val patientId = extras.getString("id")
        loadPatientData(patientId!!)
    }

    fun editPatientClicked(v: View) {
        Toast.makeText(this, "'Edit' clicked", Toast.LENGTH_SHORT).show()
//        val i = Intent(this, EditPatientActivity::class.java)
//        i.putExtra("id", patient.id.toString())
//        startActivityForResult(i, 1000)
    }

    fun goBackClicked(v: View) {
        finish()
    }

    private fun loadPatientData(patientId: String) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                patient = hapiHandler.getPatientWithId(patientId)
            }
            job.join()
            val given = patient.name[0].given.joinToString(" ")
            val family = patient.name[0].family
            supportActionBar!!.title = "Details of $given $family"
            idOutput.text = patient.idElement.idPart.toString()
            givenOutput.text = given
            familyOutput.text = family
            genderOutput.text = patient.gender.toString()
            birthDateOutput.text = patient.birthDate.toString()
        }
    }

}