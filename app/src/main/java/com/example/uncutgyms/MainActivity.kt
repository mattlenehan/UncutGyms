package com.example.uncutgyms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.uncutgyms.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val navController: NavController by lazy {
        Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
    }

    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    private val appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.fragment_gyms
        )
    )

    private val navigationDestinationListener =
        NavController.OnDestinationChangedListener { _, _: NavDestination, _ ->
            // can record screen view events here
            // or check nav destination to set visibility for toolbar and title
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController.addOnDestinationChangedListener(navigationDestinationListener)

        binding.appBar.background = null

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}