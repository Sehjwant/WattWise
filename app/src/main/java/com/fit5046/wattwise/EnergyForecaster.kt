package com.fit5046.wattwise

/**
 * EnergyForecaster
 *
 * On-device next-hour energy consumption forecasting using a
 * pre-trained linear regression model.
 *
 * The model was trained offline in Python on the Kaggle Household
 * Electricity Usage Dataset (Baig, 2024) following the appliance-level
 * modelling approach of Candanedo et al. (2017).
 *
 * Input features (6):
 *   energy_kwh, room_temp_c, occupancy_count,
 *   tariff_per_kwh_usd, is_weekend, day_of_week
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
    val dayOfWeek: Float
)

// ── EnergyForecaster ──────────────────────────────────────────────────────────
class EnergyForecaster {

    companion object {
        private val SCALER_MEANS = floatArrayOf(
            0.326530f,
            19.981168f,
            1.143914f,
            0.140749f,
            0.000000f,
            0.560056f
        )
        private val SCALER_SCALES = floatArrayOf(
            0.622605f,
            7.403289f,
            1.289945f,
            0.044544f,
            1.000000f,
            0.571315f
        )
        private const val INTERCEPT = 0.3265f
        private val WEIGHTS = floatArrayOf(
            0.4821f,
            0.0023f,
            0.0187f,
            0.1543f,
            0.0312f,
            -0.0089f
        )
    }

    // ── Normalise input features using StandardScaler params ──────────────────
    /**
     * Applies StandardScaler normalisation to raw input features.
     * Formula: (value - mean) / scale
     * Matches the Python training preprocessing exactly.
     */
    private fun normalise(input: ForecastInput): FloatArray {
        val raw = floatArrayOf(
            input.energyKwh,
            input.roomTempC,
            input.occupancyCount,
            input.tariffPerKwh,
            input.isWeekend,
            input.dayOfWeek
        )
        return FloatArray(6) { i ->
            (raw[i] - SCALER_MEANS[i]) / SCALER_SCALES[i]
        }
    }
}