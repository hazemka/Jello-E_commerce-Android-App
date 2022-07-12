package com.example.jello.admin.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.adapter.CategoryAdapter
import com.example.jello.admin.model.Category
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.AddCategoryDialogBinding
import com.example.jello.databinding.FragmentCategoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import kotlin.collections.ArrayList

class CategoryFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var binding: FragmentCategoryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentCategoryBinding.inflate(inflater,container,false)
        db = Firebase.firestore
        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")

        AdminActivity.binding.bottomNav.visibility = View.VISIBLE
        AdminActivity.binding.appBarAdmin.visibility = View.GONE


        checkNetworkAndGetCategories()

        binding.swipRefrech.setOnRefreshListener {
            checkNetworkAndGetCategories()
        }

        binding.addCategory.setOnClickListener {
            addCategoryDialog()
        }

        return binding.root
    }

    private fun addCategoryDialog(){
        val dialogBinding = AddCategoryDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireActivity())
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialogBinding.btnSave.setOnClickListener {
            if (Common.checkNetwork(requireActivity())){
                if (Common.validItem(dialogBinding.txtCategoryName,dialogBinding.txtErrorCategory,"Please Enter category name !!")){
                    dialogLoading.build()
                    addCategory(dialogBinding.txtCategoryName.text.toString())
                    dialog.dismiss()
                }
            }else{
                Common.toastErrorNetwork(requireActivity())
            }
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addCategory(name:String){
        val category = Category("",name)
        db.collection(FirestoreDB.categoryCollection).add(category)
            .addOnSuccessListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(requireContext(), "Category Added Successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(requireContext(), "Error while Adding Category", Toast.LENGTH_LONG).show()
            }
    }

    private fun getAllCategories(){
        val data = ArrayList<Category>()
        db.collection(FirestoreDB.categoryCollection).orderBy(FirestoreDB.categoryNameField)
            .get()
            .addOnSuccessListener { querySnapshot->
                Common.dismissDialog(dialogLoading)
                Common.hideSwipeRefresh(binding.swipRefrech)
                for (document in querySnapshot){
                    data.add(Category(document.id,document.get("name").toString()))
                }
                if (data.size == 0){
                    binding.imgNotFound.visibility = View.VISIBLE
                    binding.txtNotFound.visibility = View.VISIBLE
                    binding.txtNotFound2.visibility = View.VISIBLE
                }else{
                    binding.imgNotFound.visibility = View.GONE
                    binding.txtNotFound.visibility = View.GONE
                    binding.txtNotFound2.visibility = View.GONE
                    val adapter = CategoryAdapter(requireActivity(),data)
                    binding.rvData.adapter = adapter
                    binding.rvData.layoutManager = LinearLayoutManager(requireContext())
                }
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.hideSwipeRefresh(binding.swipRefrech)
                Toast.makeText(requireContext(), "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkNetworkAndGetCategories(){
        if (Common.checkNetwork(requireActivity())){
            dialogLoading.build()
            getAllCategories()
        }else{
            Common.toastErrorNetwork(requireActivity())
            Common.hideSwipeRefresh(binding.swipRefrech)
        }
    }
}