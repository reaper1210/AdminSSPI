package com.swamisamarthpet.adminsspi.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.data.model.Details
import com.swamisamarthpet.adminsspi.databinding.DetailsSingleRowBinding
import javax.inject.Inject

class PartDetailsAdapter
@Inject
constructor(): ListAdapter<Details, PartDetailsAdapter.PartDetailsViewHolder>(Diff) {

    lateinit var activityContext: AppCompatActivity

    class PartDetailsViewHolder(private val binding: DetailsSingleRowBinding, private val adapterContext: PartDetailsAdapter, private val activityContext: AppCompatActivity): RecyclerView.ViewHolder(binding.root){

        fun bind(details: Details){
            binding.apply{
                txtDetailsSingleRowKey.text = details.key
                txtDetailsSingleRowValue.text = details.value
                btnRemoveDetailsSingleRow.setOnClickListener {
                    Constants.currentPartDetails.removeAt(layoutPosition)
                    adapterContext.notifyItemRemoved(layoutPosition)
                }
                btnEditDetailsSingleRow.setOnClickListener {
                    val builder = AlertDialog.Builder(activityContext)
                    val activityInflater = activityContext.layoutInflater
                    val view = activityInflater.inflate(R.layout.dialog_add_detail,null)

                    builder.setView(view)
                    val dialog=builder.create()
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val position = layoutPosition

                    val edtTxtAddDetailFeature = view.findViewById<EditText>(R.id.edtTxtAddDetailFeature)
                    edtTxtAddDetailFeature.setText(adapterContext.getItem(position).key)
                    val edtTxtAddDetailDetail = view.findViewById<EditText>(R.id.edtTxtAddDetailDetail)
                    edtTxtAddDetailDetail.setText(adapterContext.getItem(position).value)
                    val btnAddDetail = view.findViewById<ImageView>(R.id.btnAddDetailConfirm)

                    btnAddDetail.setOnClickListener {
                        val feature = edtTxtAddDetailFeature.text.toString()
                        val detail = edtTxtAddDetailDetail.text.toString()
                        if(feature.isNotEmpty() && detail.isNotEmpty()){
                            Constants.currentPartDetails.removeAt(position)
                            Constants.currentPartDetails.add(position,Details(feature, detail))
                            adapterContext.notifyItemChanged(position)
                            dialog.cancel()
                        }
                        else{
                            Toast.makeText(activityContext,"Please fill the fields",
                                Toast.LENGTH_SHORT).show()
                        }

                    }

                    dialog.show()
                }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartDetailsViewHolder {
        return PartDetailsViewHolder(
            DetailsSingleRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            this,
            activityContext
        )
    }

    override fun onBindViewHolder(holder: PartDetailsViewHolder, position: Int) {
        val partDetail = getItem(position)
        holder.bind(partDetail)
    }

}