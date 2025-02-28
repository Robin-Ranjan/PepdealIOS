package com.pepdeal.infotech.support

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters

class GoogleFormService {
    private val client = HttpClient(Darwin) {

    }

    suspend fun submitToGoogleForm(
        name: String,
        mobileNo: String,
        email: String,
        subject: String,
        query: String
    ): Boolean {
        val url =
            "https://docs.google.com/forms/d/e/1FAIpQLSeIh6xmJ9jrQ3YeYvBFbGGm2iAjtOLu1CRXciBz6FUvOJPp5g/formResponse"

        return try {
            val response: HttpResponse = client.submitForm(
                url = url,
                formParameters = parameters {
                    append("entry.1142689591", name)
                    append("entry.732092402", mobileNo)
                    append("entry.379125778", email)
                    append("entry.416826113", subject)
                    append("entry.1330352250", query)
                }
            )

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}