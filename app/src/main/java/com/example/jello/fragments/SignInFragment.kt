package com.example.jello.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
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
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentSignInBinding
import com.example.jello.user.UserActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        SignInSignUpActivity.binding.toolbarSign.setNavigationIcon(R.drawable.ic_arrow_back)
        SignInSignUpActivity.binding.toolbarSign.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.btnSignUp.setOnClickListener {
            swipeFragment(SignUpFragment())
        }

        binding.btnLogin.setOnClickListener {
            loginProcess(it)
        }
        binding.btnForgetPass.setOnClickListener {
            swipeFragment(ForgotPassFragment())
        }
        return binding.root
    }

    private fun swipeFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun validation(): Boolean {
        val email = Common.validItem(binding.txtEmail, binding.txtErrorEmail, "Please Enter Valid Email",
            binding.txtEmail.text.contains("@"))
        val pass = Common.validItem(binding.txtPassword, binding.txtErrorPass,
            "Please Enter Valid Password (10 or more)", binding.txtPassword.text.toString().length >= 10)
        return email && pass
    }

    private fun alertUser(editText1: EditText, editText2: EditText, txtError1: TextView, txtError2: TextView,
        txt: String) {
        txtError1.visibility = View.VISIBLE
        txtError1.text = txt
        txtError2.visibility = View.VISIBLE
        txtError2.text = txt
        editText1.setBackgroundResource(R.drawable.text_field_error)
        editText2.setBackgroundResource(R.drawable.text_field_error)
    }

    private fun clearFields() {
        binding.txtPassword.text.clear()
        binding.txtEmail.text.clear()
    }

    private fun saveUserIdInSharedPreferences(userId: String, userType: String,docId:String,isLogin:Boolean) {
        val sharePreferences = requireActivity().getSharedPreferences("UserInfo", MODE_PRIVATE)
        val editor = sharePreferences.edit()
        editor.putString("userId", userId)
        editor.putString("userType", userType)
        editor.putBoolean("isLogin",isLogin)
        editor.putString("userDoc",docId)
        if (editor.commit()){
            goToActivity()
        }
    }

    private fun loginProcess(view: View) {
        if (validation()) {
            Snackbar.make(view,"Logging...",Snackbar.LENGTH_INDEFINITE).show()
            auth.signInWithEmailAndPassword(binding.txtEmail.text.toString(),
                binding.txtPassword.text.toString())
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        if (auth.currentUser!!.email.toString() == "admin@gmail.com") {
                            checkBoxAndSaveUserInfo("admin@gmail.com","admin")
                            Snackbar.make(view,"Login Successfully",Snackbar.LENGTH_SHORT).show()
                        } else {
                            checkBoxAndSaveUserInfo(auth.currentUser!!.email.toString(),"user")
                            Snackbar.make(view,"Login Successfully",Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(view,"Username or password is incorrect",Snackbar.LENGTH_SHORT).show()
                        alertUser(binding.txtEmail, binding.txtPassword, binding.txtErrorEmail, binding.txtErrorPass, "Check this field")
                    }
                }
        }
    }

    private fun goToActivity() {
        val sharePreferences = requireActivity().getSharedPreferences("UserInfo", MODE_PRIVATE)
        if (sharePreferences.getString("userType", "") == "admin") {
            Toast.makeText(requireContext(), "Admin", Toast.LENGTH_SHORT).show()
            clearFields()
            val i = Intent(requireContext(), AdminActivity::class.java)
            requireActivity().startActivity(i)
            requireActivity().finish()
        } else {
            val i = Intent(requireContext(), UserActivity::class.java)
            clearFields()
            requireActivity().startActivity(i)
            requireActivity().finish()
        }
    }

    private fun checkBoxAndSaveUserInfo(email: String,userType: String){
        var docId = ""
        db.collection(FirestoreDB.usersCollection)
            .whereEqualTo("email",email)
            .get()
            .addOnSuccessListener {
                for (i in it){
                    docId = i.id
                }
                if (binding.checkBoxRemem.isChecked) {
                    saveUserIdInSharedPreferences(auth.currentUser!!.uid, userType,docId,true)
                } else {
                    saveUserIdInSharedPreferences(auth.currentUser!!.uid,userType,docId,false)
                }
            }
            .addOnFailureListener {

            }
    }


}