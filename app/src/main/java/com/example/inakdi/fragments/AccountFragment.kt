package com.example.inakdi.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.inakdi.activities.MainActivity
import com.example.inakdi.activities.ProfileEditActivity
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    private companion object {
        private const val TAG = "ACCOUNT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mContext: Context

    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context) {

        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Mohon Tunggu Sebentar ...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadMyInfo()

        //BUTTON LOGOUT
        binding.logoutCv.setOnClickListener{
            firebaseAuth.signOut()//logout
            // Balik lagi ke main activity
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }

        // BUTTON EDIT
        binding.editProfileCv.setOnClickListener{
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }


        binding.verifyAccountCv.setOnClickListener{
            verifyAccount()
        }
    }

    private fun loadMyInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val phone = phoneCode+phoneNumber

                    //Avoid Null or format execptions
                    if (timestamp == "null"){
                        timestamp = "0"
                    }

                    //format timestamp to dd/MM/yyyy
                    val formattedDate = Utils.formatTimestampDate(timestamp.toLong())

                    //set Data to UI
                    binding.emailTv.text = email
                    binding.nameTv.text = name
                    binding.numberTv.text = phone
                    binding.memberSinceTv.text = formattedDate

                    //Logika untuk cek user untuk memverifikasi status akun
                    if (userType == "Email"){
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified){
                            //user terverifikasi
                            binding.verifyAccountCv.visibility = View.GONE
                            binding.verifTv.text = "Terverifikasi"
                        }
                        else{
                            //user belum terverifikasi
                            binding.verifyAccountCv.visibility = View.VISIBLE
                            binding.verifTv.text = " Belum Terverifikasi"
                        }
                    }
                    else{
                        binding.verifyAccountCv.visibility = View.GONE
                        binding.verifTv.text = "Terverifikasi"
                    }

                    //set profile image to profileTv
                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.profile)
                            .into(binding.profileTv)
                    }
                    catch (e : Exception){
                        Log.e(TAG, "onDataChange: ", e )
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun verifyAccount(){
        Log.d(TAG, "verifyAccount: ")

        progressDialog.setMessage("Instruksi untuk verifikasi akun sudah dikirim pada email ...")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener {
                //Berhasil mengirimkan instuksi
                Log.d(TAG, "verifyAccount: Berhasil mengirimkan instruksi verifikasi")
                progressDialog.dismiss()
                Utils.toast(mContext, "Instruksi untuk verifikasi akun sudah dikirim pada email")
            }
            .addOnFailureListener{e ->
                //Gagal mengirimkan instuksi
                Log.e(TAG, "verifyAccount: ", e)
                progressDialog.dismiss()
                Utils.toast(mContext, "Gagal mengirim instruksi verifikasi. Error ${e.message}")
            }
    }

}