package mx.ucol.aquafeed.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HttpRequest {

    /**
     * Sends an HTTP request and returns the response as a JsonElement.
     *
     * @param requestUrl The endpoint URL.
     * @param requestMethod The HTTP method (e.g., "GET", "POST", "PUT", "DELETE").
     * @param params A map of parameters to send. For GET, these will be sent as query parameters.
     *               For POST, PUT, etc., these will be sent as a JSON body.
     * @return A JsonElement representing the JSON response or an error object.
     */
    suspend fun sendRequest(
        requestUrl: String,
        requestMethod: String,
        params: Map<String, String> = emptyMap()
    ): JsonElement {
        return withContext(Dispatchers.IO) {
            try {
                val finalUrl = buildUrlWithParams(requestUrl, requestMethod, params)
                val connection = setupConnection(finalUrl, requestMethod)

                if (shouldSendBody(requestMethod)) {
                    sendRequestBody(connection, params)
                }

                val response = readResponse(connection)
                connection.disconnect()

                parseResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                createErrorJson(e.localizedMessage)
            }
        }
    }

    /**
     * Builds the final URL with query parameters if the request method is GET.
     *
     * @param requestUrl The base endpoint URL.
     * @param requestMethod The HTTP method.
     * @param params The map of parameters.
     * @return The final URL with query parameters if applicable.
     */
    private fun buildUrlWithParams(
        requestUrl: String,
        requestMethod: String,
        params: Map<String, String>
    ): String {
        return if (isGetRequest(requestMethod) && params.isNotEmpty()) {
            "$requestUrl?${params.map { "${it.key}=${it.value}" }.joinToString("&")}"
        } else {
            requestUrl
        }
    }

    /**
     * Sets up the HTTP connection with the appropriate method and headers.
     *
     * @param urlString The final URL string.
     * @param requestMethod The HTTP method.
     * @return An initialized HttpURLConnection.
     */
    private fun setupConnection(urlString: String, requestMethod: String): HttpURLConnection {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = requestMethod.uppercase()
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doInput = true
        return connection
    }

    /**
     * Determines whether the request method requires sending a request body.
     *
     * @param requestMethod The HTTP method.
     * @return True if the method should send a body, false otherwise.
     */
    private fun shouldSendBody(requestMethod: String): Boolean {
        return requestMethod.uppercase() in listOf("POST", "PUT", "PATCH")
    }

    /**
     * Sends the request body as JSON for methods like POST, PUT, PATCH.
     *
     * @param connection The HttpURLConnection instance.
     * @param params The map of parameters to send in the body.
     */
    private fun sendRequestBody(connection: HttpURLConnection, params: Map<String, String>) {
        connection.doOutput = true
        val jsonParams = Json.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            params
        )
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(jsonParams)
            writer.flush()
        }
    }

    /**
     * Reads the response from the server, handling both successful and error responses.
     *
     * @param connection The HttpURLConnection instance.
     * @return The response as a string.
     */
    private fun readResponse(connection: HttpURLConnection): String {
        val responseCode = connection.responseCode
        val inputStream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            return reader.readText()
        }
    }

    /**
     * Parses the response string into a JsonElement.
     *
     * @param response The response string.
     * @return The parsed JsonElement.
     */
    private fun parseResponse(response: String): JsonElement {
        return Json.parseToJsonElement(response)
    }

    /**
     * Creates a JsonElement representing an error message.
     *
     * @param message The error message.
     * @return A JsonElement containing the error.
     */
    private fun createErrorJson(message: String?): JsonElement {
        return Json.parseToJsonElement(
            """
            { "error": "${message ?: "Unknown error."}"}
            """.trimIndent()
        )
    }

    /**
     * Checks if the HTTP method is GET.
     *
     * @param requestMethod The HTTP method.
     * @return True if GET, false otherwise.
     */
    private fun isGetRequest(requestMethod: String): Boolean {
        return requestMethod.uppercase() == "GET"
    }
}