package com.example.tp_android_contacts_v3.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.tp_android_contacts_v3.model.Contact

class ContactDao(context: Context) {
    private val dbHelper = ContactDbHelper.getInstance(context)

    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, contact.nom)
            put(ContactContract.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.COLUMN_EMAIL, contact.email)
            put(ContactContract.COLUMN_PHOTO_URI, contact.photoUri)
            if (contact.latitude != null) put(ContactContract.COLUMN_LATITUDE, contact.latitude)
            if (contact.longitude != null) put(ContactContract.COLUMN_LONGITUDE, contact.longitude)
        }
        return db.insert(ContactContract.TABLE_NAME, null, values)
    }

    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, contact.nom)
            put(ContactContract.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.COLUMN_EMAIL, contact.email)
            put(ContactContract.COLUMN_PHOTO_URI, contact.photoUri)
            put(ContactContract.COLUMN_LATITUDE, contact.latitude)
            put(ContactContract.COLUMN_LONGITUDE, contact.longitude)
        }
        return db.update(
            ContactContract.TABLE_NAME,
            values,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            ContactContract.TABLE_NAME,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getAll(): List<Contact> {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${ContactContract.TABLE_NAME} ORDER BY ${ContactContract.COLUMN_NOM} COLLATE NOCASE, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE"
        val contacts = mutableListOf<Contact>()
        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(cursorToContact(cursor))
            }
        }
        return contacts
    }

    fun getById(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${ContactContract.TABLE_NAME} WHERE ${ContactContract.COLUMN_ID} = ?"
        db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToContact(cursor)
            }
        }
        return null
    }

    fun search(searchQuery: String): List<Contact> {
        val db = dbHelper.readableDatabase
        val pattern = "%$searchQuery%"
        val query = """
            SELECT * FROM ${ContactContract.TABLE_NAME} 
            WHERE ${ContactContract.COLUMN_NOM} LIKE ? 
               OR ${ContactContract.COLUMN_PRENOM} LIKE ? 
               OR ${ContactContract.COLUMN_TELEPHONE} LIKE ?
            ORDER BY ${ContactContract.COLUMN_NOM} COLLATE NOCASE, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE
        """.trimIndent()
        val contacts = mutableListOf<Contact>()
        db.rawQuery(query, arrayOf(pattern, pattern, pattern)).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(cursorToContact(cursor))
            }
        }
        return contacts
    }

    private fun cursorToContact(cursor: Cursor): Contact {
        val idIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_ID)
        val nomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_NOM)
        val prenomIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PRENOM)
        val telIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_TELEPHONE)
        val emailIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_EMAIL)
        val photoIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PHOTO_URI)
        val latIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LATITUDE)
        val lngIdx = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LONGITUDE)

        return Contact(
            id = cursor.getLong(idIdx),
            nom = cursor.getString(nomIdx),
            prenom = cursor.getString(prenomIdx),
            telephone = cursor.getString(telIdx),
            email = if (cursor.isNull(emailIdx)) null else cursor.getString(emailIdx),
            photoUri = if (cursor.isNull(photoIdx)) null else cursor.getString(photoIdx),
            latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx),
            longitude = if (cursor.isNull(lngIdx)) null else cursor.getDouble(lngIdx)
        )
    }
}
