package com.pepdeal.infotech.registration

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object KtorHttpClient {

    val client = HttpClient(Darwin) {
        try{
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }catch (e:Exception){
            e.printStackTrace()
            println(e.message)
        }
    }
}
