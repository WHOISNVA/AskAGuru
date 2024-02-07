package com.example.askguru.ui.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter


class ProfileViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle

) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val NUM_TABS = 3

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> LikeFragment()
            2 -> ContributionsFragment()
            else -> ProfilePlayListFragment()
        }
    }
}