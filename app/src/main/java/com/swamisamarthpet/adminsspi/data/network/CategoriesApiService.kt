package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.data.model.Category
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import javax.inject.Inject

class CategoriesApiService @Inject constructor(){

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getAllCategories(): List<Category>{
        return client.get {
            url("https://sspi-test-api.herokuapp.com/v1/getAllCategories")
        }
    }

}