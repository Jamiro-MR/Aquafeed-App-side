package mx.ucol.aquafeed

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mx.ucol.aquafeed.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.refillWaterButton.setOnClickListener {
            // Handle refill water button click
        }

        binding.fillWaterButton.setOnClickListener {
            // Handle fill water button click
        }

        binding.fillFoodButton.setOnClickListener {
            // Handle fill food button click
        }
    }
}
