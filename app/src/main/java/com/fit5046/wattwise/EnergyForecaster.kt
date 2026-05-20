package com.fit5046.wattwise

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * EnergyForecaster
 *
 * On-device next-hour energy consumption forecasting using a
 * pre-trained TensorFlow Lite model (energy_forecast.tflite).
 *
 * The model was trained offline in Python on the Kaggle Household
 * Electricity Usage Dataset (Baig, 2024) following the appliance-level
 * modelling approach of Candanedo et al. (2017).
 *
 * Input features (9):
 *   energy_kwh, room_temp_c, occupancy_count, tariff_per_kwh,
 *   is_weekend, day_of_week, hour_of_day, is_holiday,
 *   cumulative_daily_kwh
 *
 * Output: predicted next-hour energy consumption (kWh)
 *
 * References:
 * TensorFlow. (2024). TensorFlow Lite overview.
 * https://www.tensorflow.org/lite/guide
 * Candanedo et al. (2017). Data driven prediction models of energy use.
 * Energy and Buildings, 140, 81-97.
 */

// ── Input data class ──────────────────────────────────────────────────────────
data class ForecastInput(
    val energyKwh: Float,
    val roomTempC: Float,
    val occupancyCount: Float,
    val tariffPerKwh: Float,
    val isWeekend: Float,
    val dayOfWeek: Float,
    val hourOfDay: Float,
    val isHoliday: Float,
    val cumulativeDailyKwh: Float
)

// ── EnergyForecaster ──────────────────────────────────────────────────────────
class EnergyForecaster(private val context: Context) {

    companion object {
        private const val MODEL_FILE = "energy_forecast.tflite"
        private const val TAG        = "EnergyForecaster"
        private const val NUM_FEATURES = 9

        // StandardScaler parameters matching the Python training pipeline
        // Features: energyKwh, roomTemp, occupancy, tariff,
        //           isWeekend, dayOfWeek, hourOfDay, isHoliday, cumulativeKwh
        private val SCALER_MEANS = floatArrayOf(
            0.3265f, 19.981f, 1.144f, 0.141f,
            0.000f,  2.861f,  11.5f,  0.000f, 12.0f
        )
        private val SCALER_SCALES = floatArrayOf(
            0.623f, 7.403f, 1.290f, 0.045f,
            1.000f, 1.871f, 6.922f, 1.000f,  8.5f
        )
    }

    private var interpreter: Interpreter? = null

    init {
        try {
            val model = loadModelFile()
            interpreter = Interpreter(model)
            Log.d(TAG, "TFLite model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite model: ${e.message}", e)
        }
    }

    // ── Load model from assets ────────────────────────────────────────────────
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val fileInputStream     = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel         = fileInputStream.channel
        val startOffset         = assetFileDescriptor.startOffset
        val declaredLength      = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // ── Normalise input features using StandardScaler params ──────────────────
    private fun normalise(input: ForecastInput): FloatArray {
        val raw = floatArrayOf(
            input.energyKwh,
            input.roomTempC,
            input.occupancyCount,
            input.tariffPerKwh,
            input.isWeekend,
            input.dayOfWeek,
            input.hourOfDay,
            input.isHoliday,
            input.cumulativeDailyKwh
        )
        return FloatArray(NUM_FEATURES) { i ->
            val scale = if (SCALER_SCALES[i] == 0f) 1f else SCALER_SCALES[i]
            (raw[i] - SCALER_MEANS[i]) / scale
        }
    }

    // ── Run TFLite inference ──────────────────────────────────────────────────
    /**
     * Predicts next-hour energy consumption in kWh.
     * Uses the on-device TFLite model for inference.
     * No network connectivity required — fully on-device.
     *
     * @param input ForecastInput containing live sensor values
     * @return predicted kWh clamped to range [0.0, 10.0]
     */
    fun predict(input: ForecastInput): Float {
        val interp = interpreter ?: run {
            Log.w(TAG, "Interpreter not initialised — returning fallback")
            return fallbackPredict(input)
        }

        try {
            val normalised = normalise(input)

            // Input buffer: [1, 9] float32
            val inputBuffer = ByteBuffer
                .allocateDirect(1 * NUM_FEATURES * 4)
                .apply { order(ByteOrder.nativeOrder()) }
            normalised.forEach { inputBuffer.putFloat(it) }
            inputBuffer.rewind()

            // Output buffer: [1, 1] float32
            val outputBuffer = ByteBuffer
                .allocateDirect(1 * 1 * 4)
                .apply { order(ByteOrder.nativeOrder()) }

            interp.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()

            val prediction = outputBuffer.float
            Log.d(TAG, "TFLite prediction: $prediction kWh")

            return prediction.coerceIn(0.0f, 10.0f)

        } catch (e: Exception) {
            Log.e(TAG, "Inference failed: ${e.message}", e)
            return fallbackPredict(input)
        }
    }

    // ── Fallback linear regression if TFLite unavailable ─────────────────────
    private fun fallbackPredict(input: ForecastInput): Float {
        val normalised = normalise(input)
        val weights    = floatArrayOf(0.4821f, 0.0023f, 0.0187f, 0.1543f, 0.0312f, -0.0089f, 0.0201f, 0.0050f, 0.1100f)
        val intercept  = 0.3265f
        var prediction = intercept
        for (i in weights.indices) {
            prediction += weights[i] * normalised[i]
        }
        return prediction.coerceIn(0.0f, 10.0f)
    }

    // ── Release resources ─────────────────────────────────────────────────────
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}