package com.example.jello.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jello.R
import com.example.jello.SignInSignUpActivity
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentEditUserProfileBinding
import com.example.jello.fragments.ForgotPassFragment
import com.example.jello.fragments.SignInFragment
import com.example.jello.user.UserActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class EditUserProfileFragment : Fragment() {
    lateinit var binding: FragmentEditUserProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: ProgressBarGIFDialog.Builder
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        dialog = Common.showDialog(
            requireActivity(),
            R.drawable.loading_all,
            R.drawable.loading_all,
            "Loading ...",
            "Loading ..."
        )
        // data from profile
        val username = requireArguments().getString("username")
        val email = requireArguments().getString("email")
        // -------------------------------------------------------------------------
        UserActivity.binding.toolbar.visibility = View.VISIBLE
        UserActivity.binding.toolbar.setBackgroundResource(R.color.toolbar_visible)
        UserActivity.binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        UserActivity.binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        UserActivity.binding.bottomNavUser.visibility = View.GONE
        UserActivity.binding.btnGoToSearch.visibility = View.GONE
        // -------------------------------------------------------------------------
        if (username != null && email != null) {
            binding.txtNewEditUsername.setText(username)
            binding.txtNewEditEmail.setText(email)
        }
        binding.btnSaveNewEdit.setOnClickListener {
            if (validation()) {
                updateProfile(it, binding.txtNewEditEmail.text.toString()
                    , binding.txtNewEditUsername.text.toString())
            }
        }
        binding.btnGoToEditPass.setOnClickListener {
            swipeFragment(ForgotPassFragment())
        }
        return binding.root
    }

    private fun validation(): Boolean {
        val username = Common.validItem(
            binding.txtNewEditUsername,
            binding.editErrorUsername,
            "Please Enter Valid Username (5 or more)"
        )
        val email = Common.validItem(
            binding.txtNewEditEmail,
            binding.editErrorEmail,
            "Please Enter Valid Email",
            binding.txtNewEditEmail.text.contains("@")
        )
        return username && email
    }

    private fun updateProfile(view: View, email: String, username: String) {
        dialog.build()
        var check = false
        db.collection("users").whereEqualTo("email", binding.txtNewEditEmail.text.toString())
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    if (binding.txtNewEditEmail.text.toString() == document.get("email").toString()) {
                        Common.dismissDialog(dialog)
                        check = true
                        Common.alertUser(binding.txtNewEditEmail, binding.editErrorEmail, "Email Already Exists")
                        break
                    } else {
                        check = false
                    }
                }
                if (!check) {
                    if (auth.currentUser != null) {
                        auth.currentUser!!.updateEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    updateProfileFirestore(view, email, username)
                                } else {
                                    Common.dismissDialog(dialog)
                                    Snackbar.make(view, "Try again later...", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(requireContext(), "No User", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error While Search Email", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileFirestore(view: View, email: String, username: String) {
        val user = hashMapOf<String, Any>()
        user["email"] = email
        user["username"] = username
        val sharedPreferences =
            requireActivity().getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val docId = sharedPreferences.getString("userDoc", "")
        db.collection(FirestoreDB.usersCollection).document(docId!!)
            .update(user)
            .addOnSuccessListener {
                Common.dismissDialog(dialog)
                Snackbar.make(view, "Profile Updated Successfully", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Common.dismissDialog(dialog)
                Snackbar.make(view, "Error while Updating ...", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun swipeFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}