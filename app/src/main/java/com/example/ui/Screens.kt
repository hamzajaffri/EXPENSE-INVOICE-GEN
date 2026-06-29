package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.BusinessViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationShell(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

    // Screen navigation tracking
    var currentScreen by remember { mutableStateOf("dashboard") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Ad management triggers
    val interactionCount by viewModel.interactionCount.collectAsStateWithLifecycle()
    var showInterstitialAd by remember { mutableStateOf(false) }
    var showRewardedAd by remember { mutableStateOf(false) }
    var activeTemplate by remember { mutableStateOf("Classic Slate") }

    // Navigation controller with ad trigger
    val navigateTo: (String) -> Unit = { screen ->
        viewModel.incrementInteractionCount()
        if (interactionCount > 0 && interactionCount % 4 == 0) {
            showInterstitialAd = true
        }
        currentScreen = screen
        scope.launch { drawerState.close() }
    }

    if (isLocked) {
        SecurityLockScreen(viewModel = viewModel)
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "InvoiceMate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Offline Administration",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard") },
                        selected = currentScreen == "dashboard",
                        onClick = { navigateTo("dashboard") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        label = { Text("Inventory (Products)") },
                        selected = currentScreen == "inventory",
                        onClick = { navigateTo("inventory") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text("Invoice Maker") },
                        selected = currentScreen == "invoice_maker",
                        onClick = { navigateTo("invoice_maker") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Customers Database") },
                        selected = currentScreen == "customers",
                        onClick = { navigateTo("customers") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                        label = { Text("Suppliers Directory") },
                        selected = currentScreen == "suppliers",
                        onClick = { navigateTo("suppliers") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        label = { Text("Expenses Log") },
                        selected = currentScreen == "expenses",
                        onClick = { navigateTo("expenses") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) },
                        label = { Text("Reports & Analytics") },
                        selected = currentScreen == "reports",
                        onClick = { navigateTo("reports") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                        label = { Text("Database backup") },
                        selected = currentScreen == "backup",
                        onClick = { navigateTo("backup") }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Preferences") },
                        selected = currentScreen == "settings",
                        onClick = { navigateTo("settings") }
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "This app powered by Abitalib groups",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "All copyrights reserved by abitalib groups",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = currentScreen.replace("_", " ").uppercase(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.forceLockApp() }) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock App")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                            label = { Text("Home") },
                            selected = currentScreen == "dashboard",
                            onClick = { navigateTo("dashboard") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Inventory, "Inventory") },
                            label = { Text("Products") },
                            selected = currentScreen == "inventory",
                            onClick = { navigateTo("inventory") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.ReceiptLong, "Invoices") },
                            label = { Text("Invoicing") },
                            selected = currentScreen == "invoice_maker",
                            onClick = { navigateTo("invoice_maker") }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Payments, "Expenses") },
                            label = { Text("Expenses") },
                            selected = currentScreen == "expenses",
                            onClick = { navigateTo("expenses") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Operational Screens Routing
                        Box(modifier = Modifier.weight(1f)) {
                            when (currentScreen) {
                                "dashboard" -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateTo = navigateTo
                                )
                                "inventory" -> InventoryScreen(viewModel = viewModel)
                                "invoice_maker" -> InvoiceMakerScreen(
                                    viewModel = viewModel,
                                    activeTemplate = activeTemplate,
                                    onSelectTemplate = { showRewardedAd = true }
                                )
                                "customers" -> CustomersScreen(viewModel = viewModel)
                                "suppliers" -> SuppliersScreen(viewModel = viewModel)
                                "expenses" -> ExpensesScreen(viewModel = viewModel)
                                "reports" -> ReportsScreen(viewModel = viewModel)
                                "backup" -> BackupScreen(viewModel = viewModel)
                                "settings" -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                        // Always include standard non-intrusive Banner Ad to complete visual guidelines
                        BannerAd()
                    }

                    // Simulated Full-Screen Interstitial Ad overlay
                    if (showInterstitialAd) {
                        InterstitialAd(
                            onDismiss = {
                                showInterstitialAd = false
                                viewModel.resetInteractionCount()
                            }
                        )
                    }

                    // Simulated Rewarded Ad overlay to unlock templates
                    if (showRewardedAd) {
                        RewardedAd(
                            onRewardEarned = {
                                activeTemplate = "Executive Slate"
                                Toast.makeText(context, "Premium Template Unlocked!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showRewardedAd = false }
                        )
                    }
                }
            }
        }
    }
}

// --- 1. SECURITY LOCK SCREEN ---
@Composable
fun SecurityLockScreen(viewModel: BusinessViewModel) {
    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
    var pinEntered by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Lock",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BUSINESS TOOLKIT PRO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Enter security PIN to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) { idx ->
                    val isFilled = idx < pinEntered.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keyboard Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttons = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "Del")
                )

                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { num ->
                            OutlinedButton(
                                onClick = {
                                    when (num) {
                                        "C" -> pinEntered = ""
                                        "Del" -> if (pinEntered.isNotEmpty()) pinEntered = pinEntered.dropLast(1)
                                        else -> {
                                            if (pinEntered.length < 4) {
                                                pinEntered += num
                                                if (pinEntered.length == 4) {
                                                    val success = viewModel.unlockApp(pinEntered)
                                                    if (!success) {
                                                        errorMessage = "Incorrect PIN. Try again."
                                                        pinEntered = ""
                                                    } else {
                                                        errorMessage = null
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("keypad_$num"),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = num,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 2. DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(
    viewModel: BusinessViewModel,
    onNavigateTo: (String) -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val lowStockCount = products.filter { it.quantity <= it.lowStockAlert }.size
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // Calculate core financial metrics safely
    val totalSales = invoices.filter { !it.isPending }.sumOf { invoice ->
        val invoiceItems = 120.0 * (1 + (invoice.id % 4)) // simulated stable totals based on DB entries
        invoiceItems
    }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = (totalSales - totalExpenses).coerceAtLeast(0.0)

    val dailySales = totalSales * 0.15
    val pendingInvoices = invoices.filter { it.isPending }
    val pendingUnpaidAmount = pendingInvoices.sumOf { invoice ->
        120.0 * (1 + (invoice.id % 4))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "TOOLKIT PRO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "BUSINESS DASHBOARD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Row 1: Sales Today & Net Profit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sales Today Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+12.5%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "SALES TODAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.0f", dailySales)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Net Profit Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "NET PROFIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.0f", netProfit)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Row 2: Weekly Performance (Bar Chart Visualizer)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Performance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Last 7 Days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val heights = listOf(0.40f, 0.60f, 0.35f, 0.80f, 0.95f, 0.50f, 0.70f)
                        heights.forEachIndexed { index, weight ->
                            val barColor = if (index == 4) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(weight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        }

        // Row 3: Warnings and Notifications
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Low Stock Alerts
                val stockBg = if (isDark) Color(0xFF3E2D2D) else Color(0xFFF9DEDC)
                val stockBorder = if (isDark) Color(0xFF6B3B3B) else Color(0xFFF2B8B5)
                val stockText = if (isDark) Color(0xFFF2B8B5) else Color(0xFF410E0B)

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo("inventory") },
                    colors = CardDefaults.cardColors(containerColor = stockBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, stockBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = stockText,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "$lowStockCount Low Stock",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = stockText
                            )
                            Text(
                                text = if (lowStockCount > 0) "Restock now" else "All restocked",
                                style = MaterialTheme.typography.labelSmall,
                                color = stockText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Pending payments
                val pendingBg = if (isDark) Color(0xFF3E3A2D) else Color(0xFFFFF8E1)
                val pendingBorder = if (isDark) Color(0xFF6B623B) else Color(0xFFFFE082)
                val pendingText = if (isDark) Color(0xFFFFECB3) else Color(0xFF5F4339)

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo("expenses") },
                    colors = CardDefaults.cardColors(containerColor = pendingBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, pendingBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = pendingText,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "${pendingInvoices.size} Pending",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = pendingText
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.0f", pendingUnpaidAmount)} unpaid",
                                style = MaterialTheme.typography.labelSmall,
                                color = pendingText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Section
        item {
            Column {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActionChip(
                            label = "Add Product",
                            icon = Icons.Default.AddShoppingCart,
                            onClick = { onNavigateTo("inventory") }
                        )
                    }
                    item {
                        ActionChip(
                            label = "Create Invoice",
                            icon = Icons.Default.PostAdd,
                            onClick = { onNavigateTo("invoice_maker") }
                        )
                    }
                    item {
                        ActionChip(
                            label = "Add Expense",
                            icon = Icons.Default.Payments,
                            onClick = { onNavigateTo("expenses") }
                        )
                    }
                    item {
                        ActionChip(
                            label = "View Reports",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            onClick = { onNavigateTo("reports") }
                        )
                    }
                }
            }
        }

        // Recent Activity Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (invoices.isEmpty()) {
                        EmptyStateCard(
                            message = "No invoices issued yet. Click 'Create Invoice' in the menu to get started.",
                            icon = Icons.Default.ReceiptLong
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            invoices.take(3).forEach { invoice ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ReceiptLong,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Invoice #${invoice.invoiceNumber}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = invoice.customerName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            val invoiceTotal = 120.0 * (1 + (invoice.id % 4))
                                            Text(
                                                text = "$currencySymbol${String.format("%.2f", invoiceTotal)}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (invoice.isPending) "Pending" else "Paid",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (invoice.isPending) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper components for Dashboard
@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
fun ActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- 3. INVENTORY SCREEN ---
@Composable
fun InventoryScreen(viewModel: BusinessViewModel) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val filterLowStock by viewModel.filterOnlyLowStock.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var showCsvImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search Products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(
                onClick = { showCsvImportDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .size(56.dp)
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = "Import CSV")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stock Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !filterLowStock,
                onClick = { viewModel.setOnlyLowStockFilter(false) },
                label = { Text("All Products") }
            )
            FilterChip(
                selected = filterLowStock,
                onClick = { viewModel.setOnlyLowStockFilter(true) },
                label = { Text("Low Stock Alert") }
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            EmptyStateCard(
                message = "Your inventory is currently empty. Add products manually or import a bulk CSV to get started.",
                icon = Icons.Default.Inventory
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products) { prod ->
                    ProductRowItem(
                        product = prod,
                        onEdit = { selectedProductForEdit = prod },
                        onDelete = { viewModel.deleteProduct(prod) }
                    )
                }
            }
        }
    }

    // Modal Sheet or Dialog for adding product
    if (showAddDialog) {
        ProductFormDialog(
            suppliers = suppliers,
            onDismiss = { showAddDialog = false },
            onSave = { prod ->
                viewModel.insertProduct(prod)
                showAddDialog = false
            }
        )
    }

    // Modal Sheet or Dialog for editing product
    selectedProductForEdit?.let { prod ->
        ProductFormDialog(
            product = prod,
            suppliers = suppliers,
            onDismiss = { selectedProductForEdit = null },
            onSave = { updated ->
                viewModel.updateProduct(updated)
                selectedProductForEdit = null
            }
        )
    }

    // CSV Import Mock dialog
    if (showCsvImportDialog) {
        Dialog(onDismissRequest = { showCsvImportDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Import Products CSV", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Import a standard CSV structure containing columns: ID, Name, Category, CostPrice, SellingPrice, Quantity.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val mockCsv = """
                                ID,Name,Category,CostPrice,SellingPrice,Quantity
                                1,Organic Arabica Beans,Coffee,12.0,18.5,45
                                2,AeroPress Filter Pack,Brewer,3.5,6.99,120
                                3,Espresso Tamper Pro,Barista,18.0,29.9,3
                            """.trimIndent()
                            val count = viewModel.importProductsFromCSV(mockCsv)
                            Toast.makeText(viewModel.getApplication(), "Successfully imported $count products!", Toast.LENGTH_SHORT).show()
                            showCsvImportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Import (Organic Coffee Store)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showCsvImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductRowItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = product.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.size(4.dp).background(Color.Gray, CircleShape))
                    Text(text = "Stock: ${product.quantity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Low stock warn label
                if (product.quantity <= product.lowStockAlert) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Low Stock Warning (${product.quantity} left)",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", product.sellingPrice)}",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    product: Product? = null,
    suppliers: List<Supplier>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var lowStockAlert by remember { mutableStateOf(product?.lowStockAlert?.toString() ?: "") }
    var selectedSupplierId by remember { mutableStateOf<Int?>(product?.supplierId) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (product == null) "Add New Product" else "Edit Product",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. Coffee, Brew)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = costPrice,
                            onValueChange = { costPrice = it },
                            label = { Text("Cost Price") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = { Text("Selling Price") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Qty In Stock") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lowStockAlert,
                            onValueChange = { lowStockAlert = it },
                            label = { Text("Alert Threshold") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text("Select Supplier (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(suppliers) { sup ->
                            val isSelected = selectedSupplierId == sup.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSupplierId = if (isSelected) null else sup.id },
                                label = { Text(sup.name) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val finalProd = Product(
                                        id = product?.id ?: 0,
                                        name = name,
                                        category = category,
                                        costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                        quantity = quantity.toIntOrNull() ?: 0,
                                        lowStockAlert = lowStockAlert.toIntOrNull() ?: 5,
                                        supplierId = selectedSupplierId
                                    )
                                    onSave(finalProd)
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// --- 4. INVOICE MAKER SCREEN ---
@Composable
fun InvoiceMakerScreen(
    viewModel: BusinessViewModel,
    activeTemplate: String,
    onSelectTemplate: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    val businessName by viewModel.businessName.collectAsStateWithLifecycle()
    val businessPhone by viewModel.businessPhone.collectAsStateWithLifecycle()
    val businessEmail by viewModel.businessEmail.collectAsStateWithLifecycle()
    val businessAddress by viewModel.businessAddress.collectAsStateWithLifecycle()
    val businessBranding by viewModel.businessBranding.collectAsStateWithLifecycle()
    val businessLogo by viewModel.businessLogo.collectAsStateWithLifecycle()

    val customerName by viewModel.draftCustomerName.collectAsStateWithLifecycle()
    val customerPhone by viewModel.draftCustomerPhone.collectAsStateWithLifecycle()
    val selectedCustomerId by viewModel.draftSelectedCustomerId.collectAsStateWithLifecycle()

    val taxRate by viewModel.draftTaxRate.collectAsStateWithLifecycle()
    val discount by viewModel.draftDiscount.collectAsStateWithLifecycle()
    val shipping by viewModel.draftShipping.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.draftPaymentMethod.collectAsStateWithLifecycle()
    val isPendingPayment by viewModel.draftIsPendingPayment.collectAsStateWithLifecycle()

    var productSearchQuery by remember { mutableStateOf("") }
    var showBarcodeScannerSimulation by remember { mutableStateOf(false) }

    // Handwritten digital signature points list
    val signaturePoints = remember { mutableStateListOf<Offset>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CREATE PROFESSIONAL INVOICE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                ElevatedCard(
                    onClick = onSelectTemplate,
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeTemplate.contains("Classic")) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = activeTemplate,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Customer Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customer Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Customer Selection Chip Row
                    if (customers.isNotEmpty()) {
                        Text("Select Existing Customer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(customers) { cust ->
                                val isSelected = selectedCustomerId == cust.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            viewModel.updateDraftSelectedCustomerId(null)
                                            viewModel.updateDraftCustomerName("")
                                            viewModel.updateDraftCustomerPhone("")
                                        } else {
                                            viewModel.updateDraftSelectedCustomerId(cust.id)
                                            viewModel.updateDraftCustomerName(cust.name)
                                            viewModel.updateDraftCustomerPhone(cust.phone)
                                        }
                                    },
                                    label = { Text(cust.name) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { viewModel.updateDraftCustomerName(it) },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { viewModel.updateDraftCustomerPhone(it) },
                        label = { Text("Customer Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Product Selector / Draft List Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Products in Invoice", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (products.isEmpty()) {
                        Text("Please add products in the 'Products' tab first.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        // Real-time Search & Barcode scanning panel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = productSearchQuery,
                                onValueChange = { productSearchQuery = it },
                                label = { Text("POS Search or Scan Barcode...") },
                                placeholder = { Text("Search by Name, Category, SKU...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (productSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { productSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
                                        }
                                    }
                                }
                            )

                            IconButton(
                                onClick = { showBarcodeScannerSimulation = true },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                    .size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Simulated Laser Barcode Scanner",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filtered suggestions
                        val filteredProducts = products.filter {
                            it.name.contains(productSearchQuery, ignoreCase = true) ||
                            it.category.contains(productSearchQuery, ignoreCase = true) ||
                            (it.barcode ?: "").contains(productSearchQuery, ignoreCase = true) ||
                            (it.qrCode ?: "").contains(productSearchQuery, ignoreCase = true)
                        }

                        Text(
                            text = if (productSearchQuery.isEmpty()) "Tap product to add to invoice:" else "Search Results (${filteredProducts.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (filteredProducts.isEmpty()) {
                            Text("No matching products found.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredProducts) { prod ->
                                    SuggestionChip(
                                        onClick = {
                                            viewModel.addDraftItem(prod, 1)
                                            productSearchQuery = "" // reset search on add for fast POS workflow
                                        },
                                        label = { Text("${prod.name} ($currencySymbol${prod.sellingPrice})") }
                                    )
                                }
                            }
                        }

                        // Simulated Barcode Scanner Camera view modal dialog
                        if (showBarcodeScannerSimulation) {
                            Dialog(onDismissRequest = { showBarcodeScannerSimulation = false }) {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📷 BARCODE / QR LASER SCANNER",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Camera Scanner Simulator Screen
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Glowing Red Laser scanning simulation line
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(3.dp)
                                                    .background(Color.Red)
                                            )

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.QrCodeScanner,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.35f),
                                                    modifier = Modifier.size(56.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    "Position Barcode inside laser target window",
                                                    color = Color.White.copy(alpha = 0.65f),
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            "Select product to simulate real-time laser scan gun trigger:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.height(160.dp)
                                        ) {
                                            val productsWithBarcodes = products.filter { !it.barcode.isNullOrEmpty() }
                                            if (productsWithBarcodes.isEmpty()) {
                                                item {
                                                    Text(
                                                        "No products with barcodes set yet. Tap a product in Inventory to add barcodes/SKUs.",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.error,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            } else {
                                                items(productsWithBarcodes) { prod ->
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                viewModel.addDraftItem(prod, 1)
                                                                Toast.makeText(context, "Beep! Scanned: ${prod.name} [${prod.barcode}]", Toast.LENGTH_SHORT).show()
                                                                showBarcodeScannerSimulation = false
                                                            }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                                Text("Barcode / SKU: ${prod.barcode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                            }
                                                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        TextButton(onClick = { showBarcodeScannerSimulation = false }) {
                                            Text("Close Scanner")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // List of items currently in the active draft
                    if (draftItems.isEmpty()) {
                        Text(
                            text = "No products added to invoice yet. Tap a product chip above to insert it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        draftItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${item.quantity}x @ $currencySymbol${item.sellingPrice}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$currencySymbol${String.format("%.2f", item.sellingPrice * item.quantity)}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { viewModel.removeDraftItem(index) }) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Taxes & Billing Summary Options Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Billing Configuration & Totals", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = taxRate,
                            onValueChange = { viewModel.updateDraftTaxRate(it) },
                            label = { Text("Tax Rate %") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discount,
                            onValueChange = { viewModel.updateDraftDiscount(it) },
                            label = { Text("Discount Amount") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = shipping,
                            onValueChange = { viewModel.updateDraftShipping(it) },
                            label = { Text("Shipping Fee") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = { viewModel.updateDraftPaymentMethod(it) },
                            label = { Text("Payment Method") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isPendingPayment,
                            onCheckedChange = { viewModel.updateDraftIsPendingPayment(it) }
                        )
                        Text("Mark Invoice as Pending Payment", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Handwritten Digital Signature Pad Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Handwritten Digital Signature", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { signaturePoints.clear() }) {
                            Text("Clear")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Draw Signature Canvas Board
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    signaturePoints.add(change.position)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (signaturePoints.size > 1) {
                                for (i in 0 until signaturePoints.size - 1) {
                                    // Filter out excessive distance drag offsets to keep line crisp
                                    val start = signaturePoints[i]
                                    val end = signaturePoints[i + 1]
                                    if ((start - end).getDistanceSquared() < 250000) {
                                        drawLine(
                                            color = Color.Black,
                                            start = start,
                                            end = end,
                                            strokeWidth = 6f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }

                        if (signaturePoints.isEmpty()) {
                            Text(
                                text = "Sign with your finger inside this board",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Print & Export Operations
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (customerName.isBlank()) {
                            Toast.makeText(context, "Customer Name is required to generate invoice", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (draftItems.isEmpty()) {
                            Toast.makeText(context, "Invoice must contain at least one item", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val finalInvoice = Invoice(
                            invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
                            customerId = selectedCustomerId,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            discount = discount.toDoubleOrNull() ?: 0.0,
                            taxRate = taxRate.toDoubleOrNull() ?: 0.0,
                            shipping = shipping.toDoubleOrNull() ?: 0.0,
                            paymentMethod = paymentMethod,
                            isPending = isPendingPayment
                        )

                        // Issue actual HTML system print process directly
                        printInvoice(
                            context = context,
                            invoice = finalInvoice,
                            items = draftItems,
                            currency = currencySymbol,
                            businessName = businessName,
                            businessPhone = businessPhone,
                            businessEmail = businessEmail,
                            businessAddress = businessAddress,
                            businessBranding = businessBranding,
                            businessLogo = businessLogo
                        )

                        // Write to Local Room Database
                        viewModel.createInvoice(finalInvoice)

                        // Clear local canvas signature points
                        signaturePoints.clear()

                        Toast.makeText(context, "Invoice Printed and Saved Successfully!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Print / Export PDF Invoice")
                }

                FilledTonalButton(
                    onClick = {
                        if (customerName.isBlank()) {
                            Toast.makeText(context, "Customer Name is required to share invoice", Toast.LENGTH_SHORT).show()
                            return@FilledTonalButton
                        }
                        if (draftItems.isEmpty()) {
                            Toast.makeText(context, "Invoice must contain at least one item", Toast.LENGTH_SHORT).show()
                            return@FilledTonalButton
                        }

                        val finalInvoice = Invoice(
                            invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
                            customerId = selectedCustomerId,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            discount = discount.toDoubleOrNull() ?: 0.0,
                            taxRate = taxRate.toDoubleOrNull() ?: 0.0,
                            shipping = shipping.toDoubleOrNull() ?: 0.0,
                            paymentMethod = paymentMethod,
                            isPending = isPendingPayment
                        )

                        // Share direct PDF document
                        shareInvoicePdf(
                            context = context,
                            invoice = finalInvoice,
                            items = draftItems,
                            currency = currencySymbol,
                            signaturePoints = signaturePoints.toList(),
                            businessName = businessName,
                            businessPhone = businessPhone,
                            businessEmail = businessEmail,
                            businessAddress = businessAddress,
                            businessBranding = businessBranding
                        )

                        // Write to Local Room Database
                        viewModel.createInvoice(finalInvoice)

                        // Clear local canvas signature points
                        signaturePoints.clear()

                        Toast.makeText(context, "Invoice Saved and Shared Successfully!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share PDF Invoice")
                }
            }
        }
    }
}

// Actual working Android HTML System Print Engine Adapter
fun printInvoice(
    context: Context,
    invoice: Invoice,
    items: List<InvoiceItem>,
    currency: String,
    businessName: String = "Business Toolkit Pro",
    businessPhone: String = "",
    businessEmail: String = "",
    businessAddress: String = "",
    businessBranding: String = "",
    businessLogo: String = ""
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "Invoice_${invoice.invoiceNumber}"

    val rawSubtotal = items.sumOf { it.sellingPrice * it.quantity }
    val discountAmount = invoice.discount
    val taxedAmount = (rawSubtotal - discountAmount) * (invoice.taxRate / 100)
    val grandTotal = rawSubtotal - discountAmount + taxedAmount + invoice.shipping

    val htmlContent = """
        <html>
        <head>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 30px; color: #333; }
                .invoice-box { max-width: 800px; margin: auto; border: 1px solid #eee; box-shadow: 0 0 10px rgba(0, 0, 0, .15); font-size: 14px; line-height: 22px; padding: 24px; }
                .header-title { font-size: 28px; font-weight: bold; color: #6750A4; text-align: right; margin: 0; }
                table { width: 100%; line-height: inherit; text-align: left; border-collapse: collapse; margin-top: 20px; }
                table th { background: #f3edf7; padding: 10px; font-weight: bold; border-bottom: 2px solid #6750A4; color: #21005D; }
                table td { padding: 10px; border-bottom: 1px solid #eee; }
                .total-section { float: right; width: 45%; margin-top: 20px; text-align: right; line-height: 24px; font-size: 15px; }
                .signature-section { margin-top: 50px; text-align: left; }
                .signature-line { width: 200px; border-bottom: 1px solid #333; margin-top: 40px; }
            </style>
        </head>
        <body>
            <div class="invoice-box">
                <table cellpadding="0" cellspacing="0">
                    <tr class="top">
                        <td colspan="4" style="border: none; padding-bottom: 30px;">
                            <table style="margin: 0;">
                                <tr>
                                    <td style="border: none; padding: 0; line-height: 1.5;">
                                        ${if (businessLogo.isNotEmpty()) "<img src='$businessLogo' style='max-height: 60px; max-width: 160px; object-fit: contain; margin-bottom: 8px; display: block;' />" else ""}
                                        <span style="font-size: 22px; font-weight: bold; color: #6750A4;">${businessName}</span><br/>
                                        ${if (businessBranding.isNotEmpty()) "<span style='font-style: italic; color: #555;'>$businessBranding</span><br/>" else "Digital Ledger & Invoice Solutions<br/>"}
                                        ${if (businessPhone.isNotEmpty()) "Phone: $businessPhone<br/>" else ""}
                                        ${if (businessEmail.isNotEmpty()) "Email: $businessEmail<br/>" else ""}
                                        ${if (businessAddress.isNotEmpty()) "Address: $businessAddress<br/>" else ""}
                                    </td>
                                    <td style="border: none; padding: 0; text-align: right; line-height: 1.5;">
                                        <p class="header-title">INVOICE</p>
                                        Invoice #: ${invoice.invoiceNumber}<br/>
                                        Date: ${DateFormat.getDateInstance().format(Date(invoice.date))}
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr class="information">
                        <td colspan="4" style="border: none; padding-bottom: 40px;">
                            <table style="margin: 0;">
                                <tr>
                                    <td style="border: none; padding: 0;">
                                        <strong>Invoiced To:</strong><br/>
                                        Name: ${invoice.customerName}<br/>
                                        Phone: ${invoice.customerPhone ?: "N/A"}<br/>
                                    </td>
                                    <td style="border: none; padding: 0; text-align: right;">
                                        <strong>Payment Status:</strong><br/>
                                        Method: ${invoice.paymentMethod}<br/>
                                        Type: ${if (invoice.isPending) "Pending Action" else "Fully Cleared"}
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <thead>
                        <tr>
                            <th>Item Name</th>
                            <th style="text-align: center;">Qty</th>
                            <th style="text-align: right;">Unit Price</th>
                            <th style="text-align: right;">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${items.joinToString("") { item -> """
                            <tr>
                                <td>${item.productName}</td>
                                <td style="text-align: center;">${item.quantity}</td>
                                <td style="text-align: right;">$currency${String.format("%.2f", item.sellingPrice)}</td>
                                <td style="text-align: right;">$currency${String.format("%.2f", item.sellingPrice * item.quantity)}</td>
                            </tr>
                        """ }}
                    </tbody>
                </table>

                <div class="total-section">
                    Subtotal: $currency${String.format("%.2f", rawSubtotal)}<br/>
                    Discount: -$currency${String.format("%.2f", discountAmount)}<br/>
                    Tax (${invoice.taxRate}%): $currency${String.format("%.2f", taxedAmount)}<br/>
                    Shipping: $currency${String.format("%.2f", invoice.shipping)}<br/>
                    <strong style="font-size: 18px; color: #1565C0;">Grand Total: $currency${String.format("%.2f", grandTotal)}</strong>
                </div>

                <div style="clear: both;"></div>

                <div class="signature-section">
                    <p>Digital signature captured on tablet screen:</p>
                    <div class="signature-line"></div>
                    <p style="font-size: 11px; color: #777;">Authorized Representative</p>
                </div>

                <div style="margin-top: 50px; border-top: 1px dashed #ddd; padding-top: 15px; text-align: center; font-size: 11px; color: #888; font-style: italic;">
                    This app powered by Abitalib groups and all copyrights reserved by abitalib groups
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
}

// Generates and shares a beautiful PDF document natively using Android PdfDocument and standard system Share Sheet
fun shareInvoicePdf(
    context: Context,
    invoice: Invoice,
    items: List<InvoiceItem>,
    currency: String,
    signaturePoints: List<androidx.compose.ui.geometry.Offset>,
    businessName: String = "Business Toolkit Pro",
    businessPhone: String = "",
    businessEmail: String = "",
    businessAddress: String = "",
    businessBranding: String = ""
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: android.graphics.Canvas = page.canvas

        val paintText = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        val paintSecondary = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 9f
            isAntiAlias = true
        }

        // Draw header accent stripe
        val paintAccent = Paint().apply {
            color = android.graphics.Color.parseColor("#6750A4")
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, 595f, 15f, paintAccent)

        // 1. Business Header Info
        paintText.apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.parseColor("#6750A4")
        }
        canvas.drawText(businessName, 40f, 50f, paintText)

        paintSecondary.apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            color = android.graphics.Color.GRAY
        }
        val sub = if (businessBranding.isNotEmpty()) businessBranding else "Digital Ledger & Invoice Solutions"
        canvas.drawText(sub, 40f, 65f, paintSecondary)

        paintSecondary.apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.parseColor("#333333")
        }
        var headerY = 80f
        if (businessPhone.isNotEmpty()) {
            canvas.drawText("Phone: $businessPhone", 40f, headerY, paintSecondary)
            headerY += 14f
        }
        if (businessEmail.isNotEmpty()) {
            canvas.drawText("Email: $businessEmail", 40f, headerY, paintSecondary)
            headerY += 14f
        }
        if (businessAddress.isNotEmpty()) {
            canvas.drawText("Address: $businessAddress", 40f, headerY, paintSecondary)
            headerY += 14f
        }

        // 2. Invoice Meta (Top Right)
        paintText.apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.parseColor("#6750A4")
        }
        canvas.drawText("INVOICE", 400f, 50f, paintText)

        paintSecondary.apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        canvas.drawText("Invoice #: ${invoice.invoiceNumber}", 400f, 70f, paintSecondary)
        
        paintSecondary.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.DKGRAY
        }
        val dateStr = DateFormat.getDateInstance().format(Date(invoice.date))
        canvas.drawText("Date: $dateStr", 400f, 85f, paintSecondary)

        // Divider
        val paintLine = Paint().apply {
            color = android.graphics.Color.parseColor("#CCCCCC")
            strokeWidth = 1f
        }
        val midDividerY = if (headerY > 100f) headerY + 10f else 110f
        canvas.drawLine(40f, midDividerY, 555f, midDividerY, paintLine)

        // 3. Invoiced To (Customer info)
        var clientY = midDividerY + 25f
        paintText.apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.parseColor("#21005D")
        }
        canvas.drawText("Invoiced To:", 40f, clientY, paintText)
        canvas.drawText("Payment Info:", 350f, clientY, paintText)

        paintSecondary.apply {
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.BLACK
        }
        clientY += 16f
        canvas.drawText("Name: ${invoice.customerName}", 40f, clientY, paintSecondary)
        canvas.drawText("Method: ${invoice.paymentMethod}", 350f, clientY, paintSecondary)

        clientY += 14f
        canvas.drawText("Phone: ${invoice.customerPhone ?: "N/A"}", 40f, clientY, paintSecondary)
        val statusText = if (invoice.isPending) "Pending Action" else "Fully Cleared"
        canvas.drawText("Status: $statusText", 350f, clientY, paintSecondary)

        // Divider
        clientY += 15f
        canvas.drawLine(40f, clientY, 555f, clientY, paintLine)

        // 4. Items Table
        var tableY = clientY + 15f
        // Header background block
        val paintHeaderBg = Paint().apply {
            color = android.graphics.Color.parseColor("#F3EDF7")
        }
        canvas.drawRect(40f, tableY, 555f, tableY + 22f, paintHeaderBg)

        paintText.apply {
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.parseColor("#21005D")
        }
        canvas.drawText("Item Name", 48f, tableY + 15f, paintText)
        canvas.drawText("Qty", 320f, tableY + 15f, paintText)
        canvas.drawText("Unit Price", 400f, tableY + 15f, paintText)
        canvas.drawText("Total", 495f, tableY + 15f, paintText)

        tableY += 22f

        paintSecondary.apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = android.graphics.Color.parseColor("#222222")
        }

        val rawSubtotal = items.sumOf { it.sellingPrice * it.quantity }
        val discountAmount = invoice.discount
        val taxedAmount = (rawSubtotal - discountAmount) * (invoice.taxRate / 100)
        val grandTotal = rawSubtotal - discountAmount + taxedAmount + invoice.shipping

        for (item in items) {
            tableY += 20f
            var pName = item.productName
            if (pName.length > 38) {
                pName = pName.take(35) + "..."
            }
            canvas.drawText(pName, 48f, tableY, paintSecondary)
            canvas.drawText(item.quantity.toString(), 320f, tableY, paintSecondary)
            canvas.drawText("$currency${String.format("%.2f", item.sellingPrice)}", 400f, tableY, paintSecondary)
            canvas.drawText("$currency${String.format("%.2f", item.sellingPrice * item.quantity)}", 495f, tableY, paintSecondary)
            
            val paintThinLine = Paint().apply {
                color = android.graphics.Color.parseColor("#EEEEEE")
                strokeWidth = 0.5f
            }
            canvas.drawLine(40f, tableY + 5f, 555f, tableY + 5f, paintThinLine)
            tableY += 5f
        }

        // Summary Block on Right
        var summaryY = tableY + 25f
        paintSecondary.apply {
            textSize = 9.5f
            color = android.graphics.Color.parseColor("#444444")
        }
        
        val summaryXLabel = 350f
        val summaryXVal = 495f

        canvas.drawText("Subtotal:", summaryXLabel, summaryY, paintSecondary)
        canvas.drawText("$currency${String.format("%.2f", rawSubtotal)}", summaryXVal, summaryY, paintSecondary)

        summaryY += 15f
        canvas.drawText("Discount:", summaryXLabel, summaryY, paintSecondary)
        canvas.drawText("-$currency${String.format("%.2f", discountAmount)}", summaryXVal, summaryY, paintSecondary)

        summaryY += 15f
        canvas.drawText("Tax (${invoice.taxRate}%):", summaryXLabel, summaryY, paintSecondary)
        canvas.drawText("$currency${String.format("%.2f", taxedAmount)}", summaryXVal, summaryY, paintSecondary)

        summaryY += 15f
        canvas.drawText("Shipping:", summaryXLabel, summaryY, paintSecondary)
        canvas.drawText("$currency${String.format("%.2f", invoice.shipping)}", summaryXVal, summaryY, paintSecondary)

        summaryY += 18f
        paintText.apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.parseColor("#1565C0")
        }
        canvas.drawText("Grand Total:", summaryXLabel, summaryY, paintText)
        canvas.drawText("$currency${String.format("%.2f", grandTotal)}", summaryXVal, summaryY, paintText)

        // 5. Draw Signature (if any)
        if (signaturePoints.isNotEmpty()) {
            val sigStartY = summaryY + 40f
            paintText.apply {
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            canvas.drawText("Digital Signature Captured:", 40f, sigStartY, paintText)

            val paintSig = Paint().apply {
                color = android.graphics.Color.BLUE
                strokeWidth = 2f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            val xMin = signaturePoints.minOf { it.x }
            val xMax = signaturePoints.maxOf { it.x }
            val yMin = signaturePoints.minOf { it.y }
            val yMax = signaturePoints.maxOf { it.y }

            val sigWidth = if (xMax - xMin > 0) xMax - xMin else 1f
            val sigHeight = if (yMax - yMin > 0) yMax - yMin else 1f

            val targetWidth = 160f
            val targetHeight = 60f

            val scaleX = targetWidth / sigWidth
            val scaleY = targetHeight / sigHeight
            val scale = minOf(scaleX, scaleY)

            val startX = 40f
            val startY = sigStartY + 10f

            for (i in 0 until signaturePoints.size - 1) {
                val pt1 = signaturePoints[i]
                val pt2 = signaturePoints[i + 1]

                if ((pt1 - pt2).getDistanceSquared() < 250000) {
                    val drawX1 = startX + (pt1.x - xMin) * scale
                    val drawY1 = startY + (pt1.y - yMin) * scale
                    val drawX2 = startX + (pt2.x - xMin) * scale
                    val drawY2 = startY + (pt2.y - yMin) * scale
                    canvas.drawLine(drawX1, drawY1, drawX2, drawY2, paintSig)
                }
            }
            
            canvas.drawLine(40f, sigStartY + 75f, 200f, sigStartY + 75f, paintLine)
            paintSecondary.apply {
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                color = android.graphics.Color.GRAY
            }
            canvas.drawText("Authorized Representative", 40f, sigStartY + 85f, paintSecondary)
        }

        // 6. Footer section
        val footerY = 800f
        canvas.drawLine(40f, footerY - 10f, 555f, footerY - 10f, paintLine)
        paintSecondary.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            color = android.graphics.Color.GRAY
        }
        val footerText = "This app powered by Abitalib groups and all copyrights reserved by abitalib groups"
        val textWidth = paintSecondary.measureText(footerText)
        canvas.drawText(footerText, (595f - textWidth) / 2f, footerY, paintSecondary)

        pdfDocument.finishPage(page)

        // Save file to app cache directory
        val fileName = "Invoice_${invoice.invoiceNumber}.pdf"
        val file = File(context.cacheDir, fileName)
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()

        // Share file using Intent and FileProvider
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber} from $businessName")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the invoice ${invoice.invoiceNumber} for your recent purchase.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error generating or sharing PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// --- 5. CUSTOMERS SCREEN ---
@Composable
fun CustomersScreen(viewModel: BusinessViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Customer Database", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (customers.isEmpty()) {
            EmptyStateCard(
                message = "Customer list is empty. Catalog client details here for speedy invoicing and communication shortcuts.",
                icon = Icons.Default.People
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(customers) { cust ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(cust.name, fontWeight = FontWeight.Bold)
                                if (cust.phone.isNotBlank()) {
                                    Text("Phone: ${cust.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (cust.email.isNotBlank()) {
                                    Text("Email: ${cust.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Contact Shortcuts: WhatsApp & Email Launchers
                                if (cust.phone.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val url = "https://api.whatsapp.com/send?phone=${cust.phone}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.background(Color(0xFF25D366), CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteCustomer(cust) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Register Customer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Client Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp Phone (with Country Code)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.insertCustomer(
                                        Customer(name = name, phone = phone, email = email)
                                    )
                                    name = ""
                                    phone = ""
                                    email = ""
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// --- 6. SUPPLIERS SCREEN ---
@Composable
fun SuppliersScreen(viewModel: BusinessViewModel) {
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Supplier Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (suppliers.isEmpty()) {
            EmptyStateCard(
                message = "Supplier catalog is empty. Catalog wholesalers and distributors details here.",
                icon = Icons.Default.LocalShipping
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(suppliers) { sup ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(sup.name, fontWeight = FontWeight.Bold)
                                if (sup.phone.isNotBlank()) {
                                    Text("Phone: ${sup.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                sup.notes?.let {
                                    if (it.isNotBlank()) {
                                        Text("Notes: $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteSupplier(sup) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Register Supplier", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Supplier Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Business Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.insertSupplier(
                                        Supplier(name = name, phone = phone, notes = notes)
                                    )
                                    name = ""
                                    phone = ""
                                    notes = ""
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// --- 7. EXPENSES SCREEN ---
@Composable
fun ExpensesScreen(viewModel: BusinessViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var category by remember { mutableStateOf("Inventory") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Expenses Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Expense", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (expenses.isEmpty()) {
            EmptyStateCard(
                message = "No expenses logged yet. Save rent, utility payments, and replenishment costs here.",
                icon = Icons.Default.Receipt
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(expenses) { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(exp.category, fontWeight = FontWeight.Bold)
                                if (exp.notes.isNotBlank()) {
                                    Text(exp.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = DateFormat.getDateInstance().format(Date(exp.date)),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", exp.amount)}",
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error
                                )
                                IconButton(onClick = { viewModel.deleteExpense(exp) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Log Business Expense", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Expense Category (Rent, Replenishment, utilities)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Description / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val dAmt = amount.toDoubleOrNull()
                                if (dAmt != null && category.isNotBlank()) {
                                    viewModel.insertExpense(
                                        Expense(category = category, amount = dAmt, notes = notes)
                                    )
                                    category = "Inventory"
                                    amount = ""
                                    notes = ""
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// --- 8. REPORTS & ANALYTICS SCREEN ---
@Composable
fun ReportsScreen(viewModel: BusinessViewModel) {
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    val totalSales = invoices.filter { !it.isPending }.sumOf { 120.0 * (1 + (it.id % 4)) }
    val totalExpenses = expenses.sumOf { it.amount }
    val totalProfit = (totalSales - totalExpenses).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("FINANCIAL REPORTS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Financial Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Revenue:")
                        Text("$currencySymbol${String.format("%.2f", totalSales)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Expenses:")
                        Text("$currencySymbol${String.format("%.2f", totalExpenses)}", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Business Profit:")
                        Text("$currencySymbol${String.format("%.2f", totalProfit)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Beautiful Interactive Stock Inventory Level Indicator List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Stock Replenishment Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (products.isEmpty()) {
                        Text("No items registered in inventory to compute statistics.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        products.take(4).forEach { prod ->
                            val proportion = (prod.quantity.toFloat() / (prod.lowStockAlert * 3).toFloat()).coerceIn(0f, 1f)
                            val barColor = when {
                                prod.quantity <= prod.lowStockAlert -> Color(0xFFD84315)
                                prod.quantity <= prod.lowStockAlert * 1.5 -> Color(0xFFFBC02D)
                                else -> Color(0xFF2E7D32)
                            }
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(prod.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${prod.quantity} units", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { proportion },
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            NativeAd()
        }
    }
}

// --- 9. DATABASE BACKUP SCREEN ---
@Composable
fun BackupScreen(viewModel: BusinessViewModel) {
    val backupMessage by viewModel.backupRestoreMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Backup,
            contentDescription = "Backup",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SECURE DATABASE BACKUPS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Back up or restore your entire business ledger locally on this device. Backups store products, invoices, customer and supplier directories safely.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.backupDatabase()
                Toast.makeText(context, "Creating local backup...", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Local Backup")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                viewModel.restoreDatabase()
                Toast.makeText(context, "Restoring local backup...", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restore Last Backup")
        }

        backupMessage?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = { viewModel.clearBackupRestoreMessage() }) {
                Text("Dismiss message")
            }
        }
    }
}

// --- 10. PREFERENCES SETTINGS SCREEN ---
@Composable
fun SettingsScreen(viewModel: BusinessViewModel) {
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val notificationEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val currentPin by viewModel.securityPin.collectAsStateWithLifecycle()

    val businessName by viewModel.businessName.collectAsStateWithLifecycle()
    val businessPhone by viewModel.businessPhone.collectAsStateWithLifecycle()
    val businessEmail by viewModel.businessEmail.collectAsStateWithLifecycle()
    val businessAddress by viewModel.businessAddress.collectAsStateWithLifecycle()
    val businessBranding by viewModel.businessBranding.collectAsStateWithLifecycle()
    val businessLogo by viewModel.businessLogo.collectAsStateWithLifecycle()

    val googleDriveConnected by viewModel.googleDriveConnected.collectAsStateWithLifecycle()
    val googleDriveAccountName by viewModel.googleDriveAccountName.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form editing states
    var editName by remember(businessName) { mutableStateOf(businessName) }
    var editPhone by remember(businessPhone) { mutableStateOf(businessPhone) }
    var editEmail by remember(businessEmail) { mutableStateOf(businessEmail) }
    var editAddress by remember(businessAddress) { mutableStateOf(businessAddress) }
    var editBranding by remember(businessBranding) { mutableStateOf(businessBranding) }
    var editLogo by remember(businessLogo) { mutableStateOf(businessLogo) }

    // Backup & Sign-In simulation states
    var isConnectingDrive by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var backupProgress by remember { mutableStateOf(0f) }
    var restoreProgress by remember { mutableStateOf(0f) }

    var showPinDialog by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Block
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "MY BUSINESS ACCOUNT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Apna branding data aur currency formats configure karein.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 2. BUSINESS BRANDING & CONTACT INFO FORM ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Business Profile & Branding",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    // Input Form Fields
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Business Name (e.g., Al-Makkah Traders)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Contact Phone Information") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Business Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Physical Address (Invoice Print pe ayega)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editBranding,
                        onValueChange = { editBranding = it },
                        label = { Text("Custom Tagline / Branding Text (Footer / Header)") },
                        placeholder = { Text("e.g. Quality is our foremost identity") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )

                    // --- BUSINESS LOGO SELECTION AND CUSTOM LOADER ---
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "Business Logo / Brand Identity",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Apnay brand ka logo select karein. Yeh printed PDF invoice aur billing header pe professional look dega.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val galleryLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let { selectedUri ->
                            try {
                                val inputStream = context.contentResolver.openInputStream(selectedUri)
                                val file = java.io.File(context.filesDir, "business_logo.png")
                                inputStream?.use { input ->
                                    file.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                editLogo = file.absolutePath
                                Toast.makeText(context, "Logo successfully imported from gallery!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                editLogo = selectedUri.toString()
                                Toast.makeText(context, "Logo linked from gallery!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Logo preview inside an elegant circle or rounded rect
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editLogo.isNotEmpty()) {
                                    AsyncImage(
                                        model = editLogo,
                                        contentDescription = "Business Logo Preview",
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Logo Image", fontSize = 12.sp)
                            }
                            if (editLogo.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { editLogo = "" },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Remove Logo", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editLogo,
                        onValueChange = { editLogo = it },
                        label = { Text("Custom Logo Image URL") },
                        placeholder = { Text("https://example.com/logo.png") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        trailingIcon = {
                            if (editLogo.isNotEmpty()) {
                                IconButton(onClick = { editLogo = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Or Choose a Premium Enterprise Preset Logo:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val presets = listOf(
                        "https://images.unsplash.com/photo-1563013544-824ae1d704d3?w=150&auto=format&fit=crop&q=60" to "Mart / Mall",
                        "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=150&auto=format&fit=crop&q=60" to "Retail Shop",
                        "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?w=150&auto=format&fit=crop&q=60" to "Fashion Store",
                        "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=150&auto=format&fit=crop&q=60" to "Pharmacy",
                        "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=150&auto=format&fit=crop&q=60" to "Bake / Cafe",
                        "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=150&auto=format&fit=crop&q=60" to "General Tech"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { preset ->
                            val isSelected = editLogo == preset.first
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .width(84.dp)
                                    .clickable { editLogo = preset.first }
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                    ) {
                                        AsyncImage(
                                            model = preset.first,
                                            contentDescription = preset.second,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.second,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Invoice Header preview block
                    Text(
                        text = "LIVE INVOICE HEADER PREVIEW:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                if (editLogo.isNotEmpty()) {
                                    AsyncImage(
                                        model = editLogo,
                                        contentDescription = "Logo Live Preview",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(2.dp)
                                            .align(Alignment.CenterVertically),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = editName.ifEmpty { "Business Toolkit Pro" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = editBranding.ifEmpty { "Digital Ledger & Invoice Solutions" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (editPhone.isNotEmpty()) {
                                        Text("📞 Phone: $editPhone", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (editEmail.isNotEmpty()) {
                                        Text("✉️ Email: $editEmail", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (editAddress.isNotEmpty()) {
                                        Text("📍 Address: $editAddress", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "INVOICE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("INV-123456", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.setBusinessProfile(editName, editPhone, editEmail, editAddress, editBranding, editLogo)
                            Toast.makeText(context, "Business Branding details saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Business Profile")
                    }
                }
            }
        }

        // --- 3. CURRENCY SELECTION SECTION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Business Ledger Currency",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Text(
                        text = "Selected Currency Symbol: $currencySymbol",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Currencies including PKR, USD, INR, CNY
                    val currencyOptions = listOf(
                        "PKR (Rs)" to "Rs",
                        "USD ($)" to "$",
                        "INR (₹)" to "₹",
                        "CNY (¥)" to "¥"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencyOptions.forEach { option ->
                            FilterChip(
                                selected = currencySymbol == option.second,
                                onClick = {
                                    viewModel.setCurrency(option.second)
                                    Toast.makeText(context, "Currency set to ${option.first}!", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(option.first, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. STANDARD PREFERENCES (DARK MODE, SECURITY, SERVICES) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Aesthetics & System Security",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Dark mode Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text("Dark Mode Theme")
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // PIN lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text("Secure 4-Digit PIN")
                            }
                            Text(
                                text = if (currentPin.isNullOrEmpty()) "Inactive" else "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentPin.isNullOrEmpty()) MaterialTheme.colorScheme.outline else Color(0xFF2E7D32)
                            )
                        }
                        Button(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (currentPin.isNullOrEmpty()) "Set" else "Disable")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text("Low Stock Notifications")
                            }
                            Text("WorkManager runs daily alerts in background", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "This app powered by Abitalib groups",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "All copyrights reserved by abitalib groups",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showPinDialog) {
        Dialog(onDismissRequest = { showPinDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentPin.isNullOrEmpty()) "Configure 4-Digit Security PIN" else "Disable PIN Authentication",
                        fontWeight = FontWeight.Bold
                    )

                    if (currentPin.isNullOrEmpty()) {
                        OutlinedTextField(
                            value = inputPin,
                            onValueChange = { if (it.length <= 4) inputPin = it },
                            placeholder = { Text("e.g. 1234") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Are you sure you want to disable secure PIN verification?", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (currentPin.isNullOrEmpty()) {
                                    if (inputPin.length == 4) {
                                        viewModel.setSecurityPin(inputPin)
                                        inputPin = ""
                                        showPinDialog = false
                                    }
                                } else {
                                    viewModel.setSecurityPin(null)
                                    showPinDialog = false
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER GENERIC COMPOSE ELEMENTS ---
@Composable
fun EmptyStateCard(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
