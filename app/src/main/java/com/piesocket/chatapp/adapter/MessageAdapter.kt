package com.piesocket.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piesocket.chatapp.R
import com.piesocket.chatapp.data.entity.Message
import com.piesocket.chatapp.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.messageText.text = message.content
            binding.messageTimeText.text = dateFormat.format(Date(message.timestamp))

            // Configure message appearance based on whether it's outgoing or incoming
            if (message.isOutgoing) {
                binding.messageCardView.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.message_bubble_outgoing)
                )
                
                // Show status for outgoing messages
                binding.messageStatusText.visibility = View.VISIBLE
                when (message.status) {
                    Message.STATUS_QUEUED -> {
                        binding.messageStatusText.text = binding.root.context.getString(R.string.message_queued)
                    }
                    Message.STATUS_SENT -> {
                        binding.messageStatusText.text = binding.root.context.getString(R.string.message_sent)
                    }
                    Message.STATUS_DELIVERED -> {
                        binding.messageStatusText.text = binding.root.context.getString(R.string.message_delivered)
                    }
                    Message.STATUS_READ -> {
                        binding.messageStatusText.text = binding.root.context.getString(R.string.message_read)
                    }
                }
            } else {
                binding.messageCardView.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.message_bubble_incoming)
                )
                binding.messageStatusText.visibility = View.GONE
            }

            // Adjust layout constraints for incoming/outgoing messages
            val layoutParams = binding.messageCardView.layoutParams as ViewGroup.MarginLayoutParams
            if (message.isOutgoing) {
                layoutParams.marginStart = (48 * binding.root.resources.displayMetrics.density).toInt()
                layoutParams.marginEnd = (8 * binding.root.resources.displayMetrics.density).toInt()
                
                // Set layout constraints for outgoing messages (right aligned)
                (binding.messageCardView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    horizontalBias = 1.0f  // Push to the end (right)
                }
            } else {
                layoutParams.marginStart = (8 * binding.root.resources.displayMetrics.density).toInt()
                layoutParams.marginEnd = (48 * binding.root.resources.displayMetrics.density).toInt()
                
                // Set layout constraints for incoming messages (left aligned)
                (binding.messageCardView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    horizontalBias = 0.0f  // Push to the start (left)
                }
            }
            binding.messageCardView.layoutParams = layoutParams
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
} 