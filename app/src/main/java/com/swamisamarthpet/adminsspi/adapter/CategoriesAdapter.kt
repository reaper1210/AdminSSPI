package com.swamisamarthpet.adminsspi.adapter

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.activity.MachineActivity
import com.swamisamarthpet.adminsspi.data.model.Category
import com.swamisamarthpet.adminsspi.databinding.CategorySingleRowBinding
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import javax.inject.Inject

class CategoriesAdapter
    @Inject
    constructor() : ListAdapter<Category, CategoriesAdapter.MyViewHolder>(Diff)
    {
        class MyViewHolder(private val binding: CategorySingleRowBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(category: Category) {
                binding.apply {
                    txtCategoryName.text = category.categoryName
                    val decompressor = Inflater()
                    val stringArray = category.categoryImage.replace("[","").replace("]","").replace(" ","").split(",")
                    val bytes = ByteArray(7323063)
                    for(i in 0..stringArray.lastIndex){
                        val byte = stringArray[i].toByte()
                        bytes[i] = byte
                    }
                    decompressor.setInput(bytes)
                    val bos = ByteArrayOutputStream(bytes.size)
                    val buf = ByteArray(1024)
                    while (!decompressor.finished()) {
                        val count = decompressor.inflate(buf)
                        bos.write(buf, 0, count)
                    }
                    bos.close()
                    val decompressedImageByteArray = bos.toByteArray()
                    Glide.with(binding.root).load(decompressedImageByteArray).into(imgCategory)
                    txtCategoryName.ellipsize = TextUtils.TruncateAt.MARQUEE
                    txtCategoryName.isSingleLine = true
                    txtCategoryName.isSelected = true
                    txtCategoryName.isHorizontalFadingEdgeEnabled = true
                    itemView.setOnClickListener {
                        Constants.currentCategory = category
                        Constants.currentCategoryImageByteArray = decompressedImageByteArray
                        val intent = Intent(it.context, MachineActivity::class.java)
                        it.context.startActivity(intent)
                    }
                }
            }
        }

        object Diff : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.categoryId == newItem.categoryId
            }

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem == newItem
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                CategorySingleRowBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val category = getItem(position)
            holder.bind(category)
        }
}