package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class BusinessViewModel(
    private val application: Application,
    private val repository: BusinessRepository,
    private val preferenceStore: PreferenceStore
) : AndroidViewModel(application) {

    // --- SEARCH / FILTERS STATES ---
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedProductCategory = MutableStateFlow("All")
    val selectedProductCategory: StateFlow<String> = _selectedProductCategory.asStateFlow()

    private val _filterOnlyLowStock = MutableStateFlow(false)
    val filterOnlyLowStock: StateFlow<Boolean> = _filterOnlyLowStock.asStateFlow()

    // --- RECURRING AD COUNTERS ---
    private val _interactionCount = MutableStateFlow(0)
    val interactionCount: StateFlow<Int> = _interactionCount.asStateFlow()

    // --- DYNAMIC DATA FLOWS ---
    val allProducts: StateFlow<List<Product>> = _productSearchQuery
        .combine(_selectedProductCategory) { q, cat -> Pair(q, cat) }
        .combine(_filterOnlyLowStock) { pair, lowOnly -> Triple(pair.first, pair.second, lowOnly) }
        .flatMapLatest { (q, cat, lowOnly) ->
            repository.searchProducts(q).map { list ->
                list.filter { prod ->
                    val matchCat = cat == "All" || prod.category.equals(cat, ignoreCase = true)
                    val matchLow = !lowOnly || prod.quantity <= prod.lowStockAlert
                    matchCat && matchLow
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- PREFERENCE STORE FLOWS ---
    val darkMode: StateFlow<Boolean> = preferenceStore.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currencySymbol: StateFlow<String> = preferenceStore.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    val languageCode: StateFlow<String> = preferenceStore.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val notificationsEnabled: StateFlow<Boolean> = preferenceStore.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val securityPin: StateFlow<String?> = preferenceStore.securityPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val biometricEnabled: StateFlow<Boolean> = preferenceStore.biometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val businessName: StateFlow<String> = preferenceStore.businessNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Business")

    val businessPhone: StateFlow<String> = preferenceStore.businessPhoneFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val businessEmail: StateFlow<String> = preferenceStore.businessEmailFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val businessAddress: StateFlow<String> = preferenceStore.businessAddressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val businessBranding: StateFlow<String> = preferenceStore.businessBrandingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val googleDriveConnected: StateFlow<Boolean> = preferenceStore.googleDriveConnectedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val googleDriveAccountName: StateFlow<String> = preferenceStore.googleDriveAccountNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // --- SECURITY ACCESS STATUS ---
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    init {
        // Initialize lock status if PIN exists
        viewModelScope.launch {
            securityPin.collect { pin ->
                _isAppLocked.value = !pin.isNullOrEmpty()
            }
        }
    }

    fun unlockApp(pinAttempt: String): Boolean {
        if (pinAttempt == securityPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun forceLockApp() {
        if (!securityPin.value.isNullOrEmpty()) {
            _isAppLocked.value = true
        }
    }

    // --- BACKUP & RESTORE STATES ---
    private val _backupRestoreMessage = MutableStateFlow<String?>(null)
    val backupRestoreMessage: StateFlow<String?> = _backupRestoreMessage.asStateFlow()

    // --- INVOICE MAKER TEMPORARY DRAFT STATE ---
    private val _draftItems = MutableStateFlow<List<InvoiceItem>>(emptyList())
    val draftItems: StateFlow<List<InvoiceItem>> = _draftItems.asStateFlow()

    fun addDraftItem(product: Product, qty: Int) {
        val existingIndex = _draftItems.value.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val list = _draftItems.value.toMutableList()
            val updatedQty = list[existingIndex].quantity + qty
            list[existingIndex] = list[existingIndex].copy(quantity = updatedQty)
            _draftItems.value = list
        } else {
            _draftItems.value = _draftItems.value + InvoiceItem(
                invoiceId = 0,
                productId = product.id,
                productName = product.name,
                quantity = qty,
                sellingPrice = product.sellingPrice
            )
        }
    }

    fun removeDraftItem(index: Int) {
        val list = _draftItems.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _draftItems.value = list
        }
    }

    fun clearDraftItems() {
        _draftItems.value = emptyList()
    }

    // --- MUTATION ACTIONS (DATABASE Operations) ---
    fun insertProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }

    fun insertCustomer(customer: Customer) = viewModelScope.launch {
        repository.insertCustomer(customer)
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch {
        repository.updateCustomer(customer)
    }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch {
        repository.deleteCustomer(customer)
    }

    fun insertSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.insertSupplier(supplier)
    }

    fun updateSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.updateSupplier(supplier)
    }

    fun deleteSupplier(supplier: Supplier) = viewModelScope.launch {
        repository.deleteSupplier(supplier)
    }

    fun createInvoice(invoice: Invoice) = viewModelScope.launch {
        repository.insertInvoice(invoice, _draftItems.value)
        clearDraftItems()
    }

    fun deleteInvoice(invoice: Invoice) = viewModelScope.launch {
        repository.deleteInvoice(invoice)
    }

    fun insertExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // --- SETTINGS ACTIONS ---
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        preferenceStore.setDarkMode(enabled)
    }

    fun setCurrency(symbol: String) = viewModelScope.launch {
        preferenceStore.setCurrency(symbol)
    }

    fun setLanguage(code: String) = viewModelScope.launch {
        preferenceStore.setLanguage(code)
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        preferenceStore.setNotificationsEnabled(enabled)
    }

    fun setSecurityPin(pin: String?) = viewModelScope.launch {
        preferenceStore.setSecurityPin(pin)
        _isAppLocked.value = !pin.isNullOrEmpty()
    }

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        preferenceStore.setBiometricEnabled(enabled)
    }

    fun setBusinessProfile(name: String, phone: String, email: String, address: String, branding: String) = viewModelScope.launch {
        preferenceStore.setBusinessProfile(name, phone, email, address, branding)
    }

    fun setGoogleDriveConnected(connected: Boolean, accountName: String) = viewModelScope.launch {
        preferenceStore.setGoogleDriveConnected(connected, accountName)
    }

    fun setSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedProductCategory.value = category
    }

    fun setOnlyLowStockFilter(onlyLow: Boolean) {
        _filterOnlyLowStock.value = onlyLow
    }

    fun incrementInteractionCount() {
        _interactionCount.value = _interactionCount.value + 1
    }

    fun resetInteractionCount() {
        _interactionCount.value = 0
    }

    // --- LOCAL BACKUP & RESTORE CONTROLLER ---
    fun backupDatabase() {
        viewModelScope.launch {
            try {
                val dbFile = repository.getDatabasePath(application)
                if (!dbFile.exists()) {
                    _backupRestoreMessage.value = "Error: Database file not initialized yet."
                    return@launch
                }

                val backupDir = File(
                    application.getExternalFilesDir(null),
                    "backups"
                )
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val backupFile = File(backupDir, "business_toolkit_db.backup")
                copyFile(dbFile, backupFile)
                _backupRestoreMessage.value = "Backup created successfully at: ${backupFile.absolutePath}"
            } catch (e: Exception) {
                recordCrash(e)
                _backupRestoreMessage.value = "Backup failed: ${e.message}"
            }
        }
    }

    fun restoreDatabase() {
        viewModelScope.launch {
            try {
                val backupFile = File(
                    File(application.getExternalFilesDir(null), "backups"),
                    "business_toolkit_db.backup"
                )
                if (!backupFile.exists()) {
                    _backupRestoreMessage.value = "Error: Backup file not found."
                    return@launch
                }

                val dbFile = repository.getDatabasePath(application)
                copyFile(backupFile, dbFile)
                _backupRestoreMessage.value = "Database restored successfully. Please restart the app."
            } catch (e: Exception) {
                recordCrash(e)
                _backupRestoreMessage.value = "Restore failed: ${e.message}"
            }
        }
    }

    fun clearBackupRestoreMessage() {
        _backupRestoreMessage.value = null
    }

    private fun copyFile(src: File, dst: File) {
        FileInputStream(src).use { inStream ->
            FileOutputStream(dst).use { outStream ->
                val buf = ByteArray(1024)
                var len: Int
                while (inStream.read(buf).also { len = it } > 0) {
                    outStream.write(buf, 0, len)
                }
            }
        }
    }

    // --- CSV IMPORT / EXPORT CONTROLLER ---
    fun exportProductsToCSV(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("ID,Name,Category,CostPrice,SellingPrice,Quantity,LowStockAlert,Supplier\n")
        allProducts.value.forEach { p ->
            stringBuilder.append("${p.id},\"${p.name.replace("\"", "\"\"")}\",\"${p.category.replace("\"", "\"\"")}\",${p.costPrice},${p.sellingPrice},${p.quantity},${p.lowStockAlert},\"${p.supplierId ?: ""}\"\n")
        }
        return stringBuilder.toString()
    }

    fun importProductsFromCSV(csvContent: String): Int {
        var count = 0
        try {
            val lines = csvContent.lines()
            if (lines.size <= 1) return 0
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue
                // Simple comma-split parsing (handles standard non-escaped fields perfectly for speed and robustness)
                val parts = line.split(",")
                if (parts.size >= 6) {
                    val name = parts[1].replace("\"", "").trim()
                    val category = parts[2].replace("\"", "").trim()
                    val costPrice = parts[3].toDoubleOrNull() ?: 0.0
                    val sellingPrice = parts[4].toDoubleOrNull() ?: 0.0
                    val quantity = parts[5].toIntOrNull() ?: 0
                    val lowStockAlert = parts.getOrNull(6)?.toIntOrNull() ?: 5
                    val supplierId = parts.getOrNull(7)?.replace("\"", "")?.toIntOrNull()

                    val prod = Product(
                        name = name,
                        category = category,
                        costPrice = costPrice,
                        sellingPrice = sellingPrice,
                        quantity = quantity,
                        lowStockAlert = lowStockAlert,
                        supplierId = supplierId
                    )
                    insertProduct(prod)
                    count++
                }
            }
        } catch (e: Exception) {
            recordCrash(e)
        }
        return count
    }

    // --- SAFE EXCEPTION LOGGING ---
    private fun recordCrash(e: Exception) {
        try {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(e)
        } catch (ignored: Exception) {}
        e.printStackTrace()
    }
}

class BusinessViewModelFactory(
    private val application: Application,
    private val repository: BusinessRepository,
    private val preferenceStore: PreferenceStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusinessViewModel(application, repository, preferenceStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
