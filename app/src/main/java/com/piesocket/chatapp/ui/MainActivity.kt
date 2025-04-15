package com.piesocket.chatapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.piesocket.chatapp.R
import com.piesocket.chatapp.databinding.ActivityMainBinding
import com.piesocket.chatapp.network.WebSocketManager
import com.piesocket.chatapp.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.chatListFragment)
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Observe network status
        viewModel.isNetworkAvailable.observe(this) { isAvailable ->
            binding.networkStatusText.visibility = if (isAvailable) View.GONE else View.VISIBLE
        }

        // Observe WebSocket status
        viewModel.webSocketStatus.observe(this) { status ->
            when (status) {
                WebSocketManager.ConnectionStatus.ERROR -> {
                    Toast.makeText(this, R.string.error_connecting_to_server, Toast.LENGTH_SHORT).show()
                }
                WebSocketManager.ConnectionStatus.CONNECTED -> {
                    // We could show a connected indication if needed
                }
                else -> {
                    // Handle other states if needed
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}