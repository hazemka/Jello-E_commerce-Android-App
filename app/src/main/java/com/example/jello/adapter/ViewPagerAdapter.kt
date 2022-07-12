package com.example.jello.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jello.databinding.WelcomeContentBinding
import com.example.jello.model.Welcome

class ViewPagerAdapter(var context: Context,var data:ArrayList<Welcome>):RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    class ViewHolder(var binding: WelcomeContentBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WelcomeContentBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.image.setImageResource(data[position].image)
        holder.binding.txtTilte.text = data[position].title
        holder.binding.txtSubTitle.text = data[position].subTitle
    }

    override fun getItemCount(): Int {
        return data.size
    }

}