package com.example.jello.user

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jello.R
import com.example.jello.databinding.ActivityUserBinding
import com.example.jello.user.fragments.CartFragment
import com.example.jello.user.fragments.HomeFragment
import com.example.jello.user.fragments.ProfileFragment
import com.example.jello.user.fragments.SearchFragment

class UserActivity : AppCompatActivity() {
   companion object{
       lateinit var binding:ActivityUserBinding
   }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        swipeOnly(HomeFragment())


        binding.btnGoToSearch.setOnClickListener {
            if (binding.btnGoToSearch.text.isNotEmpty()){
                    val fragment = SearchFragment()
                    val b = Bundle()
                    b.putString("searchKey",binding.btnGoToSearch.text.toString())
                    fragment.arguments = b
                    swipeFragmentWithBack(fragment)
                binding.btnGoToSearch.text.clear()
                this.currentFocus?.let { view ->
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
        binding.bottomNavUser.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homeUser -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.root.setBackgroundResource(R.drawable.background)
                    swipeOnly(HomeFragment())
                }
                R.id.cart -> swipeFragment(CartFragment())
                R.id.profile -> swipeFragment(ProfileFragment())
            }
            return@setOnItemSelectedListener true
        }
    }
    private fun swipeFragment(fragment: Fragment){
        binding.toolbar.visibility = View.GONE
        binding.root.setBackgroundResource(R.color.white)
        supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer,fragment)
            .commit()
    }

    private fun swipeOnly(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer,fragment)
            .commit()
    }

    fun swipeFragmentWithBack(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer,fragment)
            .addToBackStack(null)
            .commit()
    }

}