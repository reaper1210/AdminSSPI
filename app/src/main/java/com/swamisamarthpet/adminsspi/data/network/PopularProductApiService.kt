package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.data.model.PopularProduct
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import javax.inject.Inject

class PopularProductApiService
@Inject constructor(){

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getAllPopularProducts(): List<HashMap<String,String>>{
        return client.get("https://sspi-test-api.herokuapp.com/v1/getAllPopularProducts")
    }

    suspend fun getPopularProductById(productId: Int): PopularProduct {
        return client.post("https://sspi-test-api.herokuapp.com/v1/getPopularProductById"){
            body = MultiPartFormDataContent(
                formData {
                    append("productId",productId)
                }
            )
        }
    }

    suspend fun updatePopularProduct(productId: Int, productPopularity: Int,productName: String, productType: String, productDetails: String, productImages: ArrayList<ByteArray>,productPdf: ByteArray, youtubeVideoLink: String): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/updatePopularProduct"){
            body =  if(productType=="part"){
                MultiPartFormDataContent(
                    formData {
                        append("productId",productId)
                        append("productDetails",productDetails)
                        append("productPopularity",productPopularity)
                        for(productImage in productImages){
                            append("machineImage",productImage, Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=${productName}.png")
                            })
                        }
                        append("adminPassword","SSPI@VASAI")
                    }
                )
            }
            else{
                MultiPartFormDataContent(
                    formData {
                        append("productId",productId)
                        append("productDetails",productDetails)
                        append("productPopularity",productPopularity)
                        append("productYoutubeVideo",youtubeVideoLink)
                        append("productPdf",productPdf, Headers.build {
                            append(HttpHeaders.ContentType, "file/pdf")
                            append(HttpHeaders.ContentDisposition, "filename=${productName}.pdf")
                        })
                        for(productImage in productImages){
                            append("machineImage",productImage, Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=${productName}.png")
                            })
                        }
                        append("adminPassword","SSPI@VASAI")
                    }
                )
            }
        }
    }

    suspend fun deletePopularProduct(productId: Int): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/deletePopularProduct"){
            body =  MultiPartFormDataContent(
                    formData {
                        append("productId",productId)
                        append("adminPassword","SSPI@VASAI")
                    }
                )
        }
    }

}
