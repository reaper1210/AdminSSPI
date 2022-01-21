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
import com.swamisamarthpet.adminsspi.activity.MachineDetailsActivity
import com.swamisamarthpet.adminsspi.databinding.MachinesSingleRowBinding
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import javax.inject.Inject

class MachinesAdapter
@Inject
constructor() : ListAdapter<HashMap<String, String>, MachinesAdapter.MyViewHolder>(Diff) {

    class MyViewHolder(private val binding: MachinesSingleRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(machine: HashMap<String,String>) {
            binding.apply {
                txtMachineName.text = machine["machineName"]

                val decompressor = Inflater()
                val stringArray = machine["machineImage"]?.replace("[","")?.replace("]","")?.replace(" ","")?.split(",")
                val bytes = ByteArray(machine["machineImage"]!!.length)
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

                Glide.with(binding.root).load(decompressedImageByteArray).error(R.drawable.ic_launcher_foreground).into(img)

                txtMachineName.ellipsize = TextUtils.TruncateAt.MARQUEE
                txtMachineName.isSingleLine = true
                txtMachineName.isSelected = true
                txtMachineName.isHorizontalFadingEdgeEnabled = true
                itemView.setOnClickListener {
                    val intent = Intent(it.context, MachineDetailsActivity::class.java)
                    intent.putExtra("machineId",machine["machineId"]?.toInt())
                    it.context.startActivity(intent)
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<HashMap<String,String>>() {
        override fun areItemsTheSame(oldItem: HashMap<String,String>, newItem: HashMap<String,String>): Boolean {
            return oldItem["machineId"]?.toInt() == newItem["machineId"]?.toInt()
        }

        override fun areContentsTheSame(oldItem: HashMap<String,String>, newItem: HashMap<String,String>): Boolean {
            return oldItem["machineId"]?.toInt() == newItem["machineId"]?.toInt()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            MachinesSingleRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val machine = getItem(position)
        holder.bind(machine)
    }
}