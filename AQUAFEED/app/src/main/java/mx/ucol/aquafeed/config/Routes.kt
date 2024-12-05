package mx.ucol.aquafeed.config

class Routes {
    companion object {
        private const val HOST = "192.168.3.95"
        private const val URL_MAIN  = "http://$HOST:3000"

        const val URL_VAR_FOOD   = "$URL_MAIN/variable/nivel_comida"
        const val URL_VAR_WATER  = "$URL_MAIN/variable/nivel_agua"
        const val URL_FUNCTIONS  = "$URL_MAIN/function/getInstruction"
        const val URL_SAVE_TOKEN = "$URL_MAIN/save-fcm-token"
    }
}