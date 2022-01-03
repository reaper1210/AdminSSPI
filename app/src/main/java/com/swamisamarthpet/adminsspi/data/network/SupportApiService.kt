package com.swamisamarthpet.adminsspi.data.network

import com.google.firebase.firestore.FirebaseFirestore
import com.swamisamarthpet.adminsspi.data.model.SupportMessage
import com.swamisamarthpet.adminsspi.data.model.User
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import kotlinx.coroutines.CompletableDeferred
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SupportApiService @Inject constructor() {

    private val firebaseClient = FirebaseFirestore.getInstance()

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getAllUsers(): List<User> {
        return client.get {
            url("https://sspi-test-api.herokuapp.com/v1/getAllUsers")
        }
    }

    suspend fun getAllMessages(userId: String): List<SupportMessage> {
        val messagesList = ArrayList<SupportMessage>()
        val def = CompletableDeferred<ArrayList<SupportMessage>>()
        firebaseClient.collection(userId).orderBy("dateAndTime").get().addOnSuccessListener {
            val documents = it.documents
            for(document in documents){
                val message = document["message"].toString()
                val dateAndTime = document["dateAndTime"] as Long
                val messageFrom = document["messageFrom"].toString()
                messagesList.add(SupportMessage(message, dateAndTime, messageFrom))
            }
            def.complete(messagesList)
        }
        return def.await()
    }

    suspend fun updateLastMessageTime(userId:String,time:String):Int{
        return client.post("https://sspi-test-api.herokuapp.com/v1/updateLastMessageTime"){
            body = MultiPartFormDataContent(
                formData {
                    append("userId",userId)
                    append("lastMessageTime",time)
                }
            )
        }
    }

    suspend fun sendMessage(userId: String, message: String): SupportMessage {
        val def = CompletableDeferred<SupportMessage>()
        firebaseClient.collection(userId).orderBy("dateAndTime").get().addOnSuccessListener {
            val lastMessageId = if(it.documents.isNotEmpty()){
                it.documents[it.documents.lastIndex].id.toInt()
            } else{
                1
            }
            val currentMessageId = (lastMessageId+1).toString()
            val date = Calendar.getInstance().time.time
            val messageHashmap = HashMap<String,Any>()
            messageHashmap["message"] = message
            messageHashmap["dateAndTime"] = date
            messageHashmap["messageFrom"] = "admin"
            firebaseClient.collection(userId).document(currentMessageId).set(messageHashmap as Map<String, Any>).addOnSuccessListener {
                def.complete(SupportMessage(message,date,"user"))
            }
        }
        return def.await()
    }

}