package com.example.jello.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.jello.R
import com.example.jello.SignInSignUpActivity
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentSignUpBinding
import com.example.jello.user.model.User
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class SignUpFragment : Fragment() {

    private lateinit var binding:FragmentSignUpBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var dialog: ProgressBarGIFDialog.Builder
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        db = Firebase.firestore
        dialog = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        auth = FirebaseAuth.getInstance()
        binding.btnCreateAcc.setOnClickListener {
            // check the inputs is not empty and doesn't already exists
            if (validation()){
                dialog.build()
                var check = false
                db.collection("users").whereEqualTo("email",binding.txtEmailCreate.text.toString())
                    .get()
                    .addOnSuccessListener {querySnapshot->
                        for (document in querySnapshot){
                            if (binding.txtEmailCreate.text.toString() == document.get("email").toString()){
                                Common.dismissDialog(dialog)
                                Toast.makeText(requireContext(), "Email Already Used", Toast.LENGTH_LONG).show()
                                check = true
                                Common.alertUser(binding.txtEmailCreate,binding.txtErrorEmail,"Email Already Exists")
                                break
                            }else{
                                Common.dismissDialog(dialog)
                                check = false
                            }
                        }
                        if (!check){
                            addUser()
                            swipeFragment(SignInFragment())
                        }
                    }
                    .addOnFailureListener { it ->
                        Toast.makeText(requireContext(), "While Search Email", Toast.LENGTH_SHORT).show()
                    }
            }
        }
            // to appear the back button and navigate with it
        SignInSignUpActivity.binding.toolbarSign.setNavigationIcon(R.drawable.ic_arrow_back)
        SignInSignUpActivity.binding.toolbarSign.setNavigationOnClickListener { requireActivity().onBackPressed() }

        binding.btnSignInCreate.setOnClickListener {
            swipeFragment(SignInFragment())
        }
        return binding.root
    }

    private fun validation():Boolean{
            val email = Common.validItem(binding.txtEmailCreate,binding.txtErrorEmail,"Please Enter Valid Email",binding.txtEmailCreate.text.contains("@"))
            val pass = Common.validItem(binding.txtPasswordCreate,binding.txtErrorPass,"Please Enter Valid Password (10 or more)",binding.txtPasswordCreate.text.toString().length >= 10)
            val username = Common.validItem(binding.txtUsername,binding.txtUserError,"Please Enter Valid Username (5 or more)",binding.txtUsername.text.toString().length >= 5)
        return email&&pass&&username
    }

    private fun swipeFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container,fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun addUser(){
        auth.createUserWithEmailAndPassword(binding.txtEmailCreate.text.toString(),
        binding.txtPasswordCreate.text.toString())
            .addOnCompleteListener (requireActivity()){task->
                if (task.isSuccessful){
                    db.collection(FirestoreDB.usersCollection)
                        .add(User(auth.currentUser!!.uid,binding.txtUsername.text.toString(),binding.txtEmailCreate.text.toString()))
                        .addOnSuccessListener {
                            saveUserDoc(it.id)
                            Toast.makeText(requireContext(), "User add Successfully", Toast.LENGTH_SHORT).show()
                            Common.dismissDialog(dialog)
                            clearFields()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Create Account Filed", Toast.LENGTH_SHORT).show()
                            Common.dismissDialog(dialog)
                        }
                }else{
                    Toast.makeText(requireContext(), "Create Account Filed", Toast.LENGTH_SHORT).show()
                    Common.dismissDialog(dialog)
                }
            }
    }

    private fun clearFields(){
        binding.txtUsername.text.clear()
        binding.txtEmailCreate.text.clear()
        binding.txtPasswordCreate.text.clear()
    }

    private fun saveUserDoc(userDoc:String){
        val sharedPreferences = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userDoc",userDoc)
        editor.apply()
    }

}
