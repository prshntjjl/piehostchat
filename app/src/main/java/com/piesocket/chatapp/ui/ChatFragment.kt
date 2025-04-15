package com.piesocket.chatapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.piesocket.chatapp.R
import com.piesocket.chatapp.adapter.MessageAdapter
import com.piesocket.chatapp.databinding.FragmentChatBinding
import com.piesocket.chatapp.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var messageAdapter: MessageAdapter
    private val args: ChatFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mark this chat as read when opening
        viewModel.markChatAsRead(args.chatId)
        
        // Set up RecyclerView
        messageAdapter = MessageAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        // Set up send button with animation
        binding.sendButton.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.button_press))
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageEditText.text?.clear()
            }
        }
        
        // Add input text watcher for interaction feedback
        binding.messageEditText.addTextChangedListener { text ->
            binding.sendButton.isEnabled = !text.isNullOrBlank()
            if (!text.isNullOrBlank() && !binding.sendButton.isEnabled) {
                binding.sendButton.alpha = 1.0f
            } else if (text.isNullOrBlank() && binding.sendButton.isEnabled) {
                binding.sendButton.alpha = 0.6f
            }
        }

        // Initialize send button state
        binding.sendButton.alpha = 0.6f
        binding.sendButton.isEnabled = false

        // Observe the chat and messages
        viewModel.getChat(args.chatId).observe(viewLifecycleOwner) { chat ->
            // Set both activity title and toolbar title for consistency
            activity?.title = chat.name
            (activity as? AppCompatActivity)?.supportActionBar?.title = chat.name
        }
        
        viewModel.getChatMessages(args.chatId).observe(viewLifecycleOwner) { messages ->
            if (messages != null) {
                messageAdapter.submitList(ArrayList(messages)) {
                    if (messages.isNotEmpty()) {
                        binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        // Observe network status
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner) { isAvailable ->
            // Always allow sending messages even when offline
            if (!isAvailable) {
                Toast.makeText(context, R.string.offline_mode, Toast.LENGTH_SHORT).show()
            } else {
                // When network is restored, retry sending queued messages
                viewModel.retryQueuedMessages()
            }
        }
    }

    private fun sendMessage(content: String) {
        val isOffline = !viewModel.isNetworkAvailable.value!!
        viewModel.sendMessage(args.chatId, content)
        
        // Show a short message if we're sending while offline
        if (isOffline) {
            Toast.makeText(
                context,
                R.string.message_queued,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 