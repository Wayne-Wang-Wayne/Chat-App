package com.example.chatapp.allPage.MediaActivity.pagerAdapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class PageFragmentAdapter<T>(fm: FragmentManager, private val fragmentList: ArrayList<T>) :
    FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position] as Fragment
    }
}