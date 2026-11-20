package com.example.tp_android_contacts_v3.data

import android.provider.BaseColumns

object ContactContract {
    const val TABLE_NAME = "contacts"
    const val COLUMN_ID = BaseColumns._ID
    const val COLUMN_NOM = "nom"
    const val COLUMN_PRENOM = "prenom"
    const val COLUMN_TELEPHONE = "telephone"
    const val COLUMN_EMAIL = "email"
    const val COLUMN_PHOTO_URI = "photo_uri"
    const val COLUMN_LATITUDE = "latitude"
    const val COLUMN_LONGITUDE = "longitude"
}
