package com.example.trainboard
import io.ktor.client.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json




class ApiClient {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            header("X-API-KEY", BuildConfig.API_KEY)
            url{
                protocol = URLProtocol.HTTPS
                host = "int-test1.tram.softwire-lner-dev.co.uk"
            }
        }
    }

    suspend fun get(path: String): String {
        return client.get(path).bodyAsText()
    }
}
