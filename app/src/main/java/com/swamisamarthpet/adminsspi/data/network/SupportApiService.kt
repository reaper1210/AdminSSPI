package com.swamisamarthpet.adminsspi.data.network

import com.google.firebase.firestore.FirebaseFirestore
import com.swamisamarthpet.adminsspi.data.model.SupportMessage
import com.swamisamarthpet.adminsspi.data.model.User
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject

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

}