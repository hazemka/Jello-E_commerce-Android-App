package com.example.jello.admin.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jello.R
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.adapter.CategoryAdapter
import com.example.jello.admin.adapter.ProductAdminAdapter
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentProductAdminBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class ProductAdminFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private lateinit var binding:FragmentProductAdminBinding
    private var categoryId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProductAdminBinding.inflate(inflater,container,false)
        db = Firebase.firestore
        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")

        categoryId = requireArguments().getString("categoryId")!!
        val categoryName = requireArguments().getString("categoryName")

        AdminActivity.binding.bottomNav.visibility = View.GONE
        AdminActivity.binding.appBarAdmin.visibility = View.VISIBLE
        AdminActivity.binding.toolBarAdmin.setNavigationIcon(R.drawable.ic_arrow_back)
        AdminActivity.binding.toolBarAdmin.setNavigationOnClickListener { requireActivity().onBackPressed() }
        getProducts()

        binding.txtProductDes.text = "Products for $categoryName"

        binding.RefreshProductAdmin.setOnRefreshListener {
            getProducts()
        }

        binding.btnAddProduct.setOnClickListener {
            val fragment = AddProductFragment()
            val bundle = Bundle()
            bundle.putString("categoryId",categoryId)
            fragment.arguments = bundle
            swipeFragment(fragment)
        }


        return binding.root
    }

    private fun swipeFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.adminContainer,fragment)
            .commit()
    }

    private fun getProducts(){
        dialogLoading.build()
        val data = ArrayList<Product>()
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").get()
            .addOnSuccessListener {
                Common.dismissDialog(dialogLoading)
                Common.hideSwipeRefresh(binding.RefreshProductAdmin)
                for (document in it){
                    data.add(Product(document.id,document.getString("name")!!,document.getString("description")!!
                    ,document.getString("image")!!,document.getDouble("price")!!,document.getDouble("rate")!!
                    ,document.getDouble("latitude")!!,document.getDouble("longitude")!!
                        ,document.get("numberOfBuyers").toString().toInt(),categoryId))
                }
                if (data.size == 0){
                    binding.imgNoPorduct.visibility = View.VISIBLE
                    binding.txtNoPro1.visibility = View.VISIBLE
                    binding.txtNoPro2.visibility = View.VISIBLE
                }else{
                    binding.imgNoPorduct.visibility = View.GONE
                    binding.txtNoPro1.visibility = View.GONE
                    binding.txtNoPro2.visibility = View.GONE
                    val adapter = ProductAdminAdapter(requireActivity(),data)

                    binding.rvProductAdmin.adapter = adapter
                    binding.rvProductAdmin.layoutManager = GridLayoutManager(requireContext(),2)
                }
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.hideSwipeRefresh(binding.RefreshProductAdmin)
                Toast.makeText(requireContext(), "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu,menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.isSubmitButtonEnabled = false
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                (binding.rvProductAdmin.adapter as ProductAdminAdapter).search(newText!!)
                return true
            }
        })

        searchView.setOnCloseListener {
            false
        }
    }

}