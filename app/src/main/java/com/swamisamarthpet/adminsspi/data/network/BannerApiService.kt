package com.swamisamarthpet.adminsspi.data.network

import com.google.common.io.Files.append
import com.swamisamarthpet.adminsspi.Constants
import com.swamisamarthpet.adminsspi.data.model.Banner
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.File
import javax.inject.Inject

class BannerApiService @Inject constructor(){

    private val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    suspend fun getAllBanners(): List<Banner> {
        return client.get {
            url("https://sspi-test-api.herokuapp.com/v1/getAllBanners")
        }
    }

    suspend fun insertBanner(bannerImage: ByteArray): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/insertBanner") {
            body = MultiPartFormDataContent(
                formData {
                    append("bannerImage", bannerImage, Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=${Constants.currentCategory?.categoryName}.png")
                    })
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun updateBanner(bannerId: Int, bannerImage: ByteArray): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/updateBanner") {
            body = MultiPartFormDataContent(
                formData {
                    append("bannerId",bannerId)
                    append("bannerImage", bannerImage, Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=${Constants.currentCategory?.categoryName}.png")
                    })
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

    suspend fun deleteBanner(bannerId: Int): Int {
        return client.post("https://sspi-test-api.herokuapp.com/v1/deleteBanner") {
            body = MultiPartFormDataContent(
                formData {
                    append("bannerId",bannerId)
                    append("adminPassword","SSPI@VASAI")
                }
            )
        }
    }

}