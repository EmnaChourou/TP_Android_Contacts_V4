package com.example.tp_android_contacts_v3.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp_android_contacts_v3.R
import com.example.tp_android_contacts_v3.adapter.ContactAdapter
import com.example.tp_android_contacts_v3.utils.PreferencesManager
import com.example.tp_android_contacts_v3.viewmodel.ContactViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var prefsManager: PreferencesManager
    private lateinit var rvContacts: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = PreferencesManager(requireContext())
        val etSearch = view.findViewById<EditText>(R.id.et_search)
        rvContacts = view.findViewById<RecyclerView>(R.id.rv_contacts)
        tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_contact)

        rvContacts.layoutManager = LinearLayoutManager(requireContext())

        setupAdapter(emptyList())

        viewModel.contactsLiveData.observe(viewLifecycleOwner) { contacts ->
            updateAdapterWithPreferences(contacts)
        }

        etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }

        fabAdd.setOnClickListener {
            val detailFragment = DetailContactFragment() // No arguments -> creation mode
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadContacts()
    }

    private fun setupAdapter(contacts: List<com.example.tp_android_contacts_v3.model.Contact>) {
        val fontSize = prefsManager.getFontSize()
        val textColor = prefsManager.getTextColor()
        val showCall = prefsManager.isCallFromListEnabled()

        adapter = ContactAdapter(
            contacts = contacts,
            fontSize = fontSize,
            textColor = textColor,
            showCallButton = showCall,
            onItemClick = { contact ->
                val detailFragment = DetailContactFragment().apply {
                    arguments = Bundle().apply {
                        putLong("contact_id", contact.id)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onCallClick = { contact ->
                val phone = contact.telephone.trim()
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), R.string.no_app_available, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        rvContacts.adapter = adapter
    }

    private fun updateAdapterWithPreferences(contacts: List<com.example.tp_android_contacts_v3.model.Contact>) {
        setupAdapter(contacts)
        if (contacts.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvContacts.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvContacts.visibility = View.VISIBLE
        }
    }
}
