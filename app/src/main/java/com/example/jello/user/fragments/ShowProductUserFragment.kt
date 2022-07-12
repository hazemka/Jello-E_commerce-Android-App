package com.example.jello.user.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.jello.R
import com.example.jello.SignInSignUpActivity
import com.example.jello.admin.adapter.ProductAdminAdapter
import com.example.jello.admin.model.Product
import com.example.jello.common.Common
import com.example.jello.common.FirestoreDB
import com.example.jello.databinding.FragmentShowProductUserBinding
import com.example.jello.user.UserActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog

class ShowProductUserFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var dialogLoading: ProgressBarGIFDialog.Builder
    private var categoryId = ""
    lateinit var binding:FragmentShowProductUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShowProductUserBinding.inflate(inflater,container,false)
        db = Firebase.firestore
        dialogLoading = Common.showDialog(requireActivity(),R.drawable.loading_all,R.drawable.loading_all,"Loading ...","Loading ...")

        UserActivity.binding.bottomNavUser.visibility = View.GONE
        UserActivity.binding.toolbar.visibility = View.VISIBLE
        UserActivity.binding.btnGoToSearch.visibility = View.GONE
        UserActivity.binding.appBarLayout.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        UserActivity.binding.toolbar.setBackgroundResource(R.color.white)
        UserActivity.binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }


        categoryId = requireArguments().getString("categoryId")!!
        val categoryName = requireArguments().getString("categoryName")

        binding.txtCategoryNameUser.text = "Products for $categoryName"
        getProducts()

        binding.swipeRefreshUserPro.setOnRefreshListener {
            getProducts()
        }

        return binding.root
    }

    private fun getProducts(){
        dialogLoading.build()
        val data = ArrayList<Product>()
        db.collection(FirestoreDB.categoryCollection).document(categoryId)
            .collection("products").get()
            .addOnSuccessListener {
                Common.dismissDialog(dialogLoading)
                Common.hideSwipeRefresh(binding.swipeRefreshUserPro)
                for (document in it){
                    data.add(
                        Product(document.id,document.getString("name")!!,document.getString("description")!!
                        ,document.getString("image")!!,document.getDouble("price")!!,document.getDouble("rate")!!
                        ,document.getDouble("latitude")!!,document.getDouble("longitude")!!
                            ,document.get("numberOfBuyers").toString().toInt(),categoryId)
                    )
                }
                if (data.size == 0){
                    binding.imgNoProductUser.visibility = View.VISIBLE
                    binding.txtNoProducts.visibility = View.VISIBLE
                }else{
                    binding.imgNoProductUser.visibility = View.GONE
                    binding.txtNoProducts.visibility = View.GONE
                    // I used this adapter from admin section :
                    val adapter = ProductAdminAdapter(requireActivity(),data)

                    binding.rvProductUser.adapter = adapter
                    binding.rvProductUser.layoutManager = GridLayoutManager(requireContext(),2)
                }
            }
            .addOnFailureListener {
                Common.dismissDialog(dialogLoading)
                Common.hideSwipeRefresh(binding.swipeRefreshUserPro)
                Toast.makeText(requireContext(), "Internet is weak, try again", Toast.LENGTH_SHORT).show()
            }
    }

    // for search with any data (Product data)
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
                (binding.rvProductUser.adapter as ProductAdminAdapter).search(newText!!)
                return true
            }
        })

        searchView.setOnCloseListener {
            false
        }
    }
}