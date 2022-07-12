package com.example.jello

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.jello.databinding.ActivitySignInSignUpBinding
import com.example.jello.fragments.SignInFragment
import com.example.jello.fragments.SignUpFragment

class SignInSignUpActivity : AppCompatActivity() {

    companion object{
        lateinit var binding: ActivitySignInSignUpBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarSign)
        when(intent.getStringExtra("type")){
            "signIn" -> swipeFragmentWithoutBack(SignInFragment())
            "signUp" -> swipeFragmentWithoutBack(SignUpFragment())
        }
        // to hide the title from toolbar
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun swipeFragmentWithoutBack(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container,fragment)
            .commit()
    }

}