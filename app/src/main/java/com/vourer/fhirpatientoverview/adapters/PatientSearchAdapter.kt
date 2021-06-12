package com.vourer.fhirpatientoverview.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.activities.PatientDetailsActivity
import org.hl7.fhir.r4.model.Patient
import kotlin.collections.ArrayList


class PatientSearchAdapter (private val mPatients: ArrayList<Patient>) : RecyclerView.Adapter<PatientSearchAdapter.ViewHolder>()
{
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val searchItemName: TextView = itemView.findViewById(R.id.searchItemName)
        val searchItemBirthDate: TextView = itemView.findViewById(R.id.searchItemBirthDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientSearchAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val searchResultsView = inflater.inflate(R.layout.search_result_row, parent, false)
        return ViewHolder(searchResultsView)
    }

    override fun onBindViewHolder(viewHolder: PatientSearchAdapter.ViewHolder, position: Int) {
        val patient: Patient = mPatients[position]
        val context = viewHolder.itemView.context

        val itemNameView = viewHolder.searchItemName
        val name = "${patient.name[0].given.joinToString(" ")} ${patient.name[0].family}"
        itemNameView.text  = name

        viewHolder.searchItemName.setOnClickListener {
            val i = Intent(context, PatientDetailsActivity::class.java)
            i.putExtra("id", patient.idElement.idPart.toString())
            context.startActivity(i)
        }

        val itemBirthView = viewHolder.searchItemBirthDate
        val birthDate = patient.birthDateElement.asStringValue()
        val birthText = "birth date: $birthDate"
        itemBirthView.text = birthText

        viewHolder.searchItemBirthDate.setOnClickListener {
            val i = Intent(context, PatientDetailsActivity::class.java)
            i.putExtra("id", patient.idElement.idPart.toString())
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return mPatients.size
    }

    fun loadNewPatient(newPatient: Patient) {
        val count = mPatients.size
        mPatients.add(newPatient)
        notifyItemInserted(count)
    }
}