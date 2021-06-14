package com.vourer.fhirpatientoverview.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient
import kotlin.Exception


class PatientDetailsActivity : AppCompatActivity() {
    private lateinit var idOutput: TextView
    private lateinit var givenOutput: TextView
    private lateinit var familyOutput: TextView
    private lateinit var genderOutput: TextView
    private lateinit var birthDateOutput: TextView

    private lateinit var patient: Patient
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

        idOutput = findViewById(R.id.idValue)
        givenOutput = findViewById(R.id.givenValue)
        familyOutput = findViewById(R.id.familyValue)
        genderOutput = findViewById(R.id.genderValue)
        birthDateOutput = findViewById(R.id.birthDateValue)

        val extras = intent.extras ?: return
        val patientId = extras.getString(ExtraCodes.PATIENT_ID)
        loadPatientData(patientId!!)
    }

    override fun onResume() {
        super.onResume()
        try {
            loadPatientData(patient.idElement.idPart)
        } catch (e: Exception) {
        }
    }

    override fun onRestart() {
        super.onRestart()
        try {
            loadPatientData(patient.idElement.idPart)
        } catch (e: Exception) {
        }
    }

    fun editPatientClicked(v: View) {
        val i = Intent(this, EditPatientActivity::class.java)
        i.putExtra(ExtraCodes.PATIENT_ID, patient.idElement.idPart)
        startActivity(i)
    }

    fun resourcesClicked(v: View) {
        Toast.makeText(this, "Loading Observations and Medication Requests...", Toast.LENGTH_SHORT).show()
        val i = Intent(this, PatientResourcesActivity::class.java)
        i.putExtra(ExtraCodes.PATIENT_ID, patient.idElement.idPart)
        startActivity(i)
    }

    fun historyClicked(v: View) {
        Toast.makeText(this, "Loading history of changes...", Toast.LENGTH_SHORT).show()
        val i = Intent(this, PatientVersionsActivity::class.java)
        i.putExtra(ExtraCodes.PATIENT_ID, patient.idElement.idPart)
        startActivity(i)
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
            populateWidgets()
        }
    }

    private fun populateWidgets() {
        val fullName = patient.name[0].nameAsSingleString
        val given = patient.name[0].given.joinToString(" ")
        val family = patient.name[0].family
        supportActionBar!!.title = "Details of $fullName"
        idOutput.text = patient.idElement.idPart
        givenOutput.text = given
        familyOutput.text = family
        genderOutput.text = patient.gender.display
        birthDateOutput.text = patient.birthDate.toString()
        setHistoryButton()
    }

    private fun setHistoryButton() {
        val url = patient.idElement.baseUrl + "/Patient/" + patient.idElement.idPart + "/_history"
        var history = listOf<Patient?>()
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                history = hapiHandler.getPatientHistory(url)
            }
            job.join()
            val historyButton: Button = findViewById(R.id.historyButton)
            historyButton.isEnabled = (history.size >= 2)
        }
    }

}