package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.data.model.Part
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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

}