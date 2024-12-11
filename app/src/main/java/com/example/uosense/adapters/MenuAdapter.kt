package com.example.uosense.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.MenuResponse

class MenuAdapter : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private var menuItems = listOf<MenuResponse>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuName: TextView = itemView.findViewById(R.id.menuName)
        val menuPrice: TextView = itemView.findViewById(R.id.menuPrice)
        val menuDescription: TextView = itemView.findViewById(R.id.menuDescription)
        val menuImage: ImageView = itemView.findViewById(R.id.menuImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]

        holder.menuName.text = menuItem.name
        holder.menuPrice.text = "${menuItem.price}원"
        holder.menuDescription.text = menuItem.description

        // 이미지 로딩 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load(menuItem.imageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(holder.menuImage)
    }

    override fun getItemCount(): Int = menuItems.size

    // submitList 함수
    fun submitList(newMenuItems: List<MenuResponse>) {
        menuItems = newMenuItems
        notifyDataSetChanged()
    }

}
