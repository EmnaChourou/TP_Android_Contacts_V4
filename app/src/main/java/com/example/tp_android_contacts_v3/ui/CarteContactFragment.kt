package com.example.tp_android_contacts_v3.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.tp_android_contacts_v3.R
import com.example.tp_android_contacts_v3.viewmodel.ContactViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class CarteContactFragment : Fragment(R.layout.fragment_carte_contact) {

    private val viewModel: ContactViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        val contactIdStr = args?.getString("contact_id")
        val contactIdLong = args?.getLong("contact_id", 0L) ?: contactIdStr?.toLongOrNull() ?: 0L

        lifecycleScope.launch {
            val contact = if (contactIdLong != 0L) viewModel.getContactById(contactIdLong) else null

            val lat = if (args != null && args.containsKey("latitude")) args.getDouble("latitude") else contact?.latitude
            val lng = if (args != null && args.containsKey("longitude")) args.getDouble("longitude") else contact?.longitude
            val name = args?.getString("contact_name") ?: contact?.let { "${it.prenom} ${it.nom}" } ?: "Contact"
            val phone = args?.getString("contact_phone") ?: contact?.telephone ?: ""

            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap ->
                if (lat != null && lng != null) {
                    val position = LatLng(lat, lng)
                    googleMap.addMarker(MarkerOptions().position(position).title(name).snippet(phone))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
                } else {
                    Toast.makeText(requireContext(), R.string.error_no_coordinates, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
