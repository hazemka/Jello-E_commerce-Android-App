package com.example.jello.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jello.R
import com.example.jello.admin.model.Category
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentSeeAllCategoryBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.adapter.AllCategoryAdapter
import com.example.jello.user.adapter.HorizontalScrollCategoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class SeeAllCategoryFragment : Fragment() {
    lateinit var binding:FragmentSeeAllCategoryBinding
    lateinit var db:FirebaseFirestore
    private lateinit var dialog: ProgressBarGIFDialog.Builder
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSeeAllCategoryBinding.inflate(inflater,container,false)
        UserActivity.binding.toolbar.visibility = View.GONE
        binding.root.setBackgroundResource(R.color.white)
        db = Firebase.firestore
        dialog = Common.showDialog(requireActivity(), R.drawable.loading_all, R.drawable.loading_all,"Loading ...","Loading ...")

        UserActivity.binding.bottomNavUser.visibility = View.GONE
        UserActivity.binding.toolbar.visibility = View.VISIBLE
        UserActivity.binding.btnGoToSearch.visibility = View.GONE
        UserActivity.binding.appBarLayout.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        UserActivity.binding.toolbar.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        getCategories()
        return binding.root
    }

    private fun getCategories(){
        dialog.build()
        val data = ArrayList<Category>()
        db.collection(FirestoreDB.categoryCollection).orderBy(FirestoreDB.categoryNameField)
            .get()
            .addOnSuccessListener {
                for (document in it){
                    data.add(Category(document.id,document.get(FirestoreDB.categoryNameField).toString()))
                }
                Common.dismissDialog(dialog)
                binding.rvAllCategory.layoutManager = GridLayoutManager(requireContext(),2)
                val adapter = AllCategoryAdapter(requireContext(),data)
                binding.rvAllCategory.adapter = adapter
            }
            .addOnFailureListener {
                Common.dismissDialog(dialog)
                Toast.makeText(requireContext(), "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }
}