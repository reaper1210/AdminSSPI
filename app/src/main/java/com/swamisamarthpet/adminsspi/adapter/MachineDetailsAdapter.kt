package com.swamisamarthpet.adminsspi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.databinding.DetailsSingleRowBinding
import javax.inject.Inject

class MachineDetailsAdapter
@Inject
constructor(): ListAdapter<Details, MachineDetailsAdapter.MachineDetailsViewHolder>(Diff) {

    class MachineDetailsViewHolder(private val binding: DetailsSingleRowBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(details: Details){
            binding.apply{
                txtDetailsSingleRowKey.text = details.key
                txtDetailsSingleRowValue.text = details.value
            }
        }

    }

    object Diff : DiffUtil.ItemCallback<Details>() {
        override fun areItemsTheSame(oldItem: Details, newItem: Details): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: Details, newItem: Details): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachineDetailsViewHolder {
        return MachineDetailsViewHolder(
            DetailsSingleRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MachineDetailsViewHolder, position: Int) {
        val machineDetail = getItem(position)
        holder.bind(machineDetail)
    }

}