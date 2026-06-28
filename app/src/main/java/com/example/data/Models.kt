package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val barcode: String? = null,
    val qrCode: String? = null,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val quantity: Int = 0,
    val lowStockAlert: Int = 5,
    val expiryDate: Long? = null, // timestamp in millis
    val supplierId: Int? = null,
    val notes: String = "",
    val imageUrl: String? = null
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String? = null,
    val notes: String? = null
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String? = null,
    val notes: String? = null
)

@Entity(
    tableName = "invoices"
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String,
    val customerId: Int? = null,
    val customerName: String,
    val customerPhone: String? = null,
    val date: Long = System.currentTimeMillis(),
    val discount: Double = 0.0,
    val taxRate: Double = 0.0, // percentage, e.g. 18.0
    val shipping: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, Card, Bank, WhatsApp Pay
    val signaturePath: String? = null, // Optional signature sketch
    val notes: String? = null,
    val isPending: Boolean = false
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val productId: Int? = null,
    val productName: String,
    val quantity: Int,
    val sellingPrice: Double
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val receiptImagePath: String? = null
)
