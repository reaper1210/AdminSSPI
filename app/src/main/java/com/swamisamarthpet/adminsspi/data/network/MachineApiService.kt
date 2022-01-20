package com.swamisamarthpet.adminsspi.data.network

import com.swamisamarthpet.adminsspi.data.model.Machine
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import javax.inject.Inject

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

}