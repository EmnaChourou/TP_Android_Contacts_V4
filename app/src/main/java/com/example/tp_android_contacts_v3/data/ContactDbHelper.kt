package com.example.tp_android_contacts_v3.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactDbHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE ${ContactContract.TABLE_NAME} (
                ${ContactContract.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactContract.COLUMN_NOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_PRENOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_TELEPHONE} TEXT NOT NULL,
                ${ContactContract.COLUMN_EMAIL} TEXT,
                ${ContactContract.COLUMN_PHOTO_URI} TEXT,
                ${ContactContract.COLUMN_LATITUDE} REAL,
                ${ContactContract.COLUMN_LONGITUDE} REAL
            )
        """.trimIndent()
        db.execSQL(createTableSql)

        // Insert at least 5 demo contacts (2 without GPS coordinates)
        val demoContacts = listOf(
            arrayOf("Ben Salah", "Mohamed", "+216 98 123 456", "mohamed.bensalah@email.tn", 36.8065, 10.1815),
            arrayOf("Dupont", "Claire", "+33 6 12 34 56 78", "claire.dupont@email.fr", 48.8566, 2.3522),
            arrayOf("Trabelsi", "Amine", "+216 55 987 654", "amine.trabelsi@email.tn", 35.8256, 10.6369),
            arrayOf("Martin", "Sophie", "+33 6 98 76 54 32", "sophie.martin@email.fr", null, null),
            arrayOf("Kastally", "Youssef", "+216 22 333 444", "youssef.kastally@email.tn", null, null)
        )

        for (contact in demoContacts) {
            val values = ContentValues().apply {
                put(ContactContract.COLUMN_NOM, contact[0] as String)
                put(ContactContract.COLUMN_PRENOM, contact[1] as String)
                put(ContactContract.COLUMN_TELEPHONE, contact[2] as String)
                put(ContactContract.COLUMN_EMAIL, contact[3] as String)
                if (contact[4] != null) {
                    put(ContactContract.COLUMN_LATITUDE, contact[4] as Double)
                    put(ContactContract.COLUMN_LONGITUDE, contact[5] as Double)
                }
            }
            db.insert(ContactContract.TABLE_NAME, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContactContract.TABLE_NAME}")
        onCreate(db)
    }
}
