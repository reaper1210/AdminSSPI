package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.data.model.Category
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import java.io.File
import javax.inject.Inject

@OptIn(InternalAPI::class)
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

    suspend fun insertCategory(categoryName: String, categoryImage: File): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/insertCategory"){
            body = MultiPartFormDataContent(
                formData {
                    append("categoryName",categoryName)
                    append("categoryImage",categoryImage.readBytes(),Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=$categoryName.png")
                    })
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun updateCategory(categoryId: Int, categoryImage: File): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/updateCategory"){
            body = MultiPartFormDataContent(
                formData {
                    append("categoryId",categoryId)
                    append("categoryImage",categoryImage.readBytes(),Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=${Constants.currentCategory?.categoryName}.png")
                    })
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun deleteCategory(categoryId:Int): Int{
        return client.post("https://sspi-test-api.herokuapp.com/v1/deleteCategory"){
            body = MultiPartFormDataContent(
                formData {
                    append("categoryId",categoryId)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

}