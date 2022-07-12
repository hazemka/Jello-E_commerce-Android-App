package com.example.jello.admin.fragments

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.core.content.PermissionChecker.*
import androidx.fragment.app.Fragment
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.MapAdminActivity
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.ChooseImageBinding
import com.example.jello.databinding.FragmentAddProductBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import java.io.ByteArrayOutputStream
import java.util.*


class AddProductFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    lateinit var dialogBinding: ChooseImageBinding
    val baos = ByteArrayOutputStream()
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val imagesRef = storageRef.child("Jello")
    var imageUri: Uri? = null
    val LOCATION_CODE = 100
    var latitude = 0.0
    var longitude = 0.0
    private lateinit var locationManager: LocationManager
    private var categoryId = ""
    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        dialogBinding.addImage.setImageURI(uri)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddProductBinding.inflate(inflater, container, false)
        db = Firebase.firestore
        dialogLoading = Common.showDialog(requireActivity(), R.drawable.loading_all, R.drawable.loading_all, "Loading ...", "Loading ...")
        dialogBinding = ChooseImageBinding.inflate(LayoutInflater.from(requireActivity()))
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        // get categoryId :
        categoryId = requireArguments().getString("categoryId")!!

        AdminActivity.binding.bottomNav.visibility = View.GONE
        AdminActivity.binding.appBarAdmin.visibility = View.VISIBLE
        AdminActivity.binding.toolBarAdmin.setNavigationIcon(R.drawable.ic_arrow_back)
        AdminActivity.binding.toolBarAdmin.setNavigationOnClickListener { requireActivity().onBackPressed() }

        binding.btnChooseImage.setOnClickListener {
            try {
                getImage()
            }catch (e:Exception){
                Toast.makeText(requireContext(), "Please Try Again", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddProduct.setOnClickListener {
                if (validation()){
                    dialogLoading.build()
                    uploadImage()
                }
        }

        binding.btnSelectLocation.setOnClickListener {
                if (Build.VERSION.SDK_INT >= 23){
                    if (checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_DENIED){
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_CODE)
                    }else if (checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED){
                        if (checkGPS()){
                            val locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
                            locationClient.lastLocation
                                .addOnSuccessListener {location ->
                                    if(location != null){
                                        latitude = location.latitude
                                        longitude = location.longitude
                                        val i = Intent(requireActivity(),MapAdminActivity::class.java)
                                        i.putExtra("latitude",latitude)
                                        i.putExtra("longitude",longitude)
                                        startActivity(i)
                                    }else{
                                        Snackbar.make(it,"Check is GPS enabled !",Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Error wile get location", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }else{
                    checkGPS()
                }
        }

        return binding.root
    }

    private fun getImage() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialogBinding.addImage.setOnClickListener {
            getContent.launch("image/*")
        }
        dialogBinding.btnSaveImage.setOnClickListener {
            val bitmap = (dialogBinding.addImage.drawable as BitmapDrawable).bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            dialog.dismiss()
            binding.savedImage.visibility = View.VISIBLE
        }
        dialog.show()
    }

    private fun uploadImage() {
        val data = baos.toByteArray()
        val imgRef = imagesRef.child(UUID.randomUUID().toString() + ".jpeg")
        val uploadTask = imgRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(requireContext(), "Error while Upload", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            imgRef.downloadUrl.addOnSuccessListener { uri ->
                imageUri = uri
                addProduct(Product("",binding.txtProductName.text.toString()
                    ,binding.txtProductDesc.text.toString(),imageUri.toString(),binding.txtProductPrice.text.toString().toDouble()
                    ,0.0, latitudeProduct, longitudeProduct,0))
            }
        }
    }

    private fun validation():Boolean{
        val name = Common.validItem(binding.txtProductName,binding.txtNameError,"Name mustn't be empty")
        val price = Common.validItem(binding.txtProductPrice,binding.txtPriceError,"Price mustn't be empty")
        val image = binding.savedImage.visibility == View.VISIBLE
        val location = binding.savedLocation.visibility == View.VISIBLE
        if (!image){
            Toast.makeText(requireContext(), "Please Choose image !!", Toast.LENGTH_SHORT).show()
        }
        if (!location){
            Toast.makeText(requireContext(), "Please Select Location !!", Toast.LENGTH_SHORT).show()
        }
        return name && price && image && location
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    checkGPS()
            }else{
                Toast.makeText(requireContext(), "You cannot add the location of the product", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkGPS():Boolean{
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("GPS isn't enabled !")
            dialog.setMessage("Enable it to be able to locate the product")
            dialog.setCancelable(true)
            dialog.setIcon(R.drawable.ic_baseline_location_off_24)
            dialog.setPositiveButton("OK") { d, _ ->
                d.dismiss()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            dialog.setNegativeButton("NO") { d, _ ->
                d.dismiss()
            }
            dialog.show()
        }
        return false
    }

     companion object{
         lateinit var binding: FragmentAddProductBinding
         var latitudeProduct = 0.0
         var longitudeProduct = 0.0
         fun setProductLocation(latitude:Double,longitude:Double){
             latitudeProduct = latitude
             longitudeProduct =longitude
             binding.savedLocation.visibility = View.VISIBLE
         }
     }

    private fun addProduct(product: Product){
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products")
            .add(product)
            .addOnSuccessListener {
                clearFields()
                Common.dismissDialog(dialogLoading)
                Toast.makeText(requireContext(), "Product added Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Please check Internet Connection", Toast.LENGTH_SHORT).show()
                Common.dismissDialog(dialogLoading)
            }
    }

    private fun clearFields(){
        binding.txtProductPrice.text.clear()
        binding.txtProductName.text.clear()
        binding.txtProductDesc.text.clear()
        binding.savedImage.visibility = View.GONE
        binding.savedLocation.visibility = View.GONE
    }


}