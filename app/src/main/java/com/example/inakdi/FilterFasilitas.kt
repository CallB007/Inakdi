package com.example.inakdi

import android.widget.Filter
import com.example.inakdi.adapters.AdapterFasilitas
import com.example.inakdi.models.ModelFasilitas
import java.util.Locale

class FilterFasilitas(
    private val adapter: AdapterFasilitas,
    private val filterList: ArrayList<ModelFasilitas>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            constraint = constraint.toString().uppercase(Locale.getDefault())

            val filteredModels = ArrayList<ModelFasilitas>()
            for (i in filterList.indices) {
                if (filterList[i].category.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].title.uppercase(Locale.getDefault()).contains(constraint)
                ) {
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapter.fasilitasArrayList = results.values as ArrayList<ModelFasilitas>
        adapter.notifyDataSetChanged()
    }
}
