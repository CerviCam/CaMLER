package id.cervicam.mobile.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class MainService {
    companion object {
        private const val API_URL = "https://jsonplaceholder.typicode.com"
        var apiKey: String? = null
        var token: String? = null

        suspend fun fetchClassificationResult(requestId: String): HttpResponse {
            val client = HttpClient()
            val responseBody = client.get<HttpResponse>("$API_URL/photos/$requestId")
            client.close()
            return responseBody
        }

    }
}