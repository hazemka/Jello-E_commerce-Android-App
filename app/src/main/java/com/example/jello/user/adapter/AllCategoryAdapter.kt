package com.example.jello.user.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.admin.model.Category
import com.example.jello.databinding.CategoryAllItemBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.fragments.ShowProductUserFragment

class AllCategoryAdapter(var context: Context,var data:ArrayList<Category>):RecyclerView.Adapter<AllCategoryAdapter.AllCategoryViewHolder>() {

    class AllCategoryViewHolder(var binding: CategoryAllItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllCategoryViewHolder {
        val binding = CategoryAllItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return AllCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllCategoryViewHolder, position: Int) {
        holder.binding.txtCategoryNameAll.text = data[position].name
        holder.binding.root.setOnClickListener {
            val fragment = ShowProductUserFragment()
            val b = Bundle()
            b.putString("categoryId",data[position].id)
            b.putString("categoryName",data[position].name)
            fragment.arguments = b
            (context as UserActivity).swipeFragmentWithBack(fragment)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}