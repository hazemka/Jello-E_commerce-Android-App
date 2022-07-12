package com.example.jello.admin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.adapter.ProductAdminAdapter
import com.example.jello.admin.model.Category
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentHomeBinding
import com.example.jello.user.adapter.HorizontalScrollCategoryAdapter
import com.example.jello.user.adapter.ProductScrollAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class HomeFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var binding:FragmentHomeBinding
    private  var category = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        AdminActivity.binding.bottomNav.visibility = View.VISIBLE
        AdminActivity.binding.appBarAdmin.visibility = View.GONE
        db = Firebase.firestore
        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")

        fillSpinner()

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, arg3: Long) {
                category = parent.getItemAtPosition(position).toString()
                binding.btnSearch.setOnClickListener {
                    if (Common.alertUser(binding.txtNumBuyers)){
                            getProducts()
                    }
                }
            }
            override fun onNothingSelected(arg0: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Choose any category", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }


    private fun fillSpinner(){
        dialogLoading.build()
        val data = ArrayList<String>()
        db.collection(FirestoreDB.categoryCollection).orderBy(FirestoreDB.categoryNameField)
            .get()
            .addOnSuccessListener { querySnapshot->
                Common.dismissDialog(dialogLoading)
                for (document in querySnapshot){
                    data.add(document.get("name").toString())
                }
                val adp = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, data)
                binding.categorySpinner.adapter = adp
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(requireContext(), "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getProducts(){
        dialogLoading.build()
        val data = ArrayList<Product>()
        db.collection(FirestoreDB.categoryCollection).whereEqualTo("name",category)
            .get()
            .addOnSuccessListener {it
                for (document in it){
                    db.collection(FirestoreDB.categoryCollection).document(document.id)
                        .collection("products")
                        .whereGreaterThanOrEqualTo("numberOfBuyers",binding.txtNumBuyers.text.toString().toInt())
                        .get()
                        .addOnSuccessListener {products->
                            for (product in products){
                                data.add(
                                    Product(product.id,product.getString("name")!!
                                    ,product.getString("description")!!,product.getString("image")!!
                                    ,product.getDouble("price")!!,product.getDouble("rate")!!
                                    ,product.getDouble("latitude")!!,product.getDouble("longitude")!!
                                    ,product.get("numberOfBuyers").toString().toInt(),document.id)
                                )
                            }
                            val adapter = ProductAdminAdapter(requireActivity(),data)
                            binding.rvHomeAdmin.layoutManager = GridLayoutManager(requireContext(),2)
                            binding.rvHomeAdmin.adapter = adapter
                            Common.dismissDialog(dialogLoading)
                        }
                }
            }.addOnFailureListener {
                Common.toastErrorNetwork(requireActivity())
                Common.dismissDialog(dialogLoading)
            }
    }


}