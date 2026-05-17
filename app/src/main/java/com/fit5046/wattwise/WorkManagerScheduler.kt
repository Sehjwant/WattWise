package com.fit5046.wattwise

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val WORK_NAME = "energy_budget_monitor"

    fun schedule(context: Context, cumulativeKwh: Double, budgetGoal: Double) {
        val inputData = Data.Builder()
            .putDouble(EnergyBudgetWorker.KEY_CUMULATIVE_KWH, cumulativeKwh)
            .putDouble(EnergyBudgetWorker.KEY_BUDGET_GOAL, budgetGoal)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<EnergyBudgetWorker>(
            repeatInterval         = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}