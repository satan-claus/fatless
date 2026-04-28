package com.niked.fatless.core.service

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.niked.fatless.core.sensor.StepService

class StepRestartWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val intent = Intent(applicationContext, StepService::class.java)

        try {
            applicationContext.startForegroundService(intent)
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}