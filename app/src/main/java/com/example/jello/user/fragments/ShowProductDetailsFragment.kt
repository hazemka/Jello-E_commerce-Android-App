package com.example.jello.user.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jello.R
import com.example.jello.admin.model.Product
import com.example.jello.app.CreateNotifications
import com.example.jello.app.JelloApp
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.DialogCommonBinding
import com.example.jello.databinding.FragmentShowProductDetailsBinding
import com.example.jello.user.MapUserActivity
import com.example.jello.user.UserActivity
import com.example.jello.user.adapter.ProductScrollAdapter
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import com.squareup.picasso.Picasso

class ShowProductDetailsFragment : Fragment() {
    var productId = ""
    var categoryId = ""
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentShowProductDetailsBinding
    private lateinit var locationManager: LocationManager
    var productDescription = ""
    var productLatitude = 0.0
    var productLongitude = 0.0
    var userLatitude = 0.0
    var userLongitude = 0.0
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShowProductDetailsBinding.inflate(inflater,container,false)
        binding.imgProductDetails.clipToOutline = true
        productId = requireArguments().getString("productId")!!
        categoryId = requireArguments().getString("categoryId")!!


        UserActivity.binding.toolbar.visibility = View.VISIBLE
        UserActivity.binding.bottomNavUser.visibility = View.GONE
        UserActivity.binding.btnGoToSearch.visibility = View.GONE
        UserActivity.binding.appBarLayout.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        UserActivity.binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        db = Firebase.firestore
        val checkPermission  = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            if (isGranted){
                if(checkGPS()){
                    val locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
                    locationClient.lastLocation
                        .addOnSuccessListener {location ->
                            if(location != null){
                                userLatitude = location.latitude
                                userLongitude = location.longitude
                                val i = Intent(requireActivity(),MapUserActivity::class.java)
                                i.putExtra("productLatitude",productLatitude)
                                i.putExtra("productLongitude",productLongitude)
                                i.putExtra("userLatitude",userLatitude)
                                i.putExtra("userLongitude",userLongitude)
                                startActivity(i)
                            }
                        }
                }
            }else{
                Toast.makeText(requireContext(), "Please accept permission to view the location", Toast.LENGTH_SHORT).show()
            }
        }
        beforeLoading()
        getProductDetails()

        binding.btnSeeDescription.setOnClickListener {
            dialogDescription()
        }

        binding.btnSeeLocation.setOnClickListener {
            checkPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.btnAddToCart.setOnClickListener {
            addProductToCart(it)
        }

        return binding.root
    }

    private fun getProductDetails(){
        dialogLoading.build()
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").document(productId)
            .get()
            .addOnSuccessListener { product ->
                binding.txtNameProDetails.text = product.getString("name")
                binding.txtPriceProDetails.text = product.getDouble("price").toString() + "$"
                Picasso.get().load(product.getString("image")).placeholder(R.drawable.placeholder_image)
                    .into(binding.imgProductDetails)
                productLatitude = product.getDouble("latitude")!!
                productLongitude = product.getDouble("longitude")!!
                productDescription = product.getString("description")!!
                getRelatedProduct()
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.toastErrorNetwork(requireActivity())
            }
    }

    private fun getRelatedProduct(){
        val data = ArrayList<Product>()
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").whereNotEqualTo("name",binding.txtNameProDetails.text.toString())
            .limit(5)
            .get()
            .addOnSuccessListener { products ->
                for (product in products){
                    data.add(Product(product.id,product.getString("name")!!
                    ,product.getString("description")!!,product.getString("image")!!
                    ,product.getDouble("price")!!,product.getDouble("rate")!!
                    ,product.getDouble("latitude")!!,product.getDouble("longitude")!!
                        ,product.get("numberOfBuyers").toString().toInt(),categoryId))
                }
                val adapter = ProductScrollAdapter(requireContext(),data)
                binding.scrollRelatedProduct.rvScrollHorizontal.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                binding.scrollRelatedProduct.rvScrollHorizontal.adapter = adapter
                Common.dismissDialog(dialogLoading)
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.toastErrorNetwork(requireActivity())
            }
    }

    private fun beforeLoading(){
        val dataProduct = ArrayList<Product>()
        for (i in 0..4){
            dataProduct.add(Product("","Product","description","https://firebasestorage.googleapis.com/v0/b/fullhazemalkateb.appspot.com/o/placeholder_image.png?alt=media&token=6622b0d4-9fdd-4a9e-b0ea-203cda6de274"
                ,199.0,0.0,0.0,0.0,0))
        }
        val productScrollAdapter = ProductScrollAdapter(requireContext(),dataProduct)
        binding.scrollRelatedProduct.rvScrollHorizontal.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.scrollRelatedProduct.rvScrollHorizontal.adapter = productScrollAdapter
    }

    private fun dialogDescription(){
        val dialogBinding = DialogCommonBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialogBinding.txtTitle.text = productDescription
        dialogBinding.btnCancelDialog.text = "OK"
        dialogBinding.btnCancelDialog.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.btnOk.visibility = View.GONE
        dialogBinding.imgIcon.visibility = View.GONE
        dialog.show()
    }

    private fun checkGPS():Boolean{
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("GPS isn't enabled !")
            dialog.setMessage("Enable it to see the distance between you and the product.")
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

    private fun addProductToCart(view: View){
        dialogLoading.build()
        val sharePreferences = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val userDoc = sharePreferences.getString("userDoc","")
                    val product = HashMap<String,Any>()
                    product["categoryId"] = categoryId
                    product["productId"] = productId
                    db.collection(FirestoreDB.usersCollection).document(userDoc!!)
                        .collection("productsInCart")
                        .add(product)
                        .addOnSuccessListener {
                            updateNumberOfBuyers(view,categoryId,productId)
                        }
                        .addOnFailureListener {
                            Common.toastErrorNetwork(requireActivity())
                        }
                }

    private fun updateNumberOfBuyers(view: View,categoryId:String,productId:String){
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").document(productId)
            .get()
            .addOnSuccessListener {
                val num = it.get("numberOfBuyers").toString().toInt()
                db.collection(FirestoreDB.categoryCollection).document(categoryId)
                    .collection("products").document(productId)
                    .update("numberOfBuyers",num+1)
                    .addOnSuccessListener {
                        Common.dismissDialog(dialogLoading)
                        Snackbar.make(view,"Product Added to cart",Snackbar.LENGTH_LONG).show()
                        // here create notification :
                        notification()
                    }
                    .addOnFailureListener {
                            Common.dismissDialog(dialogLoading)
                        Common.toastErrorNetwork(requireActivity())
                    }
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.toastErrorNetwork(requireActivity())
            }
    }
    private fun notification(){
        val notification = CreateNotifications(requireContext())
        val createNotification = notification.createNotification(JelloApp.channel_ID_one,"The Product added to cart !",
            "Name: ${binding.txtNameProDetails.text} , Price: ${binding.txtPriceProDetails.text}",R.drawable.logo)
        notification.notifyNotification(createNotification)
    }

    private fun swipeFragmentWithBack(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer,fragment)
            .addToBackStack(null)
            .commit()
    }

}

