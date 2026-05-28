package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.MarketplaceRepository
import com.example.ui.MainAppUI
import com.example.ui.MarketplaceViewModel
import com.example.ui.MarketplaceViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MarketplaceViewModel by lazy {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "marketplace_local_db"
        )
            .fallbackToDestructiveMigration()
            .build()
        val repository = MarketplaceRepository(db)
        ViewModelProvider(this, MarketplaceViewModelFactory(repository))[MarketplaceViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppUI(viewModel = viewModel)
            }
        }
    }
}
