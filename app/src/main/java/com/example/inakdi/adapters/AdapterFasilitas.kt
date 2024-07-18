package com.example.inakdi.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.inakdi.FilterFasilitas
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.activities.FasilitasDetailsActivity
import com.example.inakdi.databinding.RowFasilitasBinding
import com.example.inakdi.models.ModelFasilitas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterFasilitas(context: Context, fasilitasArrayList: ArrayList<ModelFasilitas>) :
    RecyclerView.Adapter<AdapterFasilitas.HolderFasilitas>(), Filterable {

    private lateinit var binding: RowFasilitasBinding

    private companion object {
        private const val TAG = "ADAPTER_FASILITAS_TAG"
    }

    private var context: Context = context
    var fasilitasArrayList: ArrayList<ModelFasilitas> = fasilitasArrayList
    private var filterList: ArrayList<ModelFasilitas> = fasilitasArrayList

    private var filter: FilterFasilitas? = null

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFasilitas {
        // Inflate the row_fasilitas.xml
        binding = RowFasilitasBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderFasilitas(binding.root)
    }

    override fun onBindViewHolder(holder: HolderFasilitas, position: Int) {
        // Get the data from fasilitasArrayList
        val modelFasilitas = fasilitasArrayList[position]

        val title = modelFasilitas.title
        val description = modelFasilitas.description
        val timestamp = modelFasilitas.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)
        val category = modelFasilitas.category

        loadFasilitasFirstImage(modelFasilitas, holder)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        holder.itemView.setOnClickListener {

            val intent = Intent(context, FasilitasDetailsActivity::class.java)
            intent.putExtra("fasilitasId", modelFasilitas.id)
            context.startActivity(intent)
        }

        // Set category icon
        holder.categoryBtn.setImageResource(Utils.getCategoryIcon(category))

        // Set background color of category icon
        holder.categoryBtn.setBackgroundColor(Utils.getCategoryColor(context, category))
    }

    private fun loadFasilitasFirstImage(modelFasilitas: ModelFasilitas, holder: HolderFasilitas) {
        // Load first image from Firebase storage
        val fasilitasId = modelFasilitas.id

        Log.d(TAG, "loadFasilitasFirstImage: fasilitasId: $fasilitasId")

        val reference = FirebaseDatabase.getInstance().getReference("Fasilitas Umum")
        reference.child(fasilitasId).child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val imageUrl = "${ds.child("imageUrl").value}"
                        Log.d(TAG, "onDataChange: imageUrl: $imageUrl")

                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.gambar)
                                .into(holder.imageIv)
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ", error.toException())
                }
            })
    }

    override fun getItemCount(): Int {
        return fasilitasArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterFasilitas(this, filterList)
        }
        return filter as FilterFasilitas
    }

    inner class HolderFasilitas(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageIv = binding.imageIv
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var dateTv = binding.dateTv
        var categoryBtn = binding.categoryBtn
    }
}
