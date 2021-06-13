package com.vourer.fhirpatientoverview.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.control.HapiFhirHandler
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Comparator


class GraphActivity : AppCompatActivity() {
    private lateinit var startingInput: EditText
    private lateinit var endingInput: EditText
    private lateinit var dataTypeSpinner: Spinner
    private lateinit var chart: LineChart

    private lateinit var patient: Patient
    private var resources = arrayListOf<Observation>()
    private var chartCode = "Body Weight"
    private val hapiHandler: HapiFhirHandler = HapiFhirHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        startingInput = findViewById(R.id.startingDateInput)
        endingInput = findViewById(R.id.endingDateInput)
        dataTypeSpinner = findViewById(R.id.chartDataTypeSpinner)
        chart = findViewById(R.id.lineChart)


        val extras = intent.extras ?: return
        val patientId = extras.getString(ExtraCodes.PATIENT_ID)

        val types = arrayOf("Body Weight", "Body Height")
        val spinnerTypesAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )
        dataTypeSpinner.adapter = spinnerTypesAdapter
        dataTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (chartCode != types[position]) {
                    chartCode = types[position]
                    loadPatientResources(patientId!!)
                }
            }
        }
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
                for (o in observations) {
                    if (o is Observation) {
                        val obsCode = o.code.codingFirstRep.display
                        if (obsCode == chartCode) {
                            val obsDate =
                                o.issued.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                            if (obsDate.isAfter(startingDate) && obsDate.isBefore(endingDate)) {
                                resources.add(o)
                            }
                        }
                    }
                }
                resources.sortWith(Comparator<Observation> { r1, r2 ->
                    when {
                        r1.issued > r2.issued -> 1
                        r1.issued == r2.issued -> 0
                        else -> -1
                    }
                })
            }
            job.join()
            drawChart()
        }
    }

    private fun drawChart() {
        chart.clear()
        val xLabels = arrayListOf<String>()
        val yVals = arrayListOf<Entry>()

        for (i in 0 until resources.size) {
            val observation = resources[i]
            val obsDate = fhirDateToString(
                observation.issued.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            )
            val obsValue = observation.valueQuantity.value.setScale(2, RoundingMode.HALF_EVEN).toFloat()
            if (obsValue > 0) {
                xLabels.add(obsDate)
                yVals.add(Entry(i.toFloat(), obsValue))
            }
        }

        val dataSet = LineDataSet(yVals, "$chartCode Chart")
        dataSet.setDrawCircles(true)
        val lineData = LineData(dataSet)
        chart.data = lineData

        // the labels that should be drawn on the XAxis
        val formatter: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return if (value.toInt() < xLabels.size) {
                    xLabels[value.toInt()]
                } else {
                    ""
                }
            }
        }
        val xAxis: XAxis = chart.xAxis
        xAxis.granularity = 1f // minimum axis-step (interval) is 1
        xAxis.labelRotationAngle = 90f
        xAxis.valueFormatter = formatter

        chart.description.isEnabled = false
        chart.invalidate()
    }

    private fun fhirDateToString(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("dd MM yyyy"))
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

    fun refreshChartClicked(v: View) {
        loadPatientResources(patient.idElement.idPart.toString())
    }
}