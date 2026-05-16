package com.fit5046.wattwise

/**
 * ContextEngine
 *
 * Aggregates sensory inputs (from SmartMeterSimulator CSV stream) with
 * non-sensory inputs (time of day, user budget goal, weekend/holiday flags)
 * to compute a situation state: Normal / Warning / Critical.
 *
 */
object ContextEngine {

    // ── Situation State ───────────────────────────────────────────────────────
    enum class SituationState { NORMAL, WARNING, CRITICAL }

    data class ContextResult(
        val state: SituationState,
        val stateLabel: String,
        val tip: String,
        val alertMessage: String?
    )

    // ── Main compute function ─────────────────────────────────────────────────
    fun compute(
        row: CsvSensorRow,
        budgetGoal: Double,
        cumulativeKwh: Double
    ): ContextResult {

        val budgetFraction = if (budgetGoal > 0) cumulativeKwh / budgetGoal else 0.0

        // Rule 1: Critical — budget exceeded
        if (budgetFraction >= 1.0) {
            return ContextResult(
                state        = SituationState.CRITICAL,
                stateLabel   = "Critical",
                tip          = "Daily budget exceeded — switch off non-essential appliances immediately.",
                alertMessage = "🚨 Budget exceeded: ${String.format("%.1f", cumulativeKwh)} / " +
                        "${String.format("%.1f", budgetGoal)} kWh used today."
            )
        }

        // Rule 2: Critical — peak tariff + high energy
        if (row.tariffPerKwh >= 0.22 && row.energyKwh > 1.5) {
            return ContextResult(
                state        = SituationState.CRITICAL,
                stateLabel   = "Critical",
                tip          = "Peak tariff active and high consumption detected — defer the " +
                        "washing machine and dishwasher to off-peak hours.",
                alertMessage = "⚡ Peak tariff (${String.format("%.2f", row.tariffPerKwh)} AUD/kWh) " +
                        "with high usage — consider deferring appliances."
            )
        }

        // Rule 3: Warning — 80% budget reached
        if (budgetFraction >= 0.8) {
            return ContextResult(
                state        = SituationState.WARNING,
                stateLabel   = "Warning",
                tip          = "You've used ${(budgetFraction * 100).toInt()}% of your daily budget — " +
                        "switch to low-energy activities.",
                alertMessage = "⚠️ Budget alert: ${(budgetFraction * 100).toInt()}% of daily " +
                        "energy budget used."
            )
        }

        // Rule 4: Warning — standby waste
        if (row.occupancyCount == 0 && row.energyKwh > 0.1) {
            return ContextResult(
                state        = SituationState.WARNING,
                stateLabel   = "Warning",
                tip          = "No one is home but energy is active — check for appliances " +
                        "left on standby.",
                alertMessage = "🌿 Standby waste detected: ${String.format("%.2f", row.energyKwh)} " +
                        "kWh consumed with zero occupancy."
            )
        }

        // Rule 5: Warning — high room temperature during peak
        if (row.roomTempC > 32.0 && row.tariffPerKwh >= 0.15) {
            return ContextResult(
                state        = SituationState.WARNING,
                stateLabel   = "Warning",
                tip          = "Room temperature is ${row.roomTempC}°C during peak hours — " +
                        "raise AC set point by 1°C to save up to 10% on cooling costs.",
                alertMessage = null
            )
        }

        // Rule 6: Warning — peak tariff active
        if (row.tariffPerKwh >= 0.22) {
            return ContextResult(
                state        = SituationState.WARNING,
                stateLabel   = "Warning",
                tip          = "Peak tariff active (${String.format("%.2f", row.tariffPerKwh)} AUD/kWh) " +
                        "— consider deferring appliances to off-peak hours.",
                alertMessage = null
            )
        }

        // Rule 6b: Warning — high energy on weekend
        if (row.isWeekend && row.energyKwh > 2.0) {
            return ContextResult(
                state        = SituationState.WARNING,
                stateLabel   = "Warning",
                tip          = "Weekend usage is high — consider spreading appliance loads " +
                        "across the day to stay within budget.",
                alertMessage = "📅 Weekend high usage: ${String.format("%.2f", row.energyKwh)} kWh detected."
            )
        }

        // Rule 7: Normal — weekend or holiday
        if (row.isWeekend || row.isHoliday) {
            return ContextResult(
                state        = SituationState.NORMAL,
                stateLabel   = "Normal",
                tip          = if (row.isHoliday)
                    "Public holiday — home usage likely elevated. Monitor your budget."
                else
                    "Weekend usage patterns active — expect higher occupancy and consumption.",
                alertMessage = null
            )
        }

        // Rule 8: Normal — all good
        return ContextResult(
            state        = SituationState.NORMAL,
            stateLabel   = "Normal",
            tip          = "Your energy usage looks great today! Keep it up.",
            alertMessage = null
        )
    }
}