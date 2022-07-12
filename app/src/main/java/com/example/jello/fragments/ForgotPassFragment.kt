package com.example.jello.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jello.R
import com.example.jello.SignInSignUpActivity
import com.example.jello.admin.AdminActivity
import com.example.jello.common.Common
import com.example.jello.databinding.FragmentForgotPassBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.fragments.EditUserProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class ForgotPassFragment : Fragment() {
    private lateinit var dialog: ProgressBarGIFDialog.Builder
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentForgotPassBinding
    private lateinit var auth: FirebaseAuth
    var check = false
    lateinit var sharePreferences: SharedPreferences
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentForgotPassBinding.inflate(inflater, container, false)
        dialog = Common.showDialog(requireActivity(), R.drawable.loading_all, R.drawable.loading_all, "Loading ...", "Loading ...")
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        sharePreferences = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)

        if (sharePreferences.getString("userType","") == "user"){
            UserActivity.binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            UserActivity.binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
            UserActivity.binding.toolbar.setBackgroundResource(R.color.white)
        }else{
            SignInSignUpActivity.binding.toolbarSign.setNavigationIcon(R.drawable.ic_arrow_back)
            SignInSignUpActivity.binding.toolbarSign.setNavigationOnClickListener { requireActivity().onBackPressed() }
        }

        binding.btnResetPass.setOnClickListener {
            if (validation()) {
                dialog.build()
                db.collection("users").whereEqualTo("email", binding.txtEmailReset.text.toString())
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot) {
                            if (binding.txtEmailReset.text.toString() == document.get("email").toString()) {
                                sendEmailForRestPass()
                                check = true
                                break
                            }else{
                                check =false
                            }
                        }
                        if (!check){
                            Common.dismissDialog(dialog)
                            Common.alertUser(binding.txtEmailReset,binding.txtErrorEmailReset,"Email doesn't exist")
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Please Check Internet Connection", Toast.LENGTH_SHORT).show()
                    }

            }
        }
        return binding.root
}

    private fun validation(): Boolean {
        return Common.validItem(
            binding.txtEmailReset,
            binding.txtErrorEmailReset,
            "Please Enter Valid Email",
            binding.txtEmailReset.text.contains("@")
        )
    }

    private fun swipeFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun sendEmailForRestPass(){
        val emailAddress = binding.txtEmailReset.text.toString()
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    check = true
                    Common.dismissDialog(dialog)
                    Toast.makeText(requireContext(), "Email Send", Toast.LENGTH_SHORT).show()
                    if (sharePreferences.getString("userType","") == "user"){

                    }else{
                        swipeFragment(SignInFragment())
                    }
                    binding.txtEmailReset.text.clear()
                } else {
                    Common.dismissDialog(dialog)
                    Toast.makeText(requireContext(), "Please Check Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
    }
}