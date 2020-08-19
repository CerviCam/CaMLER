package id.cervicam.mobile.services

import android.content.Context
import id.cervicam.mobile.helper.LocalStorage
import id.cervicam.mobile.helper.Utility
import okhttp3.*
import java.io.File

class MainService {
    enum class HttpMethod(val value: String) {
        GET("GET"),
        POST("POST"),
//        PATCH("PATCH"),
//        DELETE("DELETE")
    }

    companion object {
        private const val API_DOMAIN = "http://34.101.228.172"
        private const val API_KEY = "28e797c9ffda7b1c85191911ad50b35489a2900e"

        private fun getAPIUri(endpoint: String): String {
            return "${API_DOMAIN}${endpoint}"
        }

        fun sendRequest(
            context: Context,
            callback: Callback,
            uri: String,
            method: HttpMethod = HttpMethod.GET,
            body: RequestBody? = null,
            useAuth: Boolean = false
        ) {
            val client: OkHttpClient = OkHttpClient().newBuilder().build()
            val rawRequest: Request.Builder = Request.Builder()
                .url(uri)
                .header("Api-Key", API_KEY)
                .method(method.value, body)

            if (useAuth) {
                rawRequest.header(
                    "Authorization",
                    "Token ${LocalStorage.get(context, LocalStorage.PreferenceKeys.TOKEN.value)}"
                )
            }

            val request: Request = rawRequest.build()
            client.newCall(request).enqueue(callback)
        }

        fun createUser(
            context: Context,
            callback: Callback,
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
        ) {
            sendRequest(
                context,
                callback = callback,
                uri = getAPIUri("/api/v1/users/"),
                method = HttpMethod.POST,
                body = RequestBody.create(
                    MediaType.parse("application/json"),
                    Utility.stringifyJSON(
                        mapOf(
                            "name" to name,
                            "account" to mapOf(
                                "username" to username,
                                "password" to password
                            ),
                            "degree" to mapOf(
                                "instance_name" to instanceName,
                                "name" to degreeName
                            ),
                            "workplace" to mapOf(
                                "country" to workplaceCountry,
                                "province" to workplaceProvince,
                                "city" to workplaceCity,
                                "street_name" to workplaceStreetName,
                                "postal_code" to workplacePostalCode
                            )
                        ) as HashMap<String, Any>
                    )
                )
            )
        }

        fun getToken(context: Context, callback: Callback, username: String, password: String) {
            sendRequest(
                context,
                callback = callback,
                uri = getAPIUri("/api/v1/auth/token/"),
                method = HttpMethod.POST,
                body = RequestBody.create(
                    MediaType.parse("application/json"),
                    Utility.stringifyJSON(
                        mapOf<String, Any>(
                            "username" to username,
                            "password" to password
                        ) as HashMap<String, Any>
                    )
                )
            )
        }

        fun classifyImage(context: Context, callback: Callback, image: File) {
            return sendRequest(
                context,
                callback = callback,
                uri = getAPIUri("/api/v1/cervic-model/classifications/"),
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

        fun getAllClassifications(
            context: Context,
            callback: Callback,
            query: HashMap<String, String>?
        ) {
            var queryString = ""
            if (query != null) {
                queryString = "?"
                for ((key, value) in query) {
                    queryString += "${key}=${value}&"
                }
                queryString = queryString.substring(0, queryString.length - 1)
            }
            sendRequest(
                context,
                callback = callback,
                uri = getAPIUri("/api/v1/cervic-model/classifications/${queryString}"),
                method = HttpMethod.GET,
                useAuth = true
            )
        }

        fun getClassification(context: Context, callback: Callback, id: String) {
            sendRequest(
                context,
                callback = callback,
                uri = getAPIUri("/api/v1/cervic-model/classifications/${id}/"),
                method = HttpMethod.GET,
                useAuth = true
            )
        }
    }
}