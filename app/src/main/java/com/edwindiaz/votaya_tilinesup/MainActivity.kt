//MainActivity.kt
package com.edwindiaz.votaya_tilinesup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.edwindiaz.votaya_tilinesup.core.navigation.AppNavigation
import com.edwindiaz.votaya_tilinesup.core.ui.theme.VotaYaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VotaYaTheme {
                AppNavigation()
            }
        }
    }
}