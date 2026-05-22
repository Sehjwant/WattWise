package com.fit5046.wattwise

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val WORK_NAME      = "energy_budget_monitor"
    private const val DEMO_WORK_NAME = "energy_budget_demo_trigger"

    fun schedule(context: Context, cumulativeKwh: Double, budgetGoal: Double) {
        val inputData = Data.Builder()
            .putDouble(EnergyBudgetWorker.KEY_CUMULATIVE_KWH, cumulativeKwh)
            .putDouble(EnergyBudgetWorker.KEY_BUDGET_GOAL, budgetGoal)
            .build()

        // ── Periodic worker (production) ──────────────────────────────────────
        // Android enforces a minimum 15-minute interval for PeriodicWorkRequest.
        // setInitialDelay fires the FIRST execution after 15 seconds so the
        // feature is visible during a demo without waiting the full 15 minutes.
        // Subsequent runs then repeat on the standard 15-minute cycle.
        val periodicRequest = PeriodicWorkRequestBuilder<EnergyBudgetWorker>(
            repeatInterval         = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setInitialDelay(15, TimeUnit.SECONDS) // first run fires in 15s for demo
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        // ── One-time immediate demo trigger ───────────────────────────────────
        // Fires a single check 15 seconds after scheduling so the notification
        // is visible immediately in the demo. Uses KEEP so re-scheduling on
        // budgetGoal changes doesn't cancel an already-queued demo trigger.
        val demoRequest = OneTimeWorkRequestBuilder<EnergyBudgetWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            DEMO_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            demoRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(DEMO_WORK_NAME)
    }
}