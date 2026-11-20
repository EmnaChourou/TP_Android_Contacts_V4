package com.example.tp_android_contacts_v3.adapter

import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tp_android_contacts_v3.R
import com.example.tp_android_contacts_v3.model.Contact

class ContactAdapter(
    private var contacts: List<Contact>,
    private val fontSize: Float,
    private val textColor: Int,
    private val showCallButton: Boolean,
    private val onItemClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.iv_contact_photo)
        val tvName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_contact_phone)
        val btnCall: ImageButton = itemView.findViewById(R.id.btn_row_call)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val fullName = "${contact.prenom} ${contact.nom}"
        holder.tvName.text = fullName
        holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        holder.tvName.setTextColor(textColor)

        holder.tvPhone.text = contact.telephone
        holder.tvPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize - 2)

        if (showCallButton) {
            holder.btnCall.visibility = View.VISIBLE
            holder.btnCall.setOnClickListener {
                onCallClick(contact)
            }
        } else {
            holder.btnCall.visibility = View.GONE
        }

        if (!contact.photoUri.isNullOrEmpty()) {
            try {
                holder.ivPhoto.setImageURI(Uri.parse(contact.photoUri))
            } catch (e: Exception) {
                holder.ivPhoto.setImageResource(R.drawable.ic_avatar_placeholder)
            }
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        holder.itemView.setOnClickListener {
            onItemClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
