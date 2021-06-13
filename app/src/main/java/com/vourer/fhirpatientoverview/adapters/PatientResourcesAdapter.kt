package com.vourer.fhirpatientoverview.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vourer.fhirpatientoverview.R
import com.vourer.fhirpatientoverview.activities.ResourceDetailsActivity
import com.vourer.fhirpatientoverview.utils.ExtraCodes
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Resource
import kotlin.collections.ArrayList

class PatientResourcesAdapter (private val mResources: ArrayList<Resource>) : RecyclerView.Adapter<PatientResourcesAdapter.ViewHolder>()
{
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val itemDate: TextView = itemView.findViewById(R.id.resourceItemDate)
        val itemName: TextView = itemView.findViewById(R.id.resourceItemName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientResourcesAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val searchResultsView = inflater.inflate(R.layout.patient_resource_row, parent, false)
        return ViewHolder(searchResultsView)
    }

    override fun onBindViewHolder(viewHolder: PatientResourcesAdapter.ViewHolder, position: Int) {
        val resource: Resource = mResources[position]
        val context = viewHolder.itemView.context

        val itemDate = viewHolder.itemDate
        val itemName = viewHolder.itemName
        var itemType = ExtraCodes.TYPE_MED_REQ

        if (resource is MedicationRequest) {
            val medCode = resource.medicationCodeableConcept.codingFirstRep.display
            val medDate = resource.authoredOn
            itemName.text = medCode
            itemDate.text = medDate.toString()
        } else if (resource is Observation) {
            itemType = ExtraCodes.TYPE_OBS
            val obsCode = resource.code.codingFirstRep.display
            val obsDate = resource.issued
            itemName.text = obsCode
            itemDate.text = obsDate.toString()
        }

        viewHolder.itemDate.setOnClickListener {
            val i = Intent(context, ResourceDetailsActivity::class.java)
            i.putExtra(ExtraCodes.RESOURCE_ID, resource.idElement.idPart.toString())
            i.putExtra(ExtraCodes.RESOURCE_TYPE, itemType)
            context.startActivity(i)
        }
        viewHolder.itemName.setOnClickListener {
            val i = Intent(context, ResourceDetailsActivity::class.java)
            i.putExtra(ExtraCodes.RESOURCE_ID, resource.idElement.idPart.toString())
            i.putExtra(ExtraCodes.RESOURCE_TYPE, itemType)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return mResources.size
    }
}