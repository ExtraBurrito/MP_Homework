package com.example.contacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.contacts.ui.theme.ContactsTheme
import android.net.Uri
class MainActivity : ComponentActivity() {


    private val viewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    ContactsAppNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactsAppNavigation(viewModel: ContactViewModel, modifier: Modifier = Modifier) {

    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = "main_screen",
        modifier = modifier
    ) {

        composable("main_screen") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToUpdate = { contactId ->
                    navController.navigate("update_screen/$contactId")
                }
            )
        }
        composable(
            route = "update_screen/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: -1

            UpdateDeleteScreen(
                contactId = contactId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMap = { name, address ->
                    val safeName = if (name.isNotBlank()) name else "Неизвестен"
                    val safeAddress = if (address.isNotBlank()) address else "Няма_адрес"
                    val encodedName = Uri.encode(safeName)
                    val encodedAddress = Uri.encode(safeAddress)
                    navController.navigate("maps_screen/$encodedName/$encodedAddress")
                }
            )
        }
        composable(
            route = "maps_screen/{name}/{address}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: ""

            MapsScreen(
                name = name,
                address = address,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}