package com.example.jello.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.jello.R
import com.example.jello.admin.fragments.CategoryFragment
import com.example.jello.admin.fragments.HomeFragment
import com.example.jello.databinding.ActivityAdminBinding
import com.example.jello.user.fragments.ProfileFragment

class AdminActivity : AppCompatActivity() {
    companion object{
        lateinit var binding:ActivityAdminBinding
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBarAdmin)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        swipeFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                    R.id.home -> swipeFragment(HomeFragment())
                    R.id.category -> swipeFragment(CategoryFragment())
                    R.id.profileAdmin -> swipeFragment(ProfileFragment())
            }
            return@setOnItemSelectedListener true
        }
    }


    fun swipeFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminContainer,fragment)
            .commit()
    }

    fun swipeFragmentWithBack(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.adminContainer,fragment)
            .commit()
    }
}