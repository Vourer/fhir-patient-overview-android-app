package com.vourer.fhirpatientoverview.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.activities.VersionDetailsActivity
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import org.hl7.fhir.r4.model.Patient


class PatientVersionsAdapter (private val mVersions: ArrayList<Patient>) : RecyclerView.Adapter<PatientVersionsAdapter.ViewHolder>()
{
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val itemVersion: TextView = itemView.findViewById(R.id.versionItemVersion)
        val itemDateModified: TextView = itemView.findViewById(R.id.versionItemDateModified)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientVersionsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val versionsView = inflater.inflate(R.layout.patient_version_row, parent, false)
        return ViewHolder(versionsView)
    }

    override fun onBindViewHolder(viewHolder: PatientVersionsAdapter.ViewHolder, position: Int) {
        val patientVersion: Patient = mVersions[position]
        val context = viewHolder.itemView.context

        val itemVersion = viewHolder.itemVersion
        val versionText = patientVersion.meta.versionIdElement.idPart
        itemVersion.text = versionText

        itemVersion.setOnClickListener {
            val i = Intent(context, VersionDetailsActivity::class.java)
            i.putExtra(ExtraCodes.PATIENT_URL, patientVersion.id)
            context.startActivity(i)
        }

        val itemDateModified = viewHolder.itemDateModified
        val dateText = patientVersion.meta.lastUpdated
        itemDateModified.text = dateText.toString()

       itemDateModified.setOnClickListener {
           val i = Intent(context, VersionDetailsActivity::class.java)
           i.putExtra(ExtraCodes.PATIENT_URL, patientVersion.id)
           context.startActivity(i)
       }
    }

    override fun getItemCount(): Int {
        return mVersions.size
    }
}