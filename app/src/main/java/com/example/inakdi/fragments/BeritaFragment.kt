package com.example.inakdi.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.inakdi.adapters.AdapterBerita
import com.example.inakdi.databinding.FragmentBeritaBinding
import com.example.inakdi.models.ModelBerita
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BeritaFragment : Fragment() {

    private lateinit var binding: FragmentBeritaBinding

    private companion object {
        private const val TAG = "HOME_TAG"
    }

    private lateinit var mContext: Context
    private lateinit var beritaArrayList: ArrayList<ModelBerita>
    private lateinit var adapterBerita: AdapterBerita

    override fun onAttach(context: android.content.Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBeritaBinding.inflate(LayoutInflater.from(mContext), container, false)

        beritaArrayList = ArrayList()
        adapterBerita = AdapterBerita(mContext, beritaArrayList)
        binding.beritaRv.adapter = adapterBerita

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadBerita("All")

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Query: $s")
                try {
                    val query = s.toString()
                    filterBerita(query)
                } catch (e: Exception) {
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadBerita(category: String) {
        Log.d(TAG, "loadBerita: category: $category")

        val ref = FirebaseDatabase.getInstance().getReference("Berita")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                beritaArrayList.clear()
                for (ds in snapshot.children) {
                    try {
                        val modelBerita = ds.getValue(ModelBerita::class.java)
                        if (modelBerita != null) {
                            beritaArrayList.add(modelBerita)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }
                Log.d(TAG, "Total berita: ${beritaArrayList.size}")
                adapterBerita.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ", error.toException())
            }
        })
    }


    private fun filterBerita(query: String) {
        val filteredList = ArrayList<ModelBerita>()
        for (berita in beritaArrayList) {
            if (berita.title.contains(query, true) || berita.description.contains(query, true)) {
                filteredList.add(berita)
            }
        }
        adapterBerita.beritaArrayList = filteredList
        adapterBerita.notifyDataSetChanged()
    }
}
