package mx.ucol.aquafeed.repository

import mx.ucol.aquafeed.utils.HttpRequest
import kotlinx.serialization.json.JsonElement
import mx.ucol.aquafeed.config.Routes

class ParticleRepository(private val httpRequest: HttpRequest) {
    suspend fun getFoodLevel(): JsonElement {
        val params = emptyMap<String, String>()
        return httpRequest.sendRequest(Routes.URL_VAR_FOOD, "GET", params)
    }

    suspend fun getWaterLevel(): JsonElement {
        val params = emptyMap<String, String>()
        return httpRequest.sendRequest(Routes.URL_VAR_WATER, "GET", params)
    }

    suspend fun sendInstruction(instruction: String): JsonElement {
        val params = mapOf("instruction" to instruction)
        return httpRequest.sendRequest(Routes.URL_FUNCTIONS, "POST", params)
    }

    suspend fun saveFcmToken(token: String): JsonElement {
        val params = mapOf("fcm_token" to token)
        return httpRequest.sendRequest(Routes.URL_SAVE_TOKEN, "POST", params)
    }
}
