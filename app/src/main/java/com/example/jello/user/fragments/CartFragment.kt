package com.example.jello.user.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jello.R
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentCartBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.adapter.CartAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class CartFragment : Fragment() {
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var db: FirebaseFirestore
    private lateinit var binding:FragmentCartBinding
    var totalPrice = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCartBinding.inflate(inflater,container,false)

        UserActivity.binding.toolbar.visibility = View.GONE
        UserActivity.binding.btnGoToSearch.visibility = View.GONE
        UserActivity.binding.toolbar.navigationIcon = null

        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        db = Firebase.firestore

        getProductInCart()

        return binding.root
    }

    private fun getProductInCart(){
        val data = ArrayList<Product>()
        dialogLoading.build()
        val sharePreferences = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val userDoc = sharePreferences.getString("userDoc","")
        db.collection(FirestoreDB.usersCollection).document(userDoc!!)
            .collection("productsInCart")
            .get()
            .addOnSuccessListener {all ->
                if (all.size() == 0){
                    Common.dismissDialog(dialogLoading)
                    binding.imgNoCart.visibility = View.VISIBLE
                    binding.txtNoCart1.visibility = View.VISIBLE
                    binding.txtNoCart2.visibility = View.VISIBLE
                }
                for (product in all){
                    db.collection(FirestoreDB.categoryCollection).document(product.getString("categoryId")!!)
                        .collection("products").document(product.getString("productId")!!)
                        .get()
                        .addOnSuccessListener {
                            data.add(Product(product.id,it.getString("name")!!
                                    ,it.getString("description")!!,it.getString("image")!!
                                    ,it.getDouble("price")!!,it.getDouble("rate")!!
                                    ,it.getDouble("latitude")!!,it.getDouble("longitude")!!
                                    ,it.get("numberOfBuyers").toString().toInt())
                            )
                            if(data.size == 0){
                                binding.imgNoCart.visibility = View.VISIBLE
                                binding.txtNoCart1.visibility = View.VISIBLE
                                binding.txtNoCart2.visibility = View.VISIBLE
                            }else{
                                binding.imgNoCart.visibility = View.GONE
                                binding.txtNoCart1.visibility = View.GONE
                                binding.txtNoCart2.visibility = View.GONE
                                totalPrice += it.getDouble("price")!!
                                val adpter = CartAdapter(requireActivity(),data)
                                binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
                                binding.rvCart.adapter = adpter
                                binding.txtTotalPrice.text = "$totalPrice$"
                            }
                            Common.dismissDialog(dialogLoading)
                        }
                        .addOnFailureListener {
                            Common.dismissDialog(dialogLoading)
                            Common.toastErrorNetwork(requireActivity())
                        }
                }
            }
            .addOnFailureListener {
                Common.toastErrorNetwork(requireActivity())
            }
    }
}