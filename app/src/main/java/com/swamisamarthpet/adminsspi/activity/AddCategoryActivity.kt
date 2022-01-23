package com.swamisamarthpet.adminsspi.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.databinding.ActivityAddCategoryBinding

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}