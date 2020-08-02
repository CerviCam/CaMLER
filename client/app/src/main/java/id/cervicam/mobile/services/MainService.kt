package id.cervicam.mobile.services

import android.content.Context
import id.cervicam.mobile.helper.LocalStorage
import id.cervicam.mobile.helper.Utility
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod

class MainService {
    companion object {
        private const val API_URL = "http://34.101.228.172"
        private const val API_KEY = "28e797c9ffda7b1c85191911ad50b35489a2900e"

        private suspend fun sendRequest(
            context: Context,
            endpoint: String,
            method: HttpMethod = HttpMethod.Get,
            body: Any = EmptyContent,
            useAuth: Boolean = false
        ): HttpResponse {
            var token: String? = null
            if (useAuth) {
                token = LocalStorage.get(context, LocalStorage.PreferenceKeys.TOKEN.value)
            }

            val client = HttpClient()
            val response = client.request<HttpResponse>("${API_URL}${endpoint}" ) {
                this.method = method
                header("Api-Key", API_KEY)
                if (token != null) {
                    header("Authorization", "Token $token")
                }
                this.body = body
            }
            client.close()
            return response
        }

        suspend fun createUser(
            context: Context,
            name: String,
            username: String,
            password: String,
            instanceName: String = "",
            degreeName: String = "",
            workplaceCountry: String = "",
            workplaceProvince: String = "",
            workplaceCity: String = "",
            workplaceStreetName: String = "",
            workplacePostalCode: String = ""
        ): HttpResponse {
            return sendRequest(
                context,
                "/api/v1/users/",
                method = HttpMethod.Post,
                body = TextContent(Utility.stringifyJSON(
                    mapOf<String, Any>(
                        "name" to name,
                        "account" to mapOf<String, String>(
                            "username" to username,
                            "password" to password
                        ),
                        "degree" to mapOf<String, String>(
                            "instance_name" to instanceName,
                            "name" to degreeName
                        ),
                        "workplace" to mapOf<String, String>(
                            "country" to workplaceCountry,
                            "province" to workplaceProvince,
                            "city" to workplaceCity,
                            "street_name" to workplaceStreetName,
                            "postal_code" to workplacePostalCode
                        )
                    ) as HashMap<String, Any>
                ), contentType = ContentType.Application.Json)
            )
        }

        suspend fun login(context: Context, username: String, password: String): HttpResponse {
            return sendRequest(
                context,
                "$API_URL/api/v1/auth/token/",
                method = HttpMethod.Post,
                body = MultiPartFormDataContent(formData {
                    append("username", username)
                    append("password", password)
                })
            )
        }

        suspend fun fetchClassificationResult(context: Context, requestId: String): HttpResponse {
            return sendRequest(
                context,
                "$API_URL/api/v1/cervic-model/classifications/",
                method = HttpMethod.Get,
                useAuth = true
            )
        }
    }
}