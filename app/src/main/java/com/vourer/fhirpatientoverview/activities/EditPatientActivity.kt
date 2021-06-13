package com.vourer.fhirpatientoverview.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.StringType

class EditPatientActivity : AppCompatActivity() {
    private lateinit var idOutput: TextView
    private lateinit var givenInput: EditText
    private lateinit var familyInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthDateOutput: TextView

    private lateinit var patient: Patient
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()
    private var selectedGender = Enumerations.AdministrativeGender.values()[0].toCode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_patient)

        idOutput = findViewById(R.id.idValue)
        givenInput = findViewById(R.id.givenValue)
        familyInput = findViewById(R.id.familyValue)
        genderSpinner = findViewById(R.id.genderSpinner)
        birthDateOutput = findViewById(R.id.birthDateValue)

        val genders = Enumerations.AdministrativeGender.values()
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        genderSpinner.adapter = spinnerAdapter
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (Enumerations.AdministrativeGender.fromCode(selectedGender) != genders[position]) {
                    selectedGender = genders[position].toCode()
                }
            }
        }

        val extras = intent.extras ?: return
        val patientId = extras.getString("id")
        loadPatientData(patientId!!)
    }

    private fun loadPatientData(patientId: String) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                patient = hapiHandler.getPatientWithId(patientId)
            }
            job.join()
        }
        populateWidgets()
    }

    private fun populateWidgets() {
        val given = patient.name[0].given.joinToString(" ")
        val family = patient.name[0].family
        supportActionBar!!.title = "Editing patient $given $family"
        idOutput.text = patient.idElement.idPart.toString()
        givenInput.setText(given)
        familyInput.setText(family)
        birthDateOutput.text = patient.birthDate.toString()
        selectedGender = patient.gender.toCode()
    }

    fun saveChangesClicked(v: View) {
        val newGivenInput = givenInput.text.toString()
        val newFamilyInput = familyInput.text.toString()
        val newGenderValue = selectedGender
        val newPatient = patient
        if (newGivenInput != "" && newGivenInput != "null") {
            val givenParts: List<StringType> = newGivenInput.split(" ").map { StringType(it) }
            newPatient.name[0].setGiven(givenParts)
        }
        if (newFamilyInput != "" && newFamilyInput != "null") {
            newPatient.name[0].setFamily(newFamilyInput)
        }
        if (patient.gender.toCode() != newGenderValue) {
            newPatient.gender = Enumerations.AdministrativeGender.fromCode(newGenderValue)
        }
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                hapiHandler.editPatientInfo(newPatient)
            }
            job.join()
        }
        Toast.makeText(this, "Details updated", Toast.LENGTH_LONG).show()
        finish()
    }

    fun goBackClicked(v: View) {
        finish()
    }
}