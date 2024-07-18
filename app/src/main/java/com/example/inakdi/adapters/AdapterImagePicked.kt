package com.example.inakdi.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.inakdi.R
import com.example.inakdi.databinding.RowImagesPickedBinding
import com.example.inakdi.models.ModelImagePicked

class AdapterImagePicked(
    private val context: Context,
    private val imagePickedArrayList: ArrayList<ModelImagePicked>
) : Adapter<AdapterImagePicked.HolderImagePicked>() {

    private lateinit var binding: RowImagesPickedBinding

    private companion object {
        private const val TAG = "IMAGES_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        // Inflate/bind the row_images_picked.xml
        binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagePicked(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        // Get data from UI
        val model = imagePickedArrayList[position]
        val imageUri = model.imageUri
        Log.d(TAG, "onBindViewHolder: imageUri $imageUri")

        try {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.gambar)
                .into(holder.imageIv)
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder: ", e)
        }

        holder.closeBtn.setOnClickListener {
            imagePickedArrayList.remove(model)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        // Return the size of list
        return imagePickedArrayList.size
    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView) {
        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }
}
