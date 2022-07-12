package com.example.jello

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.jello.adapter.ViewPagerAdapter
import com.example.jello.admin.AdminActivity
import com.example.jello.databinding.ActivityMainBinding
import com.example.jello.model.Welcome
import com.example.jello.user.UserActivity

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE)
        // check if user or admin is already login
        if (sharedPreferences.getBoolean("isLogin",false)){
            // go to user or admin page
            when(sharedPreferences.getString("userType","")){
                "user" -> goToMain(UserActivity())
                "admin" -> goToMain(AdminActivity())
            }
        }else{
            val data = ArrayList<Welcome>()
            data.add(Welcome(R.drawable.ilustration,"ORIGINAL PRODUCT","Original with 1000 product from a lot of  different brand across the world. buy so easy with just simple step then your item will send it."))
            data.add(Welcome(R.drawable.ilustration_2,"FREE SHIPPING","We offer free delivery to all countries supported in the application, and this includes products whose price reaches 100\$ or more."))
            data.add(Welcome(R.drawable.ilustration_3,"FAST DELIVERY","We guarantee you quick access to the product at all times of work, with care and preservation."))

            val viewPagerAdapter = ViewPagerAdapter(this,data)

            binding.viewPager.adapter = viewPagerAdapter

            binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when(position){
                        0 -> {
                            changeDots(R.drawable.dot,R.drawable.dot_disabled,R.drawable.dot_disabled)
                        }
                        1-> {
                            changeDots(R.drawable.dot_disabled,R.drawable.dot,R.drawable.dot_disabled)
                        }
                        2->{
                            changeDots(R.drawable.dot_disabled,R.drawable.dot_disabled,R.drawable.dot)
                        }
                    }
                }
            })

            binding.btnSignInMain.setOnClickListener {
                val i = Intent(this,SignInSignUpActivity::class.java)
                i.putExtra("type","signIn")
                startActivity(i)
                finish()
            }
            binding.btnSignUpMain.setOnClickListener {
                val i = Intent(this,SignInSignUpActivity::class.java)
                i.putExtra("type","signUp")
                startActivity(i)
                finish()
            }
        }

    }

    private fun changeDots(dot1:Int,dot2:Int,dot3:Int){
        binding.dotOne.setImageResource(dot1)
        binding.dotTwo.setImageResource(dot2)
        binding.dotThree.setImageResource(dot3)
    }

    private fun goToMain(activity: Activity) {
        val i = Intent(this,activity::class.java)
        startActivity(i)
        finish()
    }
}