package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.data.model.Machine
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import javax.inject.Inject

@OptIn(InternalAPI::class)
class MachineApiService
@Inject constructor() {

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getAllMachines(categoryName:String): List<HashMap<String,String>>{
        return client.post("https://sspi-test-api.herokuapp.com/v1/getAllMachines"){
            body = MultiPartFormDataContent(
                formData {
                    append("categoryName",categoryName)
                }
            )
        }
    }

    suspend fun getMachineById(machineId: Int, categoryName: String): Machine {
        return client.post("https://sspi-test-api.herokuapp.com/v1/getMachineById"){
            body = MultiPartFormDataContent(
                formData {
                    append("machineId",machineId)
                    append("categoryName",categoryName)
                }
            )
        }
    }

    suspend fun deleteMachine(machineId:Int, categoryName: String): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/deleteMachine"){
            body = MultiPartFormDataContent(
                formData {
                    append("machineId",machineId)
                    append("categoryName",categoryName)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun insertMachine(categoryName:String, machineName:String, machineDetails:String,
        machinePDF:ByteArray, machineImages:ArrayList<ByteArray>, youtubeVideoLink:String): Int{
        return client.post("https://sspi-test-api.herokuapp.com/v1/insertMachine"){
            body = MultiPartFormDataContent(
                formData {
                    append("categoryName",categoryName)
                    append("machineName",machineName)
                    append("machineDetails",machineDetails)
                    append("machinePdf",machinePDF, Headers.build {
                        append(HttpHeaders.ContentType, "file/pdf")
                        append(HttpHeaders.ContentDisposition, "filename=${machineName}.pdf")
                    })
                    for(machineImage in machineImages){
                        append("machineImg",machineImage,Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=${machineName}.png")
                        })
                    }
                    append("youtubeVideoLink",youtubeVideoLink)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun updateMachine(categoryName:String, machineId:Int, machineName:String, machineDetails:String,
                              machinePDF:ByteArray, machineImages:ArrayList<ByteArray>, youtubeVideoLink:String): Int{
        return client.post("https://sspi-test-api.herokuapp.com/v1/updateMachine"){
            body = MultiPartFormDataContent(
                formData {
                    append("categoryName",categoryName)
                    append("machineId",machineId)
                    append("machineDetails",machineDetails)
                    append("machinePdf",machinePDF, Headers.build {
                        append(HttpHeaders.ContentType, "file/pdf")
                        append(HttpHeaders.ContentDisposition, "filename=${machineName}.pdf")
                    })
                    for(machineImage in machineImages){
                        append("machineImg",machineImage,Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=${machineName}.png")
                        })
                    }
                    append("youtubeVideoLink",youtubeVideoLink)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun markMachineAsPopular(machineName: String,machineDetails: String, machinePopularity:Int, machinePDF:ByteArray, machineImages: ArrayList<ByteArray>, youtubeVideoLink: String): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/insertPopularProduct"){
            body = MultiPartFormDataContent(
                formData {
                    append("productName",machineName)
                    append("productDetails",machineDetails)
                    append("productType","machine")
                    append("productPopularity",machinePopularity)
                    append("machinePdf",machinePDF, Headers.build {
                        append(HttpHeaders.ContentType, "file/pdf")
                        append(HttpHeaders.ContentDisposition, "filename=${machineName}.pdf")
                    })
                    for(machineImage in machineImages){
                        append("machineImage",machineImage, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=${machineName}.png")
                        })
                    }
                    append("productYoutubeVideo",youtubeVideoLink)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

}