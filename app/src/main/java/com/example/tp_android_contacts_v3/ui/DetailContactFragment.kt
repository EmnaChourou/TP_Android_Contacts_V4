package com.example.tp_android_contacts_v3.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.tp_android_contacts_v3.R
import com.example.tp_android_contacts_v3.model.Contact
import com.example.tp_android_contacts_v3.viewmodel.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val viewModel: ContactViewModel by activityViewModels()
    private var contactId: Long = 0L
    private var currentContact: Contact? = null
    private var photoUriStr: String? = null

    private lateinit var ivPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { savePhotoToInternalStorage(it) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = File(requireContext().filesDir, "contact_photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            photoUriStr = Uri.fromFile(file).toString()
            ivPhoto.setImageBitmap(it)
        }
    }

    private fun savePhotoToInternalStorage(sourceUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(sourceUri)
                val file = File(requireContext().filesDir, "contact_photo_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                val savedUriStr = Uri.fromFile(file).toString()
                withContext(Dispatchers.Main) {
                    photoUriStr = savedUriStr
                    ivPhoto.setImageURI(Uri.parse(savedUriStr))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erreur lors de l'enregistrement de la photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPhoto = view.findViewById(R.id.iv_detail_photo)
        etNom = view.findViewById(R.id.et_nom)
        etPrenom = view.findViewById(R.id.et_prenom)
        etTelephone = view.findViewById(R.id.et_telephone)
        etEmail = view.findViewById(R.id.et_email)
        etLatitude = view.findViewById(R.id.et_latitude)
        etLongitude = view.findViewById(R.id.et_longitude)

        val btnGalerie = view.findViewById<Button>(R.id.btn_galerie)
        val btnCamera = view.findViewById<Button>(R.id.btn_camera)
        val btnCall = view.findViewById<Button>(R.id.btn_call)
        val btnEmailAction = view.findViewById<Button>(R.id.btn_email_action)
        val btnMap = view.findViewById<Button>(R.id.btn_map)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        contactId = arguments?.getLong("contact_id", 0L) ?: 0L
        if (contactId != 0L) {
            btnDelete.visibility = View.VISIBLE
            lifecycleScope.launch {
                currentContact = viewModel.getContactById(contactId)
                currentContact?.let { contact ->
                    etNom.setText(contact.nom)
                    etPrenom.setText(contact.prenom)
                    etTelephone.setText(contact.telephone)
                    etEmail.setText(contact.email ?: "")
                    contact.latitude?.let { etLatitude.setText(it.toString()) }
                    contact.longitude?.let { etLongitude.setText(it.toString()) }
                    photoUriStr = contact.photoUri
                    if (!photoUriStr.isNullOrEmpty()) {
                        try {
                            ivPhoto.setImageURI(Uri.parse(photoUriStr))
                        } catch (e: Exception) {
                            ivPhoto.setImageResource(R.drawable.ic_avatar_placeholder)
                        }
                    }
                }
            }
        } else {
            btnDelete.visibility = View.GONE
        }

        btnGalerie.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCamera.setOnClickListener {
            cameraLauncher.launch(null)
        }

        btnCall.setOnClickListener {
            val phone = etTelephone.text.toString().trim()
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), R.string.no_app_available, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Numéro de téléphone requis", Toast.LENGTH_SHORT).show()
            }
        }

        btnEmailAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), R.string.no_app_available, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Adresse e-mail requise", Toast.LENGTH_SHORT).show()
            }
        }

        btnMap.setOnClickListener {
            val latStr = etLatitude.text.toString().trim()
            val lngStr = etLongitude.text.toString().trim()
            if (latStr.isNotEmpty() && lngStr.isNotEmpty()) {
                val lat = latStr.toDoubleOrNull()
                val lng = lngStr.toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    val carteFragment = CarteContactFragment().apply {
                        arguments = Bundle().apply {
                            putLong("contact_id", contactId)
                            putDouble("latitude", lat)
                            putDouble("longitude", lng)
                            putString("contact_name", "${etPrenom.text} ${etNom.text}")
                            putString("contact_phone", etTelephone.text.toString())
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, carteFragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), R.string.error_coordinates_invalid, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), R.string.error_no_coordinates, Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    viewModel.deleteContact(contactId) {
                        Toast.makeText(requireContext(), R.string.contact_deleted_success, Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
                .setNegativeButton(R.string.dialog_no, null)
                .show()
        }

        btnSave.setOnClickListener {
            val nom = etNom.text.toString().trim()
            val prenom = etPrenom.text.toString().trim()
            val telephone = etTelephone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_champs_obligatoires, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val latStr = etLatitude.text.toString().trim()
            val lngStr = etLongitude.text.toString().trim()
            val lat = latStr.toDoubleOrNull()
            val lng = lngStr.toDoubleOrNull()

            if ((latStr.isNotEmpty() || lngStr.isNotEmpty()) && (lat == null || lng == null || lat !in -90.0..90.0 || lng !in -180.0..180.0)) {
                Toast.makeText(requireContext(), R.string.error_coordinates_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contactId == 0L) {
                val newContact = Contact(
                    nom = nom,
                    prenom = prenom,
                    telephone = telephone,
                    email = if (email.isEmpty()) null else email,
                    photoUri = photoUriStr,
                    latitude = lat,
                    longitude = lng
                )
                viewModel.insertContact(newContact) {
                    Toast.makeText(requireContext(), R.string.contact_saved_success, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } else {
                val updatedContact = Contact(
                    id = contactId,
                    nom = nom,
                    prenom = prenom,
                    telephone = telephone,
                    email = if (email.isEmpty()) null else email,
                    photoUri = photoUriStr,
                    latitude = lat,
                    longitude = lng
                )
                viewModel.updateContact(updatedContact) {
                    Toast.makeText(requireContext(), R.string.contact_saved_success, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
