package com.example.inakdi.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.inakdi.databinding.FragmentHomeBinding
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.inakdi.R
import com.example.inakdi.RvListenerCategory
import com.example.inakdi.Utils
import com.example.inakdi.adapters.AdapterCategory
import com.example.inakdi.adapters.AdapterFasilitas
import com.example.inakdi.models.ModelCategory
import com.example.inakdi.models.ModelFasilitas
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private companion object {
        private const val TAG = "HOME_TAG"
    }

    private lateinit var mContext: Context
    private lateinit var fasilitasArrayList: ArrayList<ModelFasilitas>
    private lateinit var adapterFasilitas: AdapterFasilitas

    override fun onAttach(context: android.content.Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false)

        fasilitasArrayList = ArrayList()
        adapterFasilitas = AdapterFasilitas(mContext, fasilitasArrayList)
        binding.fasilitasRv.adapter = adapterFasilitas

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCategories()
        loadFasilitas("All")

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Query: $s")
                try {
                    val query = s.toString()
                    adapterFasilitas.filter.filter(query)
                } catch (e: Exception) {
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.faqIcon.setOnClickListener {
            showAlertDialog()
        }

        binding.allIcon.setOnClickListener {
            loadFasilitas("All")
        }

    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(mContext)
        val dialogView = layoutInflater.inflate(R.layout.faq_message, null)
        builder.setView(dialogView)
        builder.setCancelable(true) // Make the dialog cancellable

        val dialog = builder.create()
        dialog.show()

        // Find the close button and set its click listener
        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeBtn)
        closeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog when the close button is clicked
        }
    }


    private fun loadFasilitas(category: String) {
        Log.d(TAG, "loadFasilitas: category: $category")

        val ref = FirebaseDatabase.getInstance().getReference("Fasilitas Umum")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fasilitasArrayList.clear()
                for (ds in snapshot.children) {
                    try {
                        val modelFasilitas = ds.getValue(ModelFasilitas::class.java)
                        if (modelFasilitas != null) {
                            Log.d(TAG, "Fasilitas data: $modelFasilitas")
                            if (category == "All" || modelFasilitas.category == category) {
                                fasilitasArrayList.add(modelFasilitas)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }
                Log.d(TAG, "Total fasilitas: ${fasilitasArrayList.size}")
                adapterFasilitas.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ", error.toException())
            }
        })
    }

    private fun loadCategories() {
        val categoryArrayList = ArrayList<ModelCategory>()
        for (i in 0 until Utils.categories.size) {
            val modelCategory = ModelCategory(
                Utils.categories[i],
                Utils.categoryIcons[i],
                Utils.categoryColors[i]
            )
            categoryArrayList.add(modelCategory)
        }
        val adapterCategory = AdapterCategory(mContext, categoryArrayList, object :
            RvListenerCategory {
            override fun onCategoryClick(modelCategory: ModelCategory) {
                val selectedCategory = modelCategory.category
                Log.d(TAG, "Category clicked: $selectedCategory")
                loadFasilitas(selectedCategory)
            }
        })
        binding.categoriesRv.adapter = adapterCategory
    }
}
