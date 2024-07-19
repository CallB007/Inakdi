package com.example.inakdi.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.inakdi.R
import com.example.inakdi.Utils
import com.example.inakdi.databinding.ActivityMainBinding
import com.example.inakdi.fragments.AccountFragment
import com.example.inakdi.fragments.BeritaFragment
import com.example.inakdi.fragments.FaqFragment
import com.example.inakdi.fragments.HomeFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //Firebase Auth for auth related task
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml = ActivityMainBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get instance of firebase auth for auth related task
        firebaseAuth = FirebaseAuth.getInstance()
        //check if user is logged in or not
        if (firebaseAuth.currentUser == null){
            //user not logged in, move to LoginOptionsActivity
            startLoginOptions()
        }

        showHomeFragment()

        binding.bottomNv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    //Home clicked, show HomeFragments
                    showHomeFragment()
                    true
                }

                R.id.menu_berita -> {
                    //Berita clicked, show BeritaFragments

                    showBeritaFragment()
                    true
                }

                R.id.menu_profile -> {
                    //Account clicked, show AccountFragments
                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this, "Masuk terlebih dahulu")
                        startLoginOptions()

                        false
                    } else {
                        showAccountFragment()
                        true
                    }
                }
                else -> {
                    false
                }
            }
        }

        binding.tambahFasilitasFab.setOnClickListener {
                //Account clicked, show AccountFragments
                if (firebaseAuth.currentUser == null) {
                    Utils.toast(this, "Masuk terlebih dahulu")
                    startLoginOptions()

                    false
                } else {
                    startActivity((Intent(this, TambahFasilitasActivity::class.java)))
                    true
                }
            }

        binding.tambahBeritaFab.setOnClickListener {
            //Account clicked, show AccountFragments
            if (firebaseAuth.currentUser == null) {
                Utils.toast(this, "Masuk terlebih dahulu")
                startLoginOptions()

                false
            } else {
                startActivity((Intent(this, TambahBeritaActivity::class.java)))
                true
            }
        }
    }

    //FAQ FRAGMENT
    private fun showFaqFragment(){
        binding.toolbarTitleTv.text = "Bantuan"

        //Show Fragment
        val fragment = FaqFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentsFl, fragment, "FaqFragment")
        fragmentTransaction.commit()
    }

    //HOME FRAGMENT
    private fun showHomeFragment(){
        binding.toolbarTitleTv.text = "Home"

        //Show Fragment
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentsFl, fragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    //BERITA FRAGMENT
    private fun showBeritaFragment(){
        binding.toolbarTitleTv.text = "Berita"

        //Show Fragment
        val fragment = BeritaFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentsFl, fragment, "BeritaFragment")
        fragmentTransaction.commit()
    }

    //ACCOUNT FRAGMENT
    private fun showAccountFragment(){
        binding.toolbarTitleTv.text = "Account"

        //Show Fragment
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentsFl, fragment, "AccountFragment")
        fragmentTransaction.commit()
    }

    private fun startLoginOptions(){
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }
}