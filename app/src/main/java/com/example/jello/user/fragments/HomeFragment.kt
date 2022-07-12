package com.example.jello.user.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.jello.R
import com.example.jello.admin.model.Category
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentHome2Binding
import com.example.jello.user.UserActivity
import com.example.jello.user.adapter.HorizontalScrollCategoryAdapter
import com.example.jello.user.adapter.ProductScrollAdapter
import com.example.jello.user.adapter.ViewPagerUserAdapter
import com.example.jello.user.model.SlideUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class HomeFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var binding:FragmentHome2Binding
    val dressImage = "https://firebasestorage.googleapis.com/v0/b/fullhazemalkateb.appspot.com/o/dress.png?alt=media&token=72bc990f-b0fb-4a89-87b0-0733eb22d216"
    val shoesImage = "https://firebasestorage.googleapis.com/v0/b/fullhazemalkateb.appspot.com/o/shoes.png?alt=media&token=c4bb73b0-8eb2-46e7-9016-40da08c56318"
    val tShirtIamge = "https://firebasestorage.googleapis.com/v0/b/fullhazemalkateb.appspot.com/o/t_shirt.png?alt=media&token=6ab0438b-3823-4c23-9544-016716562a2c"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHome2Binding.inflate(inflater,container,false)
        db = Firebase.firestore

        UserActivity.binding.toolbar.visibility = View.VISIBLE
        UserActivity.binding.bottomNavUser.visibility = View.VISIBLE
        UserActivity.binding.btnGoToSearch.visibility = View.VISIBLE
        UserActivity.binding.toolbar.navigationIcon = null
        UserActivity.binding.appBarLayout.setBackgroundResource(android.R.color.transparent)
        UserActivity.binding.toolbar.setBackgroundResource(R.color.toolbar_visible)
        
        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")
        // slides :
        val data = ArrayList<SlideUser>()
        data.add(SlideUser(0,shoesImage))
        data.add(SlideUser(1,tShirtIamge))
        data.add(SlideUser(2,dressImage))
        val adapter  = ViewPagerUserAdapter(requireActivity(),data)
        binding.viewPagerSlide.adapter = adapter
        binding.viewPagerSlide.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0 -> {
                        changeDots(R.drawable.user_dot,R.drawable.user_dot_not,R.drawable.user_dot_not)
                    }
                    1-> {
                        changeDots(R.drawable.user_dot_not,R.drawable.user_dot,R.drawable.user_dot_not)
                    }
                    2->{
                        changeDots(R.drawable.user_dot_not,R.drawable.user_dot_not,R.drawable.user_dot)
                    }
                }
            }
        })
        // scroll categories :
        checkInternetAndGet()
        beforeLoading()

        binding.btnSeeAllCategories.setOnClickListener {
            swipeFragment(SeeAllCategoryFragment())
        }

        return binding.root
    }

        private fun get5Categories(){
            val data = ArrayList<Category>()
            db.collection(FirestoreDB.categoryCollection).orderBy(FirestoreDB.categoryNameField).limit(5)
                .get()
                .addOnSuccessListener {it
                    for (document in it){
                        data.add(Category(document.id,document.get(FirestoreDB.categoryNameField).toString()))
                    }
                    val adapter = HorizontalScrollCategoryAdapter(requireContext(),data)
                    binding.scrollHome2.rvScrollHorizontal.adapter = adapter
                    binding.scrollHome2.rvScrollHorizontal.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                    get5Products()
                }
                .addOnFailureListener {
                    Common.dismissDialog(dialogLoading)
                    Toast.makeText(requireContext(), "Internet is weak, try again", Toast.LENGTH_SHORT).show()
                }
        }

        private fun get5Products(){
            val data = ArrayList<Product>()
            db.collection(FirestoreDB.categoryCollection).limit(10)
                .get()
                .addOnSuccessListener {it
                    for (document in it){
                        db.collection(FirestoreDB.categoryCollection).document(document.id)
                            .collection("products").whereGreaterThan("numberOfBuyers",5)
                            .limit(1)
                            .get()
                            .addOnSuccessListener {
                                for (product in it){
                                    data.add(Product(product.id,product.getString("name")!!
                                    ,product.getString("description")!!,product.getString("image")!!
                                    ,product.getDouble("price")!!,product.getDouble("rate")!!
                                    ,product.getDouble("latitude")!!,product.getDouble("longitude")!!
                                    ,product.get("numberOfBuyers").toString().toInt(),document.id))
                                }
                                val productAdapter = ProductScrollAdapter(requireContext(),data)
                                binding.srcollProdutsHome2.rvScrollHorizontal.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                                binding.srcollProdutsHome2.rvScrollHorizontal.adapter = productAdapter
                                Common.dismissDialog(dialogLoading)
                            }
                    }
            }.addOnFailureListener {
                Common.toastErrorNetwork(requireActivity())
                Common.dismissDialog(dialogLoading)
            }
        }

        private fun checkInternetAndGet(){
            if (Common.checkNetwork(requireActivity())){
                dialogLoading.build()
                get5Categories()
            }else{
                Common.toastErrorNetwork(requireActivity())
            }
        }

        private fun beforeLoading(){
            val dataBeforeLoad = ArrayList<Category>()
            for (i in 0..4){
                dataBeforeLoad.add(Category("$i","Category"))
            }
            val adapterScrollCardAdapter = HorizontalScrollCategoryAdapter(requireContext(),dataBeforeLoad)
            binding.scrollHome2.rvScrollHorizontal.adapter = adapterScrollCardAdapter
            binding.scrollHome2.rvScrollHorizontal.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            // before loading products :
            val dataProduct = ArrayList<Product>()
            for (i in 0..9){
                dataProduct.add(Product("","Product","description","https://firebasestorage.googleapis.com/v0/b/fullhazemalkateb.appspot.com/o/placeholder_image.png?alt=media&token=6622b0d4-9fdd-4a9e-b0ea-203cda6de274"
                ,199.0,0.0,0.0,0.0,0))
            }
            val productScrollAdapter = ProductScrollAdapter(requireContext(),dataProduct)
            binding.srcollProdutsHome2.rvScrollHorizontal.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            binding.srcollProdutsHome2.rvScrollHorizontal.adapter = productScrollAdapter
        }

    private fun changeDots(dot1:Int,dot2:Int,dot3:Int){
        binding.dotOneSlide.setImageResource(dot1)
        binding.dotTwoSlide.setImageResource(dot2)
        binding.dotThreeSlide.setImageResource(dot3)
    }

    private fun swipeFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.userContainer,fragment)
            .addToBackStack(null)
            .commit()
    }

}