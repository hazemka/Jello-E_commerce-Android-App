package com.example.jello.user.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jello.R
import com.example.jello.admin.adapter.ProductAdminAdapter
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentSearchBinding
import com.example.jello.user.UserActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {
    lateinit var db: FirebaseFirestore
    private lateinit var dialog: ProgressBarGIFDialog.Builder
    private lateinit var binding: FragmentSearchBinding
    private lateinit var mutableList:MutableList<String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater,container,false)

        db = Firebase.firestore
        dialog = Common.showDialog(requireActivity(), R.drawable.loading_all, R.drawable.loading_all,"Loading ...","Loading ...")

        UserActivity.binding.btnGoToSearch.visibility = View.GONE
        UserActivity.binding.bottomNavUser.visibility = View.GONE
        UserActivity.binding.toolbar.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        UserActivity.binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        // from user Activity :
        val searchKey = requireArguments().getString("searchKey")
        binding.txtSearch.text = "Search for $searchKey"

        val keyCapital = searchKey!!.uppercase()
        val keySmall = searchKey.lowercase()
        mutableList = mutableListOf(searchKey, keyCapital, keySmall)

        search(searchKey)

        return binding.root
    }

    private fun search(searchKey:String){
        dialog.build()
        val data = ArrayList<Product>()
        db.collection(FirestoreDB.categoryCollection)
            .get()
            .addOnSuccessListener { categories ->
                for (category in categories) {
                    db.collection(FirestoreDB.categoryCollection).document(category.id)
                        .collection("products")
                        .whereIn("name", mutableList).get()
                        .addOnSuccessListener { products ->
                            for (product in products) {
                                data.add(
                                    Product(
                                        product.id,
                                        product.getString("name")!!,
                                        product.getString("description")!!,
                                        product.getString("image")!!,
                                        product.getDouble("price")!!,
                                        product.getDouble("rate")!!,
                                        product.getDouble("latitude")!!,
                                        product.getDouble("longitude")!!,
                                        product.get("numberOfBuyers").toString().toInt(),
                                        category.id
                                    )
                                )
                            }
                            val adapter = ProductAdminAdapter(requireActivity(), data)
                            binding.rvSearch.layoutManager = GridLayoutManager(requireContext(), 2)
                            binding.rvSearch.adapter = adapter
                            Common.dismissDialog(dialog)
                        }
                        .addOnFailureListener {
                            Common.dismissDialog(dialog)
                        }
                }
            }
            .addOnFailureListener {
                Common.toastErrorNetwork(requireActivity())
            }
    }
}