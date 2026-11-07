package br.com.dispensa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Product(
    val id: Long,
    val name: String,
    val barcode: String,
    val quantity: Double,
    val unit: String,
    val minimum: Double,
    val photo: ByteArray?
)

class ProductDatabase(context: Context) : SQLiteOpenHelper(context, "dispensa.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                barcode TEXT NOT NULL DEFAULT '',
                quantity REAL NOT NULL,
                unit TEXT NOT NULL,
                minimum REAL NOT NULL,
                photo BLOB
            )""".trimIndent()
        )
    }

    // Recria a tabela no novo formato solicitado para começar o cadastro do zero.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS products")
        onCreate(db)
    }

    fun allProducts(): List<Product> {
        val result = mutableListOf<Product>()
        readableDatabase.query("products", null, null, null, null, null, "name COLLATE NOCASE ASC").use { cursor ->
            while (cursor.moveToNext()) {
                result += Product(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("barcode")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("minimum")),
                    cursor.getBlob(cursor.getColumnIndexOrThrow("photo"))
                )
            }
        }
        return result
    }

    fun addProduct(product: Product) {
        val values = ContentValues().apply {
            put("name", product.name.trim())
            put("barcode", product.barcode.trim())
            put("quantity", product.quantity)
            put("unit", product.unit)
            put("minimum", product.minimum)
            product.photo?.let { put("photo", it) } ?: putNull("photo")
        }
        writableDatabase.insertOrThrow("products", null, values)
    }

    fun nameExists(name: String): Boolean {
        readableDatabase.query(
            "products",
            arrayOf("id"),
            "LOWER(TRIM(name)) = LOWER(TRIM(?))",
            arrayOf(name.trim()),
            null,
            null,
            null,
            "1"
        ).use { return it.moveToFirst() }
    }

    fun barcodeExists(barcode: String): Boolean {
        val normalizedBarcode = barcode.trim()
        if (normalizedBarcode.isEmpty()) return false
        readableDatabase.query(
            "products",
            arrayOf("id"),
            "barcode = ?",
            arrayOf(normalizedBarcode),
            null,
            null,
            null,
            "1"
        ).use { return it.moveToFirst() }
    }

    fun deleteProduct(id: Long) {
        writableDatabase.delete("products", "id = ?", arrayOf(id.toString()))
    }
}
