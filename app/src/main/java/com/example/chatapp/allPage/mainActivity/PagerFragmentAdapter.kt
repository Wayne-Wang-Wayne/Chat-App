package com.example.chatapp.allPage.mainActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

open class PagerFragmentAdapter<E> : FragmentStateAdapter {
     var fragments = ArrayList<E>()

    constructor(fragment: Fragment, fragments: ArrayList<E>) : super(fragment) {
        this.fragments = fragments
    }

    constructor(
        fragmentManager: FragmentManager, lifecycle: Lifecycle, fragments: ArrayList<E>) : super(
        fragmentManager, lifecycle) {
        this.fragments = fragments
    }


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position] as Fragment
    }
}