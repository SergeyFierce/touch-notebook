package com.example.otebookbeta

import android.content.res.ColorStateList
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.databinding.ItemContactCardBinding
import com.example.otebookbeta.utils.StatusUi
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val onLongClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    class ContactViewHolder(
        private val binding: ItemContactCardBinding,
        private val onClick: (Contact) -> Unit,
        private val onLongClick: (Contact) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            // Имя
            binding.contactName.text = contact.name

            // Статус (из макета есть только один TextView contact_status)
            binding.contactStatus.text = contact.status ?: ""
            updateStatusColor(contact.status)
            binding.contactStatus.visibility =
                if (contact.status.isNullOrBlank()) View.GONE else View.VISIBLE

            // Теги
            val group: ChipGroup = binding.tagsGroup
            group.removeAllViews()
            group.visibility = View.GONE

            contact.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.forEach { tag ->
                val chip = Chip(binding.root.context).apply {
                    text = tag
                    isClickable = false
                    isCheckable = false
                    setEnsureMinTouchTargetSize(false)

                    when (tag) {
                        "VIP" -> {
                            setTextColor(ContextCompat.getColor(context, R.color.black))
                            chipBackgroundColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.yellow)
                            )
                            chipStrokeWidth = 1f
                            chipStrokeColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.black)
                            )
                        }
                        "Срочно" -> {
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                            chipBackgroundColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.red)
                            )
                        }
                        "Напомнить" -> {
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                            chipBackgroundColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.purple_500)
                            )
                        }
                        else -> {
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                            chipBackgroundColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.purple_500)
                            )
                        }
                    }

                    // компактные отступы
                    val vPad = 6.dpToPx()
                    setPadding(12, vPad, 12, vPad)
                }
                group.addView(chip)
                group.visibility = View.VISIBLE
            }

            binding.contactCard.setOnClickListener { onClick(contact) }
            binding.contactCard.setOnLongClickListener { onLongClick(contact); true }
        }

        private fun updateStatusColor(status: String?) {
            // Цвет для плашки статуса
            val mapped = StatusUi.colorResFor(status)
            val colorRes = if (mapped == android.R.color.transparent) R.color.purple_500 else mapped
            val drawable = ContextCompat.getDrawable(binding.root.context, R.drawable.status_background)?.mutate()
            drawable?.setTint(ContextCompat.getColor(binding.root.context, colorRes))
            binding.contactStatus.background = drawable
        }

        private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem == newItem
}
