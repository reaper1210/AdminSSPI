package com.swamisamarthpet.adminsspi.adapter

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swamisamarthpet.adminsspi.activity.MachineDetailsActivity
import com.swamisamarthpet.adminsspi.activity.PartDetailsActivity
import com.swamisamarthpet.adminsspi.databinding.OthersFragmentPopularProductSingleRowBinding
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import javax.inject.Inject

class PopularProductAdapter
@Inject
constructor() : ListAdapter<HashMap<String, String>, PopularProductAdapter.MyViewHolder>(Diff) {

    class MyViewHolder(private val binding: OthersFragmentPopularProductSingleRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: HashMap<String,String>) {
            binding.apply {
                txtCategoryName.text = product["productName"]
                val decompressor = Inflater()
                val stringArray = product["productImage"]?.replace("[","")?.replace("]","")?.replace(" ","")?.split(",")
                val bytes = ByteArray(product["productImage"]!!.length)
                for(i in 0..stringArray?.lastIndex!!){
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

                Glide.with(binding.root).load(decompressedImageByteArray).into(imgPopularProduct)

                txtCategoryName.ellipsize = TextUtils.TruncateAt.MARQUEE
                txtCategoryName.isSingleLine = true
                txtCategoryName.isSelected = true
                txtCategoryName.isHorizontalFadingEdgeEnabled = true
                itemView.setOnClickListener { view ->
                    if(product["productType"] == "machine") {
                        val intent = Intent(view.context, MachineDetailsActivity::class.java)
                        intent.putExtra("productId", product["productId"]?.toInt())
                        view.context.startActivity(intent)
                    }
                    else{
                        val intent = Intent(view.context, PartDetailsActivity::class.java)
                        intent.putExtra("productId", product["productId"]?.toInt())
                        view.context.startActivity(intent)
                    }
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<HashMap<String,String>>() {
        override fun areItemsTheSame(oldItem: HashMap<String,String>, newItem: HashMap<String,String>): Boolean {
            return oldItem["productId"]?.toInt() == newItem["productId"]?.toInt()
        }

        override fun areContentsTheSame(oldItem: HashMap<String,String>, newItem: HashMap<String,String>): Boolean {
            return oldItem["productId"]?.toInt() == newItem["productId"]?.toInt()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            OthersFragmentPopularProductSingleRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }
}