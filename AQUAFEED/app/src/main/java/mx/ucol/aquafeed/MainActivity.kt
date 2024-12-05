package mx.ucol.aquafeed

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import mx.ucol.aquafeed.databinding.ActivityMainBinding
import mx.ucol.aquafeed.repository.ParticleRepository
import mx.ucol.aquafeed.utils.HttpRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var particleRepository: ParticleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        particleRepository = ParticleRepository(HttpRequest())

        b.refillButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                checkAndRefill()
            }
        }

        b.fillWaterButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                sendInstruction("3")
            }
        }

        b.fillFoodButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                sendInstruction("1")
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            startCheckingLevels()
            getAndSendToken()
        }
    }

    private suspend fun startCheckingLevels() {
        while (true) {
            val foodLevel = getFoodLevel()
            val waterLevel = getWaterLevel()

            if (foodLevel != null && waterLevel != null) {
                Log.d("LEVELS", "Food Level: ${getLevelCategory(foodLevel)}, Water Level: ${getLevelCategory(waterLevel)}")

                handleLevelActions(foodLevel, waterLevel)
            }

            delay(10000)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLevelActions(foodLevel: Int, waterLevel: Int) {

        println(foodLevel)
        println(waterLevel)
        if (foodLevel >= 75 && waterLevel >= 75) {
            b.catImage.setImageResource(R.drawable.cat_happy)
        }

        if (foodLevel in 50..74 && waterLevel in 50..74) {
            b.catImage.setImageResource(R.drawable.cat_happy_2)
        }

        if (foodLevel in 25..49 && waterLevel in 25..49) {
            b.catImage.setImageResource(R.drawable.cat_frown)
        }

        if (foodLevel < 25 && waterLevel < 25) {
            b.catImage.setImageResource(R.drawable.cat_sad)
        }

        if (foodLevel == 0 && waterLevel == 0) {
            b.catImage.setImageResource(R.drawable.cat_xx)
        }

        if (foodLevel in 75..100 && waterLevel < 75) {
            b.catImage.setImageResource(R.drawable.cat_full_food)
        }

        if (foodLevel < 75 && waterLevel in 75..100) {
            b.catImage.setImageResource(R.drawable.cat_full_water)
        }

        when (foodLevel) {
            in 0..25 -> {
                Log.d("LEVELS", "Food level is between 0-25, sending refill instruction for food.")
                b.textFoodLabel.text = "¡Ay no! El nivel de comida está muy bajo ($foodLevel%). Rellénalo pronto."
                b.imgFoodStatus.setImageResource(R.drawable.food_pill_empty)
            }
            in 26..50 -> {
                Log.d("LEVELS", "Food level is between 26-50, consider refilling.")
                b.textFoodLabel.text = "Nivel de comida ($foodLevel%) moderado. ¡Es hora de considerar un poco más!"
                b.imgFoodStatus.setImageResource(R.drawable.food_pill_empty)
            }
            in 51..75 -> {
                Log.d("LEVELS", "Food level is between 51-75, sufficient for now.")
                b.textFoodLabel.text = "Comida suficiente por ahora ($foodLevel%). Todo bajo control, pero no olvides revisar."
                b.imgFoodStatus.setImageResource(R.drawable.food_pill_full)
            }
            in 76..100 -> {
                Log.d("LEVELS", "Food level is between 76-100, no action needed.")
                b.textFoodLabel.text = "¡Nivel máximo de comida alcanzado! ($foodLevel%) ¡Bien hecho!"
                b.imgFoodStatus.setImageResource(R.drawable.food_pill_full)
            }
        }

        when (waterLevel) {
            in 0..25 -> {
                Log.d("LEVELS", "Water level is between 0-25, sending refill instruction for water.")
                b.textWaterLabel.text = "¡El agua está a punto de agotarse! Nivel bajo ($waterLevel%). ¡Rellénalo ya!"
                b.imgWaterStatus.setImageResource(R.drawable.water_pill_empty)
            }
            in 26..50 -> {
                Log.d("LEVELS", "Water level is between 26-50, consider refilling.")
                b.textWaterLabel.text = "Nivel de agua ($waterLevel%) moderado. Considera añadir más agua."
                b.imgWaterStatus.setImageResource(R.drawable.water_pill_empty)
            }
            in 51..75 -> {
                Log.d("LEVELS", "Water level is between 51-75, sufficient for now.")
                b.textWaterLabel.text = "Suficiente agua por ahora ($waterLevel%). ¡Todo bien!"
                b.imgWaterStatus.setImageResource(R.drawable.water_pill_full)
            }
            in 76..100 -> {
                Log.d("LEVELS", "Water level is between 76-100, no action needed.")
                b.textWaterLabel.text = "¡Nivel máximo de agua! ($waterLevel%) ¡Estás listo!"
                b.imgWaterStatus.setImageResource(R.drawable.water_pill_full)
            }
        }
    }

    private fun getLevelCategory(level: Int): String {
        return when (level) {
            in 0..25 -> "0-25"
            in 26..50 -> "26-50"
            in 51..75 -> "51-75"
            in 76..100 -> "76-100"
            else -> "Invalid level"
        }
    }


    private suspend fun checkAndRefill() {
        val foodLevel = getFoodLevel()
        val waterLevel = getWaterLevel()

        if (foodLevel != null && waterLevel != null) {
            if (foodLevel <= 50) {
                sendInstruction("1")
            }
            if (waterLevel <= 50) {
                sendInstruction("3")
            }
        }
    }

    private suspend fun getAndSendToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("TOKEN FCM:" , token)
            if (token.isNullOrEmpty()) return

            sendFcmTokenToServer(token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun sendFcmTokenToServer(token: String) {
        try {
            val response = particleRepository.saveFcmToken(token)
            Log.d("TOKEN FCM", "SEND TOKEN: $token")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getFoodLevel(): Int? {
        return try {
            val response = particleRepository.getFoodLevel()
            println("Comida:" + response.toString().toIntOrNull())

            response.toString().toIntOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getWaterLevel(): Int? {
        return try {
            val response = particleRepository.getWaterLevel()
            println("AGUA:" + response.toString().toIntOrNull())

            response.toString().toIntOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun sendInstruction(instruction: String) {
        try {
            val response = particleRepository.sendInstruction(instruction)
            Log.d("Instruction",  response.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}