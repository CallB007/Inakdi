package com.example.inakdi

import android.widget.Filter
import com.example.inakdi.adapters.AdapterBerita
import com.example.inakdi.models.ModelBerita
import java.util.Locale

class FilterBerita(
    private val adapter: AdapterBerita,
    private val filterList: ArrayList<ModelBerita>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            constraint = constraint.toString().uppercase(Locale.getDefault())

            val filteredModels = ArrayList<ModelBerita>()
            for (i in filterList.indices) {
                if (filterList[i].title.uppercase(Locale.getDefault()).contains(constraint)
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
        adapter.beritaArrayList = results.values as ArrayList<ModelBerita>
        adapter.notifyDataSetChanged()
    }
}
