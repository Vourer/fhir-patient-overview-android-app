package com.vourer.fhirpatientoverview.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Patient


class VersionDetailsActivity : AppCompatActivity() {
    private lateinit var idOutput: TextView
    private lateinit var modifiedOutput: TextView
    private lateinit var givenOutput: TextView
    private lateinit var familyOutput: TextView
    private lateinit var genderOutput: TextView
    private lateinit var birthDateOutput: TextView

    private lateinit var patient: Patient
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_details)

        idOutput = findViewById(R.id.idValue)
        modifiedOutput = findViewById(R.id.modifiedValue)
        givenOutput = findViewById(R.id.givenValue)
        familyOutput = findViewById(R.id.familyValue)
        genderOutput = findViewById(R.id.genderValue)
        birthDateOutput = findViewById(R.id.birthDateValue)

        val extras = intent.extras ?: return
        val patientUrl = extras.getString(ExtraCodes.PATIENT_URL)
        loadVersionData(patientUrl!!)
    }

    private fun loadVersionData(url: String) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                patient = hapiHandler.getPatientWithUrl(url)
            }
            job.join()
            populateWidgets()
        }
    }

    private fun populateWidgets() {
        val version = patient.meta.versionIdElement.idPart
        supportActionBar!!.title = "Details of version $version"
        modifiedOutput.text = patient.meta.lastUpdated.toString()
        idOutput.text = patient.idElement.idPart
        givenOutput.text = patient.name[0].given.joinToString(" ")
        familyOutput.text = patient.name[0].family
        genderOutput.text = patient.gender.display
        birthDateOutput.text = patient.birthDate.toString()
    }

    fun goBackClicked(v: View) {
        finish()
    }
}