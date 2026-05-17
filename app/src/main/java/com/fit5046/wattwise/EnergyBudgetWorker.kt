package com.fit5046.wattwise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class EnergyBudgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID             = "wattwise_budget_alerts"
        const val CHANNEL_NAME           = "WattWise Budget Alerts"
        const val KEY_CUMULATIVE_KWH     = "cumulative_kwh"
        const val KEY_BUDGET_GOAL        = "budget_goal"
    }

    override suspend fun doWork(): Result {
        val cumulativeKwh = inputData.getDouble(KEY_CUMULATIVE_KWH, 0.0)
        val budgetGoal    = inputData.getDouble(KEY_BUDGET_GOAL, 20.0)

        if (budgetGoal <= 0) return Result.success()

        val progress = cumulativeKwh / budgetGoal

        createNotificationChannel()

        when {
            progress >= 1.0 -> sendNotification(
                id      = 1001,
                title   = "⚠️ WattWise — Budget Exceeded",
                message = "You have used ${"%.1f".format(cumulativeKwh)} / ${"%.1f".format(budgetGoal)} kWh " +
                        "(${(progress * 100).toInt()}%). Avoid running high-wattage appliances."
            )
            progress >= 0.8 -> sendNotification(
                id      = 1002,
                title   = "⚡ WattWise — 80% Budget Used",
                message = "You have used ${"%.1f".format(cumulativeKwh)} / ${"%.1f".format(budgetGoal)} kWh. " +
                        "Shift remaining appliances to off-peak hours."
            )
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Budget threshold alerts from WattWise"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }
}