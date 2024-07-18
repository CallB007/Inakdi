package com.example.inakdi.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.inakdi.databinding.RowCategoryBinding
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.inakdi.RvListenerCategory
import com.example.inakdi.models.ModelCategory

class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory
) : Adapter<AdapterCategory.HolderCategory>() {

    private lateinit var binding: RowCategoryBinding

    private companion object {
        private const val TAG = "ADAPTER_CATEGORY_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val modelCategory = categoryArrayList[position]
        val icon = modelCategory.icon
        val category = modelCategory.category
        val color = modelCategory.color

        holder.categoryIconIv.setImageResource(icon)
        holder.categoryTv.text = category
        holder.categoryIconIv.setBackgroundColor(ContextCompat.getColor(context, color))

        holder.itemView.setOnClickListener {
            Log.d(TAG, "Category clicked: $category")
            rvListenerCategory.onCategoryClick(modelCategory)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    inner class HolderCategory(itemView: View) : ViewHolder(itemView) {
        var categoryIconIv = binding.categoryIconIv
        var categoryTv = binding.categoryTv
    }
}
