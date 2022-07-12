package com.example.jello.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.R
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.ProductInCardBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import com.squareup.picasso.Picasso

class CartAdapter(var activity: Activity , var data:ArrayList<Product>):RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var db: FirebaseFirestore

    class CartViewHolder(var binding: ProductInCardBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        dialogLoading = Common.showDialog(activity,R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        db = Firebase.firestore
        val binding = ProductInCardBinding.inflate(LayoutInflater.from(activity),parent,false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        var count = 1
        holder.binding.btnPlus.setOnClickListener {
                count++
                holder.binding.btnCount.text = count.toString()
        }
        holder.binding.btnMin.setOnClickListener {
            if (count != 1){
                count--
                holder.binding.btnCount.text = count.toString()
            }
        }
        holder.binding.txtNameCart.text = data[position].name
        holder.binding.txtPriceCart.text = data[position].price.toString() + "$"
        holder.binding.imgProCart.clipToOutline = true
        Picasso.get().load(data[position].image).placeholder(R.drawable.placeholder_image)
            .into(holder.binding.imgProCart)
        holder.binding.btnShowChoices.setOnClickListener {
            val popupMenu = PopupMenu(activity,it)
            popupMenu.inflate(R.menu.cart_choices_menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.removeProduct -> removeProduct(position)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun removeProduct(position: Int){
        dialogLoading.build()
        val sharePreferences = activity.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val userDoc = sharePreferences.getString("userDoc","")
        db.collection(FirestoreDB.usersCollection).document(userDoc!!)
            .collection("productsInCart").document(data[position].id)
            .delete()
            .addOnSuccessListener {
                data.removeAt(position)
                notifyDataSetChanged()
                Common.dismissDialog(dialogLoading)
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.toastErrorNetwork(activity)
            }
    }




}