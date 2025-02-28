package com.example.inakdi.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.adapters.AdapterImageSlider
import com.example.inakdi.databinding.ActivityBeritaDetailsBinding
import com.example.inakdi.models.ModelBerita
import com.example.inakdi.models.ModelImageSlider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BeritaDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBeritaDetailsBinding

    private companion object {
        private const val TAG = "BERITA_DETAILS_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private var beritaId = ""

    private var adminUid = ""
    private var adminPhone = ""

    private lateinit var imageSliderArrayList: ArrayList<ModelImageSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeritaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Dapatkan beritaId dari intent
        beritaId = intent.getStringExtra("beritaId").toString()
        Log.d(TAG, "onCreate: beritaId : $beritaId")

        loadBeritaImages()
        loadBeritaDetails()

        // Set onClickListeners untuk tombol-tombol
        binding.toolBarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarDeleteBtn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Berita")
                .setMessage("Yakin ingin Menghapus Berita ?")
                .setPositiveButton("Hapus") { dialog, which ->
                    Log.d(TAG, "onCreate: Memilih Menghapus")
                    deleteBerita()
                }
                .setNegativeButton("Batal") { dialog, which ->
                    Log.d(TAG, "onCreate: Membatalkan Menghapus")
                    dialog.dismiss()
                }
                .show()
        }

        binding.toolbarEditBtn.setOnClickListener {
            //NEW !!
            val intent = Intent(this, TambahBeritaActivity::class.java)
            intent.putExtra("editMode", true)
            intent.putExtra("beritaId", beritaId)
            intent.putExtra("title", binding.titleTv.text.toString())
            intent.putExtra("description", binding.descriptionTv.text.toString())
            // Tambahkan intent untuk daftar gambar jika diperlukan
            startActivity(intent)
        }

        binding.adminProfileCv.setOnClickListener {
            // TODO: Implement admin profile click functionality
        }

        binding.callBtn.setOnClickListener {
            Utils.callIntent(this, adminPhone)
        }

        binding.smsBtn.setOnClickListener {
            Utils.smsIntent(this, adminPhone)
        }

        binding.waBtn.setOnClickListener {
            Utils.waIntent(this, adminPhone)
        }

        checkUserStatus()
    }

    private fun loadBeritaDetails() {
        Log.d(TAG, "loadBeritaDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Berita")
        ref.child(beritaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: Snapshot exists: ${snapshot.exists()}")
                    Log.d(TAG, "onDataChange: Snapshot value: ${snapshot.value}")

                    if (snapshot.exists()) {
                        try {
                            val modelBerita = snapshot.getValue(ModelBerita::class.java)
                            Log.d(TAG, "onDataChange: ModelBerita: $modelBerita")

                            if (modelBerita == null) {
                                Log.e(TAG, "onDataChange: ModelBerita is null")
                                Utils.toast(this@BeritaDetailsActivity, "Failed to load facility details")
                                return
                            }

                            // Proses lebih lanjut dengan modelBerita
                            binding.titleTv.text = modelBerita.title
                            binding.descriptionTv.text = modelBerita.description
                            binding.dateTv.text = Utils.formatTimestampDate(modelBerita.timestamp)

                            // Set adminUid dan load admin details
                            adminUid = modelBerita.uid
                            loadAdminDetails()

                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChange: Exception occurred", e)
                            Utils.toast(this@BeritaDetailsActivity, "An error occurred while loading details")
                        }
                    } else {
                        Log.e(TAG, "onDataChange: Data does not exist")
                        Utils.toast(this@BeritaDetailsActivity, "Facility data does not exist")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: DatabaseError", error.toException())
                    Utils.toast(this@BeritaDetailsActivity, "Failed to load facility details: ${error.message}")
                }
            })
    }


    private fun loadAdminDetails() {
        Log.d(TAG, "loadAdminDetails: ")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(adminUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = snapshot.child("timestamp").value as Long

                    val formattedDate = Utils.formatTimestampDate(timestamp)

                    adminPhone = "$phoneCode$phoneNumber"

                    binding.adminNameTv.text = name
                    binding.memberSinceTv.text = formattedDate
                    try {
                        Glide.with(this@BeritaDetailsActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile)
                            .into(binding.adminProfileIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }

                    // Set visibility berdasarkan status login dan adminUid
                    setButtonVisibility()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ", error.toException())
                }
            })
    }

    private fun loadBeritaImages() {
        Log.d(TAG, "loadBeritaImages: ")

        imageSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Berita")
        ref.child(beritaId).child("Images")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    imageSliderArrayList.clear()

                    for (ds in snapshot.children) {
                        try {
                            val modelImageSlider = ds.getValue(ModelImageSlider::class.java)
                            modelImageSlider?.let { imageSliderArrayList.add(it) }
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }

                    val adapterImageSlider = AdapterImageSlider(this@BeritaDetailsActivity, imageSliderArrayList)
                    binding.imageSliderVp.adapter = adapterImageSlider
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ", error.toException())
                }
            })
    }

    private fun deleteBerita() {
        Log.d(TAG, "deleteBerita: ")

        val ref = FirebaseDatabase.getInstance().getReference("Berita")
        ref.child(beritaId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "deleteBerita: Deleted")
                Utils.toast(this, "Berita Berhasil Dihapus")
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteBerita: ", e)
                Utils.toast(this, "Gagal Menghapus Berita. Error ${e.message}")
            }
    }

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // Pengguna belum login
            binding.callBtn.visibility = View.VISIBLE
            binding.waBtn.visibility = View.VISIBLE
            binding.smsBtn.visibility = View.VISIBLE
            binding.toolbarEditBtn.visibility = View.GONE
            binding.toolbarDeleteBtn.visibility = View.GONE
        } else {
            // Pengguna sudah login
            binding.callBtn.visibility = View.GONE
            binding.waBtn.visibility = View.GONE
            binding.smsBtn.visibility = View.GONE
            binding.toolbarEditBtn.visibility = View.VISIBLE
            binding.toolbarDeleteBtn.visibility = View.VISIBLE
        }
    }

    private fun setButtonVisibility() {
        if (adminUid == firebaseAuth.currentUser?.uid) {
            // Pengguna adalah admin
            binding.callBtn.visibility = View.GONE
            binding.waBtn.visibility = View.GONE
            binding.smsBtn.visibility = View.GONE
        } else {
            // Pengguna bukan admin
            binding.callBtn.visibility = View.VISIBLE
            binding.waBtn.visibility = View.VISIBLE
            binding.smsBtn.visibility = View.VISIBLE
        }
    }
}
