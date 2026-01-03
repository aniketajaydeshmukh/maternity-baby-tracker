package com.maternitytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maternitytracker.data.database.AppDatabase
import com.maternitytracker.data.repository.ShoppingRepository
import com.maternitytracker.ui.screens.BudgetScreen
import com.maternitytracker.ui.screens.FilterScreen
import com.maternitytracker.ui.screens.HomeScreen
import com.maternitytracker.ui.theme.MaternityBabyTrackerTheme
import com.maternitytracker.viewmodel.ShoppingViewModel
import com.maternitytracker.viewmodel.ViewModelFactory

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Filter : Screen("filter", "Filter", Icons.Filled.FilterList)
    object Budget : Screen("budget", "Budget", Icons.Filled.AccountBalance)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaternityBabyTrackerTheme {
                MaternityBabyTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaternityBabyTrackerApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { 
        ShoppingRepository(database.shoppingItemDao(), database.labelDao()) 
    }
    val viewModel: ShoppingViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )
    
    val navController = rememberNavController()
    val items = remember { mutableStateOf(emptyList<com.maternitytracker.data.entities.ItemWithLabels>()) }
    val labels = remember { mutableStateOf(emptyList<com.maternitytracker.data.entities.Label>()) }
    val filterState by viewModel.filterState.collectAsState()
    val budgetSummary by viewModel.budgetSummary.collectAsState()
    
    // Collect filtered items
    LaunchedEffect(Unit) {
        viewModel.filteredItems.collect { itemsList ->
            items.value = itemsList
        }
    }
    
    // Collect all labels
    LaunchedEffect(Unit) {
        viewModel.allLabels.collect { labelsList ->
            labels.value = labelsList
        }
    }
    
    val screens = listOf(Screen.Home, Screen.Budget)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Maternity & Baby Tracker",
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    items = items.value,
                    labels = labels.value,
                    filterState = filterState,
                    onAddItem = { name, quantity, budget, labelIds ->
                        viewModel.addItem(name, quantity, budget, labelIds)
                    },
                    onUpdateItem = { itemWithLabels, labelIds ->
                        viewModel.updateItem(itemWithLabels.item, labelIds)
                    },
                    onDeleteItem = { itemWithLabels ->
                        viewModel.deleteItem(itemWithLabels.item)
                    },
                    onTogglePurchased = { itemWithLabels ->
                        viewModel.toggleItemPurchased(itemWithLabels.item)
                    },
                    onAddLabel = { labelName ->
                        viewModel.addLabel(labelName)
                    },
                    onFilterClick = {
                        navController.navigate(Screen.Filter.route)
                    }
                )
            }
            
            composable(Screen.Filter.route) {
                FilterScreen(
                    labels = labels.value,
                    filterState = filterState,
                    onApplyFilter = { selectedLabels, useAndLogic ->
                        viewModel.updateFilter(selectedLabels, useAndLogic)
                    },
                    onClearFilter = {
                        viewModel.clearFilter()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Budget.route) {
                BudgetScreen(
                    budgetSummary = budgetSummary
                )
            }
        }
    }
}