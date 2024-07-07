package it.unipi.RescuePulse.mobile.setupFragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SetupPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PersonalInfoFragment()
            1 -> EmergencyContactsFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}