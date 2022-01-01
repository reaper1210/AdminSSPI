package com.swamisamarthpet.adminsspi.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swamisamarthpet.adminsspi.activity.UserChatActivity
import com.swamisamarthpet.adminsspi.data.model.User
import com.swamisamarthpet.adminsspi.databinding.UserSingleRowSupportFragmentBinding
import javax.inject.Inject

class UsersAdapter
@Inject
constructor():ListAdapter<User,UsersAdapter.UserViewHolder>(Diff) {

    class UserViewHolder(private val binding: UserSingleRowSupportFragmentBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(user: User) {
            binding.apply {
                txtUserName.text = user.userName
                root.setOnClickListener {
                    val intent = Intent(it.context,UserChatActivity::class.java)
                    intent.putExtra("userId",user.userId)
                    intent.putExtra("userName",user.userName)
                    intent.putExtra("phoneNumber",user.phoneNumber)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            UserSingleRowSupportFragmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

}