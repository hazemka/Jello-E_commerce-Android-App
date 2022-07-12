package com.example.jello.admin.adapter

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.fragments.ProductAdminFragment
import com.example.jello.admin.model.Category
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.AddCategoryDialogBinding
import com.example.jello.databinding.CategoryItemBinding
import com.example.jello.databinding.DialogCommonBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import kotlin.collections.ArrayList

class CategoryAdapter(var activity: Activity,var data:ArrayList<Category>):RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(){

    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var db:FirebaseFirestore
    class CategoryViewHolder(var binding: CategoryItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        dialogLoading = Common.showDialog(activity,R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        db = Firebase.firestore
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(activity),parent,false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.binding.txtViewCategoryName.text = data[position].name
        holder.binding.clickMore.setOnClickListener { it ->
            val popupMenu = PopupMenu(activity,it)
            popupMenu.inflate(R.menu.category_menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_delete -> alertAndDelete(position)
                    R.id.action_edit -> editCategoryDialog(position)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
        holder.binding.btnShowProducts.setOnClickListener {
            val fragment = ProductAdminFragment()
            val b = Bundle()
            b.putString("categoryName",data[position].name)
            b.putString("categoryId",data[position].id)
            fragment.arguments = b
            (activity as AdminActivity).swipeFragmentWithBack(fragment)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


    private fun alertAndDelete(position: Int) {
        val dialogBinding = DialogCommonBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(activity)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialogBinding.txtTitle.text = "Delete Category !"
        dialogBinding.btnCancelDialog.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnOk.setOnClickListener {
                if (Common.checkNetwork(activity)){
                    dialog.dismiss()
                    dialogLoading.build()
                    deleteCategory(position)
                }else{
                    Common.toastErrorNetwork(activity)
                }
        }
        dialog.show()
    }

    private fun deleteCategory(position: Int){
        db.collection(FirestoreDB.categoryCollection).document(data[position].id)
            .delete()
            .addOnSuccessListener {
                    Common.dismissDialog(dialogLoading)
                    data.removeAt(position)
                    notifyDataSetChanged()
                Toast.makeText(activity, "Category deleted Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(activity, "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editCategoryDialog(position: Int){
        val dialogBinding = AddCategoryDialogBinding.inflate(activity.layoutInflater)
        val dialog = Dialog(activity)
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialogBinding.txtTitleDialog.text = "Enter new name:"
        dialogBinding.txtCategoryName.setText(data[position].name)
        dialogBinding.btnSave.setOnClickListener {
            if (Common.checkNetwork(activity)){
                if (Common.validItem(dialogBinding.txtCategoryName,dialogBinding.txtErrorCategory,"Please Enter category name !!")){
                    dialogLoading.build()
                    editCategory(dialogBinding.txtCategoryName.text.toString(),position)
                    dialog.dismiss()
                }
            }else{
                Common.toastErrorNetwork(activity)
            }
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun editCategory(categoryName :String ,position: Int){
        db.collection(FirestoreDB.categoryCollection).document(data[position].id)
            .update(FirestoreDB.categoryNameField,categoryName)
            .addOnSuccessListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(activity, "Category Updated Successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(activity,"Internet is week , Try again",Toast.LENGTH_LONG).show()
            }
    }
}