package com.example.productapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Product::class], version = 2, exportSchema = true)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var instance: ProductDatabase? = null

        // Preserve version-1 records; assign existing products a valid quantity.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): ProductDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, ProductDatabase::class.java, "product_db"
            ).addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Seed within creation, avoiding a race with the first user insert.
                        db.execSQL("INSERT INTO products (id, name, price, deliveryDate, category, isFavorite, quantity) VALUES (101, 'Phone', 299.99, '2025-04-01', 'Electronics', 0, 1)")
                        db.execSQL("INSERT INTO products (id, name, price, deliveryDate, category, isFavorite, quantity) VALUES (102, 'Laptop', 999.99, '2025-04-02', 'Electronics', 1, 1)")
                        db.execSQL("INSERT INTO products (id, name, price, deliveryDate, category, isFavorite, quantity) VALUES (103, 'Microwave', 149.99, '2025-04-03', 'Appliances', 0, 1)")
                    }
                }).build().also { instance = it }
        }
    }
}
