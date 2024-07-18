package com.example.inakdi.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.inakdi.Utils
import com.example.inakdi.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    private companion object {
        private const val TAG = "LOGIN_OPTION_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    //ProgressDialog to show while sign in
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_login_email.xml = ActivityLoginEmailBinding
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get instance of firebase auth for auth related task
        firebaseAuth = FirebaseAuth.getInstance()

        //init setup ProgressDialog to show while sign in
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu Sebentar ...")
        progressDialog.setCanceledOnTouchOutside(false)

        //tombol Back, toolbarBackBtn click, go back
        binding.toolBarBackBtn.setOnClickListener{
            onBackPressed()
        }

        //tombol Text untuk daftar
        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        //tombol Login, click validate account
        binding.loginBtn.setOnClickListener{
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    //VALIDASI DATA AKUN (Data Account)
    private fun validateData(){
        //input Data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "validateData: password: $password")

        //Logika validate Data Account
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //jika format email tidak benar maka muncul peringatan
            binding.emailEt.error = "Email yang dimasukkan salah"
            binding.emailEt.requestFocus()
        } else if (password.isEmpty()){
            //jika password belum diisi maka muncul peringatan
            binding.passwordEt.error = "Masukkan Password"
            binding.passwordEt.requestFocus()
        } else{
            //format email dan password benar, maka lanjut login
            loginUser()
        }
    }

    private fun loginUser(){
        Log.d(TAG, "loginUser: ")
        //show progress
        progressDialog.setMessage("Sedang diproses Masuk ...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //user login succes
                Log.d(TAG, "loginUser: Berhasil Masuk")
                progressDialog.dismiss()
                //diarahkan ke MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
                Utils.toast(this, "Berhasil Masuk")
            }
            .addOnFailureListener{e ->
                //user login failed
                Log.e(TAG, "loginUser: ", e)
                progressDialog.dismiss()

                Utils.toast(this, "Gagal Login. Error : ${e.message}")
            }
    }
}