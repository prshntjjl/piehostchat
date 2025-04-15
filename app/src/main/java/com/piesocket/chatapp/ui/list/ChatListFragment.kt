package com.piesocket.chatapp.ui.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piesocket.chatapp.R
import com.piesocket.chatapp.adapter.ChatAdapter
import com.piesocket.chatapp.databinding.FragmentChatListBinding
import com.piesocket.chatapp.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        // Set up RecyclerView
        chatAdapter = ChatAdapter { chat ->
            // Handle chat item click - navigate to chat detail
            val action = ChatListFragmentDirections.actionChatListToChat(chat.id)
            findNavController().navigate(action)
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        // Set up SwipeRefreshLayout with our primary color
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupObservers() {
        // Observe chats
        viewModel.allChats.observe(viewLifecycleOwner) { chats ->
            Log.d("ChatListFragment", "Received ${chats.size} chats, updating list")
            // Debug chat list content
            chats.forEach { chat ->
                Log.d(
                    "ChatListFragment",
                    "Chat: ${chat.id} - ${chat.name} - ${chat.lastMessage} - ${chat.lastUpdated}"
                )
            }

            // Force adapter refresh with completely new adapter
            binding.chatRecyclerView.adapter = null
            chatAdapter = ChatAdapter { chat ->
                val action = ChatListFragmentDirections.actionChatListToChat(chat.id)
                findNavController().navigate(action)
            }
            binding.chatRecyclerView.adapter = chatAdapter

            // Submit list
            chatAdapter.submitList(chats)
            updateEmptyState(chats.isEmpty())
        }

    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.swipeRefreshLayout.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}