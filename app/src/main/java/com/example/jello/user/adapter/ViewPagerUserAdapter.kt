package com.example.jello.user.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.R
import com.example.jello.databinding.SlideUserBinding
import com.example.jello.user.model.SlideUser
import com.squareup.picasso.Picasso

class ViewPagerUserAdapter(var context: Context ,var data:ArrayList<SlideUser>):RecyclerView.Adapter<ViewPagerUserAdapter.UserViewHolder>(){

    class UserViewHolder(var binding: SlideUserBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = SlideUserBinding.inflate(LayoutInflater.from(context),parent,false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Picasso.get().load(data[position].img).placeholder(R.drawable.placeholder_image).into(holder.binding.imgSlide)
    }

    override fun getItemCount(): Int {
        return data.size
    }

}