package com.example.tp_android_contacts_v3.model

data class Contact(
    val id: Long = 0L,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String? = null,
    var photoUri: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)
