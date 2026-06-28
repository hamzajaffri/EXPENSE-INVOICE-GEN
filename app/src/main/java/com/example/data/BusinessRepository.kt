package com.example.data

import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val supplierDao = db.supplierDao()
    private val invoiceDao = db.invoiceDao()
    private val invoiceItemDao = db.invoiceItemDao()
    private val expenseDao = db.expenseDao()

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts("%$query%")
    }

    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // Customers
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    suspend fun getCustomerById(id: Int): Customer? = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Suppliers
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    suspend fun getSupplierById(id: Int): Supplier? = supplierDao.getSupplierById(id)
    suspend fun insertSupplier(supplier: Supplier): Long = supplierDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.deleteSupplier(supplier)

    // Invoices
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    val pendingInvoices: Flow<List<Invoice>> = invoiceDao.getPendingInvoices()
    suspend fun getInvoiceById(id: Int): Invoice? = invoiceDao.getInvoiceById(id)
    
    suspend fun insertInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        items.forEach { item ->
            invoiceItemDao.insertInvoiceItem(item.copy(invoiceId = invoiceId.toInt()))
            // Update product stock inventory if appropriate
            item.productId?.let { prodId ->
                val prod = productDao.getProductById(prodId)
                if (prod != null) {
                    val newQty = (prod.quantity - item.quantity).coerceAtLeast(0)
                    productDao.updateProduct(prod.copy(quantity = newQty))
                }
            }
        }
        return invoiceId
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceItemDao.deleteItemsForInvoice(invoice.id)
        invoiceDao.deleteInvoice(invoice)
    }

    // Invoice Items
    fun getItemsForInvoice(invoiceId: Int): Flow<List<InvoiceItem>> = invoiceItemDao.getItemsForInvoice(invoiceId)

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    suspend fun getExpenseById(id: Int): Expense? = expenseDao.getExpenseById(id)
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    // Direct access to raw database file path for backup/restore purposes
    fun getDatabasePath(context: android.content.Context): java.io.File {
        return context.getDatabasePath("business_toolkit_db")
    }
}
