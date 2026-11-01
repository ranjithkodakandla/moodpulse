package com.moodpulse.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moodpulse.app.AppContainer
import com.moodpulse.app.smartcontext.SmartContextViewModel
import com.moodpulse.app.ui.history.HistoryScreen
import com.moodpulse.app.ui.history.HistoryViewModel
import com.moodpulse.app.ui.home.HomeScreen
import com.moodpulse.app.ui.home.HomeViewModel
import com.moodpulse.app.ui.insights.InsightsScreen
import com.moodpulse.app.ui.insights.InsightsViewModel
import com.moodpulse.app.ui.log.LogMoodSheet
import com.moodpulse.app.ui.log.LogMoodViewModel
import com.moodpulse.app.ui.settings.SettingsScreen
import com.moodpulse.app.ui.settings.SettingsViewModel
import com.moodpulse.app.ui.smartcontext.DigitalHabitsScreen
import com.moodpulse.app.ui.smartcontext.SmartContextOnboardingScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Today", Icons.Outlined.Home)
    object History : Screen("history", "History", Icons.Outlined.CalendarMonth)
    object Insights : Screen("insights", "Pulse", Icons.Outlined.BarChart)
    object Habits : Screen("habits", "Habits", Icons.Outlined.Insights)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Tune)
}

val bottomNavItems = listOf(Screen.Home, Screen.History, Screen.Insights, Screen.Habits, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodPulseNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    var showLogSheet by remember { mutableStateOf(false) }
    var editEntryId by remember { mutableStateOf<Long?>(null) }

    val logVm: LogMoodViewModel = viewModel(factory = LogMoodViewModel.Factory(container.moodRepository))
    val logState by logVm.state.collectAsStateWithLifecycle()

    val smartContextVm: SmartContextViewModel = viewModel(
        factory = SmartContextViewModel.Factory(container.database, container.context)
    )
    val smartContextState by smartContextVm.uiState.collectAsStateWithLifecycle()

    // Auto-close sheet on save
    LaunchedEffect(logState.saved) {
        if (logState.saved) {
            showLogSheet = false
            editEntryId = null
            logVm.reset()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
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
                val vm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(container.moodRepository, container.database)
                )
                val state by vm.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    uiState = state,
                    onLogMood = { showLogSheet = true },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onEntryClick = { id ->
                        editEntryId = id
                        logVm.loadForEdit(id)
                        showLogSheet = true
                    },
                    onSuggestionConfirm = smartContextVm::confirmSuggestion,
                    onSuggestionDismiss = smartContextVm::dismissSuggestion,
                    pendingSuggestion = smartContextState.pendingSuggestion
                )
            }
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(container.moodRepository))
                val state by vm.uiState.collectAsStateWithLifecycle()
                HistoryScreen(
                    uiState = state,
                    onDateSelected = vm::selectDate,
                    onEntryClick = { id ->
                        editEntryId = id
                        logVm.loadForEdit(id)
                        showLogSheet = true
                    }
                )
            }
            composable(Screen.Insights.route) {
                val vm: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory(container.moodRepository))
                val state by vm.uiState.collectAsStateWithLifecycle()
                InsightsScreen(uiState = state)
            }
            composable(Screen.Habits.route) {
                DigitalHabitsScreen(
                    uiState = smartContextState,
                    viewModel = smartContextVm,
                    onNavigateToOnboarding = { navController.navigate("smart_context_onboarding") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("smart_context_onboarding") {
                SmartContextOnboardingScreen(
                    hasPermission = smartContextState.hasPermission,
                    onOpenPermissionSettings = smartContextVm::openPermissionSettings,
                    onEnable = {
                        smartContextVm.setMonitoringEnabled(true)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.preferencesRepository,
                        container.moodRepository,
                        container.context
                    )
                )
                val state by vm.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = state,
                    onRemindersToggled = vm::setRemindersEnabled,
                    onReminderTimeSet = vm::setReminderTime,
                    onThemeChanged = vm::setThemeMode,
                    onExportJson = vm::exportJson,
                    onExportCsv = vm::exportCsv,
                    onImport = vm::importJson,
                    onDeleteAllRequest = vm::showDeleteConfirm,
                    onDeleteAllConfirm = vm::deleteAllData,
                    onDeleteDismiss = vm::dismissDeleteConfirm,
                    onClearMessages = vm::clearMessages,
                    appVersion = container.appVersion,
                    smartContextState = smartContextState,
                    onSmartContextToggle = { enabled ->
                        if (enabled && !smartContextState.hasPermission) {
                            navController.navigate("smart_context_onboarding")
                        } else {
                            smartContextVm.setMonitoringEnabled(enabled)
                        }
                    }
                )
            }
        }
    }

    // Log mood bottom sheet
    if (showLogSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showLogSheet = false
                editEntryId = null
                logVm.reset()
            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            LogMoodSheet(
                state = logState,
                onMoodSelected = logVm::selectMood,
                onTagToggled = logVm::toggleTag,
                onNoteChanged = logVm::updateNote,
                onSave = { if (editEntryId != null) logVm.saveEdit() else logVm.save() },
                onDelete = editEntryId?.let { id ->
                    {
                        logVm.deleteEntry(id) {
                            showLogSheet = false
                            editEntryId = null
                            logVm.reset()
                        }
                    }
                },
                onDismiss = {
                    showLogSheet = false
                    editEntryId = null
                    logVm.reset()
                }
            )
        }
    }
}
