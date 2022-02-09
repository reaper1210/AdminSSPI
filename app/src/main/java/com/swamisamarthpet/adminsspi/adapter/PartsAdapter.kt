package com.swamisamarthpet.adminsspi.adapter

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.activity.PartDetailsActivity
import com.swamisamarthpet.adminsspi.databinding.PartSingleRowBinding
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import javax.inject.Inject

class PartsAdapter
@Inject
constructor(): ListAdapter<HashMap<String,String>,PartsAdapter.PartsViewHolder>(Diff)
{
    class PartsViewHolder(private val binding: PartSingleRowBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(part: HashMap<String,String>) {
            binding.apply {
                txtPartNameSingleRow.text = part["partName"]

                val decompressor = Inflater()
                val stringArray = part["partImage"]?.replace("[","")?.replace("]","")?.replace(" ","")?.split(",")
                val bytes = ByteArray(part["partImage"]!!.length)
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

                Glide.with(binding.root).load(decompressedImageByteArray).error(R.drawable.ic_launcher_foreground).into(imgPartSingleRow)
                txtPartNameSingleRow.apply{
                    ellipsize = TextUtils.TruncateAt.MARQUEE
                    isSingleLine = true
                    isSelected = true
                    isHorizontalFadingEdgeEnabled = true
                }
                itemView.setOnClickListener { view ->
                    Intent(view.context, PartDetailsActivity::class.java).also{
                        it.putExtra("partId",part["partId"]?.toInt())
                        view.context.startActivity(it)
                    }
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<HashMap<String,String>>() {
        override fun areItemsTheSame(oldItem: HashMap<String,String>, newItem: HashMap<String,String>): Boolean {
            return oldItem["partId"]?.toInt() == newItem["partId"]?.toInt()
        }

        override fun areContentsTheSame(oldItem: HashMap<String,String>, newItem: HashMap<String,String>): Boolean {
            return oldItem["partId"]?.toInt() == newItem["partId"]?.toInt()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartsViewHolder {
        return PartsViewHolder(
            PartSingleRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PartsViewHolder, position: Int) {
        val part = getItem(position)
        holder.bind(part)
    }

}