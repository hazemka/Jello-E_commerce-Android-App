package com.example.jello.user.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.admin.AdminActivity
import com.example.jello.admin.adapter.ProductAdminAdapter
import com.example.jello.admin.fragments.ProductAdminFragment
import com.example.jello.admin.model.Category
import com.example.jello.databinding.CardItemScrollBinding
import com.example.jello.user.UserActivity
import com.example.jello.user.fragments.ShowProductUserFragment

class HorizontalScrollCategoryAdapter(var context:Context, var data:ArrayList<Category>):RecyclerView.Adapter<HorizontalScrollCategoryAdapter.CardViewHolder>() {

    class CardViewHolder(var binding:CardItemScrollBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = CardItemScrollBinding.inflate(LayoutInflater.from(context),parent,false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.binding.txtCardName.text = data[position].name
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