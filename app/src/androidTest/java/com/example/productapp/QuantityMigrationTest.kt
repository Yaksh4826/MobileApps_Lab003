package com.example.productapp

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.example.productapp.data.Product
import com.example.productapp.data.ProductDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuantityMigrationTest {
    @Test fun migrationPreservesOldProductsAndPersistsNewQuantity() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "quantity-migration-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL("CREATE TABLE products (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, price REAL NOT NULL, deliveryDate TEXT NOT NULL, category TEXT NOT NULL, isFavorite INTEGER NOT NULL)")
            old.execSQL("INSERT INTO products VALUES (101, 'Existing phone', 100.0, '2025-04-01', 'Electronics', 0)")
            old.version = 1
        }
        fun open() = Room.databaseBuilder(context, ProductDatabase::class.java, name)
            .addMigrations(ProductDatabase.MIGRATION_1_2).build()
        val upgraded = open()
        try {
            val oldProduct = upgraded.productDao().getAll().first().single()
            assertEquals("Existing phone", oldProduct.name)
            assertEquals(1, oldProduct.quantity)
            upgraded.productDao().insert(Product(104, "Keyboard", 25.0, "2026-11-20", "Electronics", false, 3))
        } finally { upgraded.close() }
        val reopened = open()
        try {
            assertEquals(3, reopened.productDao().getAll().first().first { it.id == 104 }.quantity)
        } finally { reopened.close(); context.deleteDatabase(name) }
    }
}
