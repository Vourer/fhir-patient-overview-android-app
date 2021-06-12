package com.vourer.fhirpatientoverview.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Quantity
import org.hl7.fhir.r4.model.Resource
import java.lang.Exception
import java.util.*

class ResourceDetailsActivity : AppCompatActivity() {
    private lateinit var idOutput: TextView
    private lateinit var typeOutput: TextView
    private lateinit var codeOutput: TextView
    private lateinit var dateOutput: TextView
    private lateinit var valueOutput: TextView
    private lateinit var noteOutput: TextView

    //private lateinit var resource: Resource
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_details)

        supportActionBar!!.title = "Resource details"

        idOutput = findViewById(R.id.idValue)
        typeOutput = findViewById(R.id.typeValue)
        codeOutput = findViewById(R.id.codeValue)
        dateOutput = findViewById(R.id.dateValue)
        valueOutput = findViewById(R.id.valueValue)
        noteOutput = findViewById(R.id.noteValue)

        val extras = intent.extras ?: return
        val resourceId = extras.getString("id")
        val resourceType = extras.getString("type")
        loadResourceData(resourceId!!, resourceType!!)
    }

    fun editResourceClicked(v: View) {
        Toast.makeText(this, "'Edit' clicked", Toast.LENGTH_SHORT).show()
//        val i = Intent(this, EditPatientActivity::class.java)
//        i.putExtra("id", patient.idElement.idPart.toString())
//        startActivity(i)
    }

    fun goBackClicked(v: View) {
        finish()
    }

    private fun loadResourceData(resourceId: String, resourceType: String) {
        var resource: Resource? = null
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                resource = if (resourceType == "m") {
                    hapiHandler.getMedicationRequestWithId(resourceId)
                } else {
                    hapiHandler.getObservationWithId(resourceId)
                }
            }
            job.join()
            populateWidgets(resource!!)
        }
    }

    private fun populateWidgets(resource: Resource) {
        idOutput.text = resource.idElement.idPart
        if (resource is Observation) {
            typeOutput.text = "Observation"
            codeOutput.text = resource.code.codingFirstRep.display
            dateOutput.text = resource.issued.toString()
            try {
                val quantity: Quantity = resource.valueQuantity
                val valueText = String.format(Locale.getDefault(), "%.2f %s", quantity.value, quantity.unit)
                valueOutput.text = valueText
            } catch (e: Exception) {
                valueOutput.text = "N/A"
            }
            noteOutput.text = resource.noteFirstRep.text
        } else if (resource is MedicationRequest) {
            typeOutput.text = "Medication Request"
            codeOutput.text = resource.medicationCodeableConcept.codingFirstRep.display
            dateOutput.text = resource.authoredOn.toString()
            val valueText = findViewById<TextView>(R.id.valueText)
            valueText.text = ""
            valueOutput.text = ""
            noteOutput.text = resource.noteFirstRep.text
        }
    }
}