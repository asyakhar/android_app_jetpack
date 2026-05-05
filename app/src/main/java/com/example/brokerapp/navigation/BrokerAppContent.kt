package com.example.brokerapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // ВАЖНЫЙ ИМПОРТ!
import com.example.brokerapp.components.MainTabs
import com.example.brokerapp.screens.LoginScreen
import com.example.brokerapp.viewmodel.BrokerViewModel

@Composable
fun BrokerAppContent(modifier: Modifier = Modifier) {
    // Подключаем нашу ViewModel
    val viewModel: BrokerViewModel = viewModel()

    // Подписываемся на реактивные данные с бэкенда
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentUser by viewModel.username.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val portfolio by viewModel.portfolio.collectAsState()

    val stockHistory by viewModel.stockHistory.collectAsState()

    // Локальные состояния только для UI-инпутов
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            LoginScreen(
                username = usernameInput,
                password = passwordInput,
                onUsernameChange = { usernameInput = it },
                onPasswordChange = { passwordInput = it },
                onLoginClick = {
                    if (usernameInput.isNotBlank() && passwordInput.isNotBlank()) {
                        // Вызываем реальный метод авторизации
                        viewModel.login(usernameInput, passwordInput)
                    }
                }
            )
        } else {
            MainTabs(
                username = currentUser,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onLogoutClick = {
                    viewModel.logout()
                    usernameInput = ""
                    passwordInput = ""
                    selectedTab = 0
                },
                stocks = stocks,
                balance = balance,
                portfolio = portfolio,
                stockHistory = stockHistory,
                onLoadHistory = { symbol -> viewModel.loadHistoryIfNeed(symbol) },
                onBuyClick = { stock -> viewModel.buyStock(stock) },
                onSellClick = { stock -> viewModel.sellStock(stock) }
            )
        }
    }
}