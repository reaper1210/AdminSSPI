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
import com.google.firebase.firestore.FirebaseFirestore
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
    private val db = FirebaseFirestore.getInstance()
    lateinit var currentUser: User
    lateinit var binding: ActivityUserChatBinding
    private val supportMessagesAdapter = SupportMessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserChatBinding.inflate(layoutInflater)

        try{
            intent.apply{
                currentUser = User(getStringExtra("userId")!!,getStringExtra("userName")!!,getStringExtra("phoneNumber")!!,
                    getStringExtra("lastMessageTime")!!)
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
            btnSendMessageUserChat.setOnClickListener {
                val message = edtTxtUserChatMessage.text.toString()
                if(message.isNotEmpty()){
                    supportViewModel.sendMessage(currentUser.userId,message)
                }
            }
        }

        supportViewModel.getAllMessages(currentUser.userId)
        handleResponse()
        initSnapShotListener()

        setContentView(binding.root)
    }

    private fun setValues() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        addDateChips()
        supportMessagesAdapter.submitList(messageList)

        binding.apply {
            recyclerUserChat.apply{
                layoutManager = linearLayoutManager
                adapter = supportMessagesAdapter
                scrollToPosition(messageList.size-1)
            }
        }

    }

    private fun handleResponse() {
        lifecycleScope.launchWhenStarted {
            supportViewModel.supportApiStateFlow.collect{ supportApiState ->
                when(supportApiState){

                    is SupportApiState.LoadingGetAllMessages ->{
                        binding.userChatProgressBarLayout.visibility = View.VISIBLE
                    }
                    is SupportApiState.SuccessGetAllMessages -> {
                        messageList = supportApiState.data as ArrayList<SupportMessage>
                        setValues()
                        binding.userChatProgressBarLayout.visibility = View.GONE
                    }
                    is SupportApiState.FailureGetAllMessages -> {
                        Toast.makeText(this@UserChatActivity,"Some Error Occurred While retrieving messages",Toast.LENGTH_SHORT).show()
                    }


                    is SupportApiState.LoadingSendMessage ->{
                        binding.apply{
                            btnSendMessageUserChatProgressBar.visibility = View.VISIBLE
                            btnSendMessageUserChat.visibility = View.INVISIBLE
                        }
                    }
                    is SupportApiState.SuccessSendMessage ->{
                        binding.apply{
                            btnSendMessageUserChatProgressBar.visibility = View.GONE
                            btnSendMessageUserChat.visibility = View.VISIBLE
                            edtTxtUserChatMessage.text.clear()
                            supportViewModel.updateLastMessageTime(currentUser.userId,supportApiState.data.dateAndTime.toString())
                            handleUpdateResponse()
                        }
                    }
                    is SupportApiState.FailureSendMessage ->{
                        Toast.makeText(this@UserChatActivity,"Some Error Occurred While sending message",Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleUpdateResponse(){
        lifecycleScope.launchWhenStarted {
            supportViewModel.supportApiStateFlow.collect{ supportApiState ->
                when(supportApiState){
                    is SupportApiState.FailureUpdateLastMessageTime -> {
                        Toast.makeText(this@UserChatActivity,"Some Error Occurred",Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
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

    private fun initSnapShotListener(){
        db.collection(currentUser.userId).addSnapshotListener { value, error ->
            if(error==null){
                val documents = value?.documents!!
                messageList.clear()
                for(document in documents){
                    val message = document["message"].toString()
                    val dateAndTime = document["dateAndTime"] as Long
                    val messageFrom = document["messageFrom"].toString()
                    messageList.add(SupportMessage(message, dateAndTime, messageFrom))
                }
                messageList.sortBy {
                    it.dateAndTime
                }
                setValues()
            }
        }
    }

}