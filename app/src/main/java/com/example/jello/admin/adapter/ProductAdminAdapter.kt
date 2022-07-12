package com.example.jello.admin.adapter

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.fragments.EditProductFragment
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.DialogCommonBinding
import com.example.jello.databinding.ProductItemBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.fragments.ShowProductDetailsFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import com.squareup.picasso.Picasso

class ProductAdminAdapter(var activity:Activity, var data:ArrayList<Product>):RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder>(){
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var db: FirebaseFirestore
    private var initialData = data

    class ProductViewHolder(var binding:ProductItemBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(activity),parent,false)
        dialogLoading = Common.showDialog(activity,R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        db = Firebase.firestore

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.binding.txtShowName.text = data[position].name
        holder.binding.txtShowPrice.text = data[position].price.toString() + "$"
        Picasso.get().load(data[position].image)
            .placeholder(R.drawable.placeholder_image).into(holder.binding.imgShowProduct)
        // to enable edit and delete if the current user is admin :
        val sharedPreferences = activity.getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        if (sharedPreferences.getString("userType","") == "admin"){
            holder.binding.editProductAdmin.visibility = View.VISIBLE
            holder.binding.editProductAdmin.setOnClickListener {
                val popupMenu = PopupMenu(activity,it)
                popupMenu.inflate(R.menu.edit_product_menu)
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.deleteProduct -> alertAndDelete(position)
                        R.id.editProduct ->  editProduct(position)
                    }
                    return@setOnMenuItemClickListener true
                }
                popupMenu.show()
            }
        }else{
            // to enable view product if the current user is user :
            holder.binding.root.setOnClickListener {
                val fragment = ShowProductDetailsFragment()
                val b = Bundle()
                b.putString("categoryId",data[position].categoryId)
                b.putString("productId",data[position].id)
                fragment.arguments = b
                (activity as UserActivity).swipeFragmentWithBack(fragment)
            }
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


    private fun alertAndDelete(position: Int){
        val dialogBinding = DialogCommonBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(activity)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialogBinding.txtTitle.text = "Delete Product !"
        dialogBinding.btnCancelDialog.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnOk.setOnClickListener {
            if (Common.checkNetwork(activity)){
                dialog.dismiss()
                dialogLoading.build()
                deleteProduct(position)
            }else{
                Common.toastErrorNetwork(activity)
            }
        }
        dialog.show()
    }


    private fun deleteProduct(position: Int){
        db.collection(FirestoreDB.categoryCollection).document(data[position].categoryId)
            .collection("products").document(data[position].id)
            .delete()
            .addOnSuccessListener {
                Common.dismissDialog(dialogLoading)
                data.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(activity, "Product deleted Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(activity, "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }


    private fun editProduct(position: Int){
        val fragment = EditProductFragment()
        val b = Bundle()
        b.putString("categoryId", data[position].categoryId)
        b.putString("productId",data[position].id)
        fragment.arguments = b
        (activity as AdminActivity).swipeFragmentWithBack(fragment)
    }

    fun search(text:String){
        val newArray = initialData.filter { it->
            it.name.contains(text) || it.name.lowercase().contains(text) ||
                    it.name.uppercase().contains(text) || it.description.contains(text) ||
                    it.price.toString().contains(text) || it.rate.toString().contains(text)
        }
        data = newArray as ArrayList<Product>
        notifyDataSetChanged()
    }

}
