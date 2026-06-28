package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.AppNavigationShell
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BusinessViewModel
import com.example.viewmodel.BusinessViewModelFactory
import com.example.worker.InventoryAlertWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: BusinessViewModel by viewModels {
        val app = application as BusinessApplication
        BusinessViewModelFactory(app, app.container.repository, app.container.preferenceStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule WorkManager periodic alert for low stock check (every 24 Hours)
        try {
            val workRequest = PeriodicWorkRequestBuilder<InventoryAlertWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "low_stock_checks",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (ignored: Exception) {}

        setContent {
            val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = darkMode) {
                AppNavigationShell(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

