package com.example.inakdi.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.inakdi.databinding.ActivityLoginOptionsBinding

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBtn.setOnClickListener{
            onBackPressed()
        }

        binding.loginBtn.setOnClickListener{
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.signupBtn.setOnClickListener{
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }
    }
}