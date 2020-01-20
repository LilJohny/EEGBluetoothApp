package com.example.bluetoothapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.bluetoothapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        val actionBar = supportActionBar
        actionBar?.hide()
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
// menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_monitor, R.id.navigation_settings
        )
            .build()
        val navController =
            Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)
    }
    override fun onBackPressed() {
        val fm: FragmentManager = supportFragmentManager
        if(fm.backStackEntryCount > 0){
            fm.popBackStackImmediate()
        }
        else{
            super.onBackPressed()
        }
    }

}