package com.example.uosense.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.uosense.fragments.ProductMenuFragment
import com.example.uosense.fragments.BusinessHoursFragment
import com.example.uosense.fragments.BasicInfoFragment

class InfoSuggestionPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductMenuFragment()
            1 -> BusinessHoursFragment()
            else -> BasicInfoFragment()
        }
    }
}
