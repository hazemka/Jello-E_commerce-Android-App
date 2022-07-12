package com.example.jello.user.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.R
import com.example.jello.admin.adapter.ProductAdminAdapter
import com.example.jello.admin.model.Product
import com.example.jello.databinding.ProductItemTwoBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.fragments.ShowProductDetailsFragment
import com.squareup.picasso.Picasso

class ProductScrollAdapter(var context: Context, var data:ArrayList<Product>):RecyclerView.Adapter<ProductScrollAdapter.ProductUserViewHolder>() {

    class ProductUserViewHolder(var binding: ProductItemTwoBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductUserViewHolder {
        val binding = ProductItemTwoBinding.inflate(LayoutInflater.from(context),parent,false)
        return ProductUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductUserViewHolder, position: Int) {
        holder.binding.nameProduct2.text = data[position].name
        holder.binding.txtPrice2.text = data[position].price.toString() + "$"
        Picasso.get().load(data[position].image).placeholder(R.drawable.placeholder_image)
            .into(holder.binding.imageProduct2)

        holder.binding.root.setOnClickListener {
            val fragment = ShowProductDetailsFragment()
            val b = Bundle()
            b.putString("categoryId",data[position].categoryId)
            b.putString("productId",data[position].id)
            fragment.arguments = b
            (context as UserActivity).swipeFragmentWithBack(fragment)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}