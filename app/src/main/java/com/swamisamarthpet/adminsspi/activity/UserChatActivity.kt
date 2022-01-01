package com.swamisamarthpet.adminsspi.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.swamisamarthpet.adminsspi.R
import com.swamisamarthpet.adminsspi.adapter.SupportMessagesAdapter
import com.swamisamarthpet.adminsspi.data.model.SupportMessage
import com.swamisamarthpet.adminsspi.data.model.User
import com.swamisamarthpet.adminsspi.data.util.SupportApiState
import com.swamisamarthpet.adminsspi.databinding.ActivityUserChatBinding
import com.swamisamarthpet.adminsspi.ui.SupportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.lang.Exception
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class UserChatActivity : AppCompatActivity() {

    private val supportViewModel: SupportViewModel by viewModels()
    private var messageList = ArrayList<SupportMessage>()
    lateinit var currentUser: User
    lateinit var binding: ActivityUserChatBinding
    private val supportMessagesAdapter = SupportMessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserChatBinding.inflate(layoutInflater)

        try{
            intent.apply{
                currentUser = User(getStringExtra("userId")!!,intent.getStringExtra("userName")!!,intent.getStringExtra("phoneNumber")!!)
            }
        }catch (e: NullPointerException){
            Toast.makeText(this@UserChatActivity,"Some Error Occurred",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.apply{
            recyclerUserChat.apply {
                adapter = supportMessagesAdapter
                val linearLayoutManager = LinearLayoutManager(this@UserChatActivity)
                linearLayoutManager.stackFromEnd = true
                layoutManager =  linearLayoutManager
            }
            btnCallUserChat.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:${currentUser.phoneNumber}")
                startActivity(callIntent)
            }
        }

        supportViewModel.getAllMessages(currentUser.userId)
        handleGetAllMessages()

        setContentView(binding.root)
    }

    private fun handleGetAllMessages() {
        lifecycleScope.launchWhenStarted {
            supportViewModel.supportApiStateFlow.collect{ supportApiState ->
                when(supportApiState){
                    is SupportApiState.LoadingGetAllMessages ->{
                        binding.userChatProgressBarLayout.visibility = View.VISIBLE
                    }
                    is SupportApiState.SuccessGetAllMessages -> {
                        messageList = supportApiState.data as ArrayList<SupportMessage>
                        addDateChips()
                        supportMessagesAdapter.submitList(messageList)
                        binding.userChatProgressBarLayout.visibility = View.GONE
                    }
                    is SupportApiState.FailureGetAllMessages -> {
                        Toast.makeText(this@UserChatActivity,"Some Error Occurred While retrieving messages",Toast.LENGTH_SHORT).show()
                    }
                    is SupportApiState.EmptyGetAllMessages -> {
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun addDateChips(){
        if(messageList.size>0){

            messageList.add(0, SupportMessage("",messageList[0].dateAndTime,""))

            var messageListSize = messageList.size
            var i = 2

            while(i<messageListSize) {

                if((messageListSize==1)){
                    break
                }

                if((messageList[i-1].messageFrom!="") and (messageList[i].messageFrom!="")){

                    val previousMessageDate = (SimpleDateFormat("dd", Locale.getDefault()).format(Date(messageList[i-1].dateAndTime))).toInt()
                    val previousMessageMonth = (SimpleDateFormat("MM", Locale.getDefault()).format(Date(messageList[i-1].dateAndTime))).toInt()
                    val previousMessageYear = (SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(messageList[i-1].dateAndTime))).toInt()

                    val date = (SimpleDateFormat("dd", Locale.getDefault()).format(Date(messageList[i].dateAndTime))).toInt()
                    val month = (SimpleDateFormat("MM", Locale.getDefault()).format(Date(messageList[i].dateAndTime))).toInt()
                    val year = (SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(messageList[i].dateAndTime))).toInt()


                    if((year!=previousMessageYear) or (month!=previousMessageMonth) or (date!=previousMessageDate)){
                        println("i $i lastIndex $messageListSize preDate $previousMessageDate date $date message ${messageList[i].message}")
                        messageList.add(i, SupportMessage("",messageList[i].dateAndTime,""))
                        messageListSize++
                    }
                }

                i++

            }
        }
    }

}