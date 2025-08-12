package com.example.otebookbeta

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.otebookbeta.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Starting onCreate")
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setSupportActionBar(binding.appBarMain.toolbar)

            drawerLayout = binding.drawerLayout
            navController = findNavController(R.id.nav_host_fragment_content_main)

            appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
            setupActionBarWithNavController(navController, appBarConfiguration)
            binding.navView.setupWithNavController(navController)

            binding.navView.itemIconTintList = null
            binding.navView.setNavigationItemSelectedListener { menuItem ->
                handleNavigationItemSelected(menuItem)
                true
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                binding.appBarMain.fab.visibility = when (destination.id) {
                    R.id.addContactFragment,
                    R.id.noteListFragment,
                    R.id.addNoteFragment,
                    R.id.contactDetailFragment,
                    R.id.editNoteFragment -> View.GONE
                    else -> View.VISIBLE
                }
                invalidateOptionsMenu() // чтобы корректно обновлять меню
            }

            binding.appBarMain.fab.setOnClickListener {
                try {
                    if (navController.currentDestination?.id != R.id.addContactFragment) {
                        navController.navigate(R.id.action_global_addContactFragment)
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "FAB navigation error: ${e.message}", e)
                    Toast.makeText(this, "Ошибка навигации: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Ошибка в MainActivity: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return try {
            menuInflater.inflate(R.menu.main, menu)
            val deleteItem = menu.findItem(R.id.action_delete)
            deleteItem.isVisible = navController.currentDestination?.id == R.id.contactDetailFragment
            true
        } catch (e: Exception) {
            Log.e("MainActivity", "Error inflating menu: ${e.message}", e)
            false
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val deleteItem = menu.findItem(R.id.action_delete)
        deleteItem.isVisible = navController.currentDestination?.id == R.id.contactDetailFragment
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                return if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START); true
                } else {
                    if (navController.currentDestination?.id == R.id.nav_home) {
                        binding.drawerLayout.openDrawer(GravityCompat.START); true
                    } else {
                        navController.navigateUp(); true
                    }
                }
            }
            R.id.action_delete -> {
                // Вместо поиска фрагмента — отправляем событие текущему back stack entry
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("menu_delete_click", true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> navController.navigate(R.id.nav_home)
            R.id.nav_support -> openTelegramSupport()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun openTelegramSupport() {
        try {
            val telegramUrl = "https://t.me/TouchNotebook"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Telegram не установлен. Установите приложение Telegram.", Toast.LENGTH_LONG).show()
            try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=org.telegram.messenger")
                }
                startActivity(playStoreIntent)
            } catch (_: Exception) {
                try {
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")
                    }
                    startActivity(webIntent)
                } catch (_: Exception) { }
            }
        }
    }
}
