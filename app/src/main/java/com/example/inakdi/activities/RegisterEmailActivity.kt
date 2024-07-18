package com.example.inakdi.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.inakdi.Utils
import com.example.inakdi.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG = "REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Menunggu ...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolBarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.haveAccountTv.setOnClickListener{
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.registerBtn.setOnClickListener{
            validateData()
        }
    }

    private var email = ""
    private var password = ""
    private var cPassword = ""

    private fun validateData(){
        // Input Data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        cPassword = binding.cPasswordEt.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "validateData: password: $password")
        Log.d(TAG, "validateData: confirm password: $cPassword")

        //Logika validate Data Account
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //jika format email tidak benar maka muncul peringatan
            binding.emailEt.error = "Format Email salah"
            binding.emailEt.requestFocus()
        } else if (password.isEmpty()){
            //jika password tidak diisi maka muncul peringatan
            binding.passwordEt.error = "Masukkan Password"
            binding.passwordEt.requestFocus()
        } else if (cPassword.isEmpty()){
            //jika passowrd tidak dimasukkan ulang maka muncul peringatan
            binding.cPasswordEt.error = "Masukkan ulang Password"
            binding.cPasswordEt.requestFocus()
        } else if (password != cPassword) {
            //jika  password tidak sama maka muncul peringatan
            binding.cPasswordEt.error = "Password harus sama"
            binding.cPasswordEt.requestFocus()
        } else {
            //format email dan password benar, maka lanjut login
            registerUser()
        }
    }

    private fun registerUser(){
        Log.d(TAG, "registerUser: ")
        // Show Progress
        progressDialog.setMessage("Membuat Akun")
        progressDialog.show()

        // Start user sign-up
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // User register success, need to save user into firebase
                Log.d(TAG, "registerUser: Pendaftaran Berhasil")
                updateUserInfo()
            }
            .addOnFailureListener{ e ->
                // User register failed
                Log.e(TAG, "registerUser: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to create account due to ${e.message}")
            }
    }

    private fun updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ")
        // Change progress dialog message
        progressDialog.setMessage("Menyimpan data pendaftaran akun")

        val timestamp = Utils.getTimestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        if (registeredUserEmail == null || registeredUserUid == null) {
            Log.e(TAG, "updateUserInfo: Firebase user is null")
            progressDialog.dismiss()
            Utils.toast(this, "Gagal menyimpan data akun")
            return
        }

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["userType"] = "Email"
        hashMap["timestamp"] = timestamp
        hashMap["email"] = registeredUserEmail
        hashMap["uid"] = registeredUserUid

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid)
            .setValue(hashMap)
            .addOnSuccessListener {
                // Firebase db save success
                Log.d(TAG, "updateUserInfo: Akun terdaftar")
                progressDialog.dismiss()

                // Start Main activity
                startActivity(Intent(this, LoginEmailActivity::class.java))
                finishAffinity()
                Utils.toast(this, "Berhasil Mendaftarkan Akun")
            }
            .addOnFailureListener{e ->
                // Firebase db save failed
                Log.e(TAG, "updateUserInfo: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Gagal menyimpan data akun. Error : ${e.message}")
            }
    }
}
