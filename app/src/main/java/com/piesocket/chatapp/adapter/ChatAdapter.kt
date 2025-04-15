package com.piesocket.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piesocket.chatapp.data.entity.Chat
import com.piesocket.chatapp.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val onChatClicked: (Chat) -> Unit) :
    ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = getItem(position)
        holder.bind(chat)
    }

    override fun submitList(list: List<Chat>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChatClicked(getItem(position))
                }
            }
        }

        fun bind(chat: Chat) {
            binding.chatNameText.text = chat.name
            binding.lastMessageText.text = chat.lastMessage
            
            // Set the avatar initial from the first letter of chat name
            if (chat.name.isNotEmpty()) {
                binding.avatarInitialText.text = chat.name.first().toString().uppercase()
            } else {
                binding.avatarInitialText.text = "?"
            }
            
            // Format and set timestamp
            chat.lastUpdated?.let {
                binding.timestampText.text = timeFormat.format(Date(it))
            }
            
            // Set unread count or hide the badge if zero
            if (chat.unreadCount > 0) {
                binding.unreadCountBadge.visibility = View.VISIBLE
                binding.unreadCountBadge.text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString()
            } else {
                binding.unreadCountBadge.visibility = View.GONE
            }
            
            // Hide message status by default - only show when needed
            binding.messageStatusText.visibility = View.GONE
        }
    }

    private class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.name == newItem.name &&
                   oldItem.lastMessage == newItem.lastMessage &&
                   oldItem.lastUpdated == newItem.lastUpdated &&
                   oldItem.unreadCount == newItem.unreadCount
        }
    }
} 