package com.example.jello.user.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.jello.R
import com.example.jello.SignInSignUpActivity
import com.example.jello.admin.AdminActivity
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentProfileBinding
import com.example.jello.user.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    lateinit var db:FirebaseFirestore
    lateinit var binding: FragmentProfileBinding
    private lateinit var dialog: ProgressBarGIFDialog.Builder
    lateinit var username:String
    lateinit var email:String
    lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        dialog = Common.showDialog(requireActivity(), R.drawable.loading_all, R.drawable.loading_all, "Loading ...", "Loading ...")
        getUserName()

        username = ""
        email = ""
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/fullhazemalkateb.appspot.com/o/user_profile.png?alt=media&token=81bf4134-99fe-4eaa-8f81-04eba7226970")
            .placeholder(R.drawable.placeholder_image)
            .resize(120,120)
            .into(binding.imgProfile)

        binding.editProfile.setOnClickListener {
            swipeFragment(EditUserProfileFragment())
        }
        binding.logout.setOnClickListener {
            logoutAndRemoveData()
        }
        return binding.root
    }

    private fun getUserName(){
        if (Common.checkNetwork(requireActivity())){
            dialog.build()
            val sharedPreferences = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
            val id = sharedPreferences.getString("userDoc","")
            db.collection(FirestoreDB.usersCollection).document(id!!)
                .get()
                .addOnSuccessListener {
                    val name = it.getString("username").toString()
                    binding.txtNameProfile.text = name
                    username = name
                    email = it.getString("email").toString()
                    if (username != "admin"){
                        UserActivity.binding.toolbar.visibility = View.GONE
                        UserActivity.binding.bottomNavUser.visibility = View.VISIBLE
                        binding.editProfile.visibility = View.VISIBLE
                        binding.divider.visibility = View.VISIBLE
                    }
                    Common.dismissDialog(dialog)
                }
                .addOnFailureListener {
                    Common.toastErrorNetwork(requireActivity())
                    Common.dismissDialog(dialog)
                }
        }else{
            Common.toastErrorNetwork(requireActivity())
        }
    }

    private fun swipeFragment(fragment: Fragment){
        // send user data to Edit his profile
        val b = Bundle()
        b.putString("username",username)
        b.putString("email",email)
        fragment.arguments = b
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer,fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun logoutAndRemoveData(){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Logout !")
        alertDialog.setMessage("You will need to log in again.")
        alertDialog.setIcon(R.drawable.ic_logout)
        alertDialog.setPositiveButton("OK"){
                d,_ ->
            val sharedPreferences = requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            auth.signOut()
            d.dismiss()
            val i = Intent(requireActivity(),SignInSignUpActivity::class.java)
            i.putExtra("type","signIn")
            requireActivity().startActivity(i)
            requireActivity().finish()
        }
        alertDialog.setNegativeButton("Cancel"){
                d,_ ->
            d.dismiss()
        }
        alertDialog.show()
    }
}