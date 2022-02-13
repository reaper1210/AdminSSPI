package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.data.model.Part
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import javax.inject.Inject

class PartApiService
@Inject constructor()
{

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getAllParts(machineName:String): List<HashMap<String,String>>{
        return client.post("https://sspi-test-api.herokuapp.com/v1/getAllParts"){
            body = MultiPartFormDataContent(
                formData {
                    append("machineName",machineName)
                }
            )
        }
    }

    suspend fun getPartById(partId: Int, machineName: String): Part {
        return client.post("https://sspi-test-api.herokuapp.com/v1/getPartById"){
            body = MultiPartFormDataContent(
                formData {
                    append("partId",partId)
                    append("machineName",machineName)
                }
            )
        }
    }

    suspend fun insertPart(machineName: String, partName: String, partDetails: String, partImages: ArrayList<ByteArray>): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/insertPart"){
            body = MultiPartFormDataContent(
                formData {
                    append("machineName",machineName)
                    append("partName",partName)
                    append("partDetails",partDetails)
                    for(partImage in partImages){
                        append("partImage",partImage, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=${partName}.png")
                        })
                    }
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun updatePart(machineName: String, partId: Int, partName: String, partDetails: String, partImages: ArrayList<ByteArray>): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/updatePart"){
            body = MultiPartFormDataContent(
                formData {
                    append("machineName",machineName)
                    append("partId",partId)
                    append("partDetails",partDetails)
                    for(partImage in partImages){
                        append("partImage",partImage, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=${partName}.png")
                        })
                    }
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun deletePart(machineName: String, partId: Int): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/deletePart"){
            body = MultiPartFormDataContent(
                formData {
                    append("machineName",machineName)
                    append("partId",partId)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun markPartAsPopular(partName: String,partDetails: String, partPopularity:Int, partImages: ArrayList<ByteArray>): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/insertPopularProduct"){
            body = MultiPartFormDataContent(
                formData {
                    append("productName",partName)
                    append("productDetails",partDetails)
                    append("productType","part")
                    append("productPopularity",partPopularity)
                    for(partImage in partImages){
                        append("partImage",partImage, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=${partName}.png")
                        })
                    }
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

}