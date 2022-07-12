package com.example.jello.admin.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.ChooseImageBinding
import com.example.jello.databinding.FragmentEditProductBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

class EditProductFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    lateinit var dialogBinding: ChooseImageBinding
    lateinit var binding: FragmentEditProductBinding
    val baos = ByteArrayOutputStream()
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val imagesRef = storageRef.child("Jello")
    var imageUri: Uri? = null
    private var categoryId = ""
    private var productId = ""
    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        dialogBinding.addImage.setImageURI(uri)
        binding.imageDisplay.setImageURI(uri)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProductBinding.inflate(inflater,container,false)
        dialogBinding = ChooseImageBinding.inflate(LayoutInflater.from(requireActivity()))
        db = Firebase.firestore
        dialogLoading = Common.showDialog(requireActivity(), R.drawable.loading_all, R.drawable.loading_all, "Loading ...", "Loading ...")
        // get from adapter :
        categoryId = requireArguments().getString("categoryId")!!
        productId = requireArguments().getString("productId")!!

        AdminActivity.binding.bottomNav.visibility = View.GONE
        AdminActivity.binding.appBarAdmin.visibility = View.VISIBLE
        AdminActivity.binding.toolBarAdmin.setNavigationIcon(R.drawable.ic_arrow_back)
        AdminActivity.binding.toolBarAdmin.setNavigationOnClickListener { requireActivity().onBackPressed() }
        getProductInfo()

        binding.btnChooseNewImage.setOnClickListener {
            try {
                getContent.launch("image/*")
            }catch (e:Exception){
                Toast.makeText(requireContext(), "Please Try Again", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveAllEdit.setOnClickListener {
            if (validation()){
                uploadImage()
            }
        }

        return binding.root
    }


    private fun validation():Boolean{
        val name = Common.validItem(binding.txtNewProName,binding.txtErrorNewName,"Name mustn't be empty")
        val price = Common.validItem(binding.txtNewProPrice,binding.txtErrorNewPrice,"Price mustn't be empty")
        return name && price
    }

    private fun uploadImage() {
        dialogLoading.build()
        val bitmap = (binding.imageDisplay.drawable as BitmapDrawable).bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        binding.newImageSaved.visibility = View.VISIBLE
        val data = baos.toByteArray()
        val imgRef = imagesRef.child(UUID.randomUUID().toString() + ".jpeg")
        val uploadTask = imgRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(requireContext(), "Error while Upload", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            imgRef.downloadUrl.addOnSuccessListener { uri ->
                imageUri = uri
                editProduct(binding.txtNewProName.text.toString(),binding.txtNewProPrice.text.toString().toDouble()
                    ,binding.txtNewProDesc.text.toString(),imageUri.toString())
            }
        }
    }

    private fun editProduct(name:String,price:Double,description:String,image:String){
        val updateProduct = HashMap<String,Any>()
        updateProduct["name"] = name
        updateProduct["price"] = price
        updateProduct["description"] = description
        updateProduct["image"] = image
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").document(productId)
            .update(updateProduct)
            .addOnSuccessListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(requireContext(), "Product Updated Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Toast.makeText(requireContext(), "Try again later..", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getProductInfo(){
        dialogLoading.build()
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").document(productId)
            .get()
            .addOnSuccessListener { product ->
                Common.dismissDialog(dialogLoading)
                binding.txtNewProName.setText(product.getString("name"))
                binding.txtNewProPrice.setText(product.getDouble("price").toString())
                binding.txtNewProDesc.setText(product.getString("description"))
                Picasso.get().load(product.getString("image")).placeholder(R.drawable.placeholder_image)
                    .into(binding.imageDisplay)
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.toastErrorNetwork(requireActivity())
            }
    }

}