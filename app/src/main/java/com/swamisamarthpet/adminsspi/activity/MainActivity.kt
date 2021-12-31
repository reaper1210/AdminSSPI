package com.swamisamarthpet.adminsspi.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.ViewPagerAdapter
import com.swamisamarthpet.adminsspi.databinding.ActivityMainBinding
import com.swamisamarthpet.adminsspi.fragment.CategoriesFragment
import com.swamisamarthpet.adminsspi.fragment.OthersFragment
import com.swamisamarthpet.adminsspi.fragment.SupportFragment
import com.swamisamarthpet.adminsspi.ui.SupportViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @InternalAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val fragmentList = arrayListOf(
            SupportFragment(),
            CategoriesFragment(),
            OthersFragment()
        )
        val viewPagerAdapter = ViewPagerAdapter(fragmentList,this.supportFragmentManager,lifecycle)

        binding.apply {
            bottomMenu.setItemSelected(R.id.home)
            viewPagerMainActivity.adapter = viewPagerAdapter
            viewPagerMainActivity.isUserInputEnabled = false
            viewPagerMainActivity.currentItem = 0
            bottomMenu.setItemSelected(R.id.support,true)
            bottomMenu.setOnItemSelectedListener {
                when(it){
                    R.id.support->{
                        viewPagerMainActivity.currentItem = 0
                    }
                    R.id.categories->{
                        viewPagerMainActivity.currentItem = 1
                    }
                    R.id.others->{
                        viewPagerMainActivity.currentItem = 2
                    }
                }
            }
        }

        setContentView(binding.root)
    }
}