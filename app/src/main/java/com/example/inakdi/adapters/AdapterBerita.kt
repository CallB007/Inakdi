package com.example.inakdi.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.databinding.RowBeritaBinding
import com.example.inakdi.models.ModelBerita
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterBerita(context: Context, beritaArrayList: ArrayList<ModelBerita>) :
    RecyclerView.Adapter<AdapterBerita.HolderBerita>() {

    private lateinit var binding: RowBeritaBinding

    private companion object {
        private const val TAG = "ADAPTER_BERITA_TAG"
    }

    private var context: Context = context
    var beritaArrayList: ArrayList<ModelBerita> = beritaArrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBerita {
        // Inflate the row_berita.xml
        binding = RowBeritaBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderBerita(binding.root)
    }

    override fun onBindViewHolder(holder: HolderBerita, position: Int) {
        // Get the data from beritaArrayList
        val modelBerita = beritaArrayList[position]

        val title = modelBerita.title
        val description = modelBerita.description
        val timestamp = modelBerita.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)

        loadBeritaFirstImage(modelBerita, holder)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate
    }

    private fun loadBeritaFirstImage(modelBerita: ModelBerita, holder: HolderBerita) {
        // Load first image from Firebase storage
        val beritaId = modelBerita.id

        Log.d(TAG, "loadBeritaFirstImage: beritaId: $beritaId")

        val reference = FirebaseDatabase.getInstance().getReference("Berita")
        reference.child(beritaId).child("Images").limitToFirst(1)
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
        return beritaArrayList.size
    }

    inner class HolderBerita(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageIv = binding.imageIv
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var dateTv = binding.dateTv
    }
}
