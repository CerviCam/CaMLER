package id.cervicam.mobile.services

import android.content.Context
import id.cervicam.mobile.helper.LocalStorage
import id.cervicam.mobile.helper.Utility
import kotlinx.coroutines.CoroutineScope
import okhttp3.*
import java.io.File
import java.io.IOException

class MainService {
    enum class HttpMethod(val value: String) {
        GET("GET"),
        POST("POST"),
        PATCH("PATCH"),
        DELETE("DELETE")
    }
    companion object {
        private const val API_URL = "http://34.101.228.172"
        private const val API_KEY = "28e797c9ffda7b1c85191911ad50b35489a2900e"

        private fun sendRequest(
            context: Context,
            endpoint: String,
            method: HttpMethod = HttpMethod.GET,
            body: RequestBody? = null,
            useAuth: Boolean = false
        ): Response {
            val client: OkHttpClient = OkHttpClient().newBuilder().build()
            val rawRequest: Request.Builder = Request.Builder()
                .url("${API_URL}${endpoint}")
                .header("Api-Key", API_KEY)
                .method(method.value, body)

            if (useAuth) {
                rawRequest.header("Authorization", "Token ${LocalStorage.get(context, LocalStorage.PreferenceKeys.TOKEN.value)}")
            }

            val request: Request = rawRequest.build()
            return client.newCall(request).execute()
        }

        fun createUser(
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
        ): Response {
            return sendRequest(
                context,
                "/api/v1/users/",
                method = HttpMethod.POST,
                body = RequestBody.create(
                    MediaType.parse("application/json"),
                    Utility.stringifyJSON(
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
                    ) as HashMap<String, Any>)
                )
            )
        }

        fun getToken(context: Context, username: String, password: String): Response {
            return sendRequest(
                context,
                "/api/v1/auth/token/",
                method = HttpMethod.POST,
                body = RequestBody.create(
                    MediaType.parse("application/json"),
                    Utility.stringifyJSON(
                    mapOf<String, Any>(
                        "username" to username,
                        "password" to password
                    ) as HashMap<String, Any>)
                )
            )
        }

        fun classifyImage(context: Context, image: File): Response {
            return sendRequest(
                context,
                "/api/v1/cervic-model/classifications/",
                method = HttpMethod.POST,
                body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        Utility.getBasename(image.path),
                        RequestBody.create(
                            MediaType.parse("application/octet-stream"),
                            image
                        )
                    )
                    .build(),
                useAuth = true
            )
        }

        fun getAllClassifications(context: Context): Response {
            return sendRequest(
                context,
                "/api/v1/cervic-model/classifications/",
                method = HttpMethod.GET,
                useAuth = true
            )
        }

        fun getClassification(context: Context, requestId: String): Response {
            return sendRequest(
                context,
                "/api/v1/cervic-model/classifications/${requestId}/",
                method = HttpMethod.GET,
                useAuth = true
            )
        }
    }
}