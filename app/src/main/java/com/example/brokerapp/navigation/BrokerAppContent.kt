package com.example.brokerapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brokerapp.components.MainTabs
import com.example.brokerapp.screens.LoginScreen
import com.example.brokerapp.screens.RegisterScreen
import com.example.brokerapp.viewmodel.BrokerViewModel

@Composable
fun BrokerAppContent(modifier: Modifier = Modifier) {
    val viewModel: BrokerViewModel = viewModel()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentUser by viewModel.username.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val portfolio by viewModel.portfolio.collectAsState()
    val stockHistory by viewModel.stockHistory.collectAsState()

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoginMode by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            if (isLoginMode) {
                LoginScreen(
                    username = usernameInput,
                    password = passwordInput,
                    onUsernameChange = { usernameInput = it },
                    onPasswordChange = { passwordInput = it },
                    onLoginClick = {
                        if (usernameInput.isNotBlank() && passwordInput.isNotBlank()) {
                            viewModel.login(usernameInput, passwordInput)
                        }
                    },
                    onSwitchToRegister = {
                        isLoginMode = false
                        emailInput = ""
                    }
                )
            } else {
                RegisterScreen(
                    username = usernameInput,
                    email = emailInput,
                    password = passwordInput,
                    onUsernameChange = { usernameInput = it },
                    onEmailChange = { emailInput = it },
                    onPasswordChange = { passwordInput = it },
                    onRegisterClick = {
                        if (usernameInput.isNotBlank() && emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            viewModel.register(usernameInput, emailInput, passwordInput)
                        }
                    },
                    onSwitchToLogin = {
                        isLoginMode = true
                    }
                )
            }
        } else {
            MainTabs(
                username = currentUser,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onLogoutClick = {
                    viewModel.logout()
                    usernameInput = ""
                    passwordInput = ""
                    emailInput = ""
                    selectedTab = 0
                },
                stocks = stocks,
                balance = balance,
                portfolio = portfolio,
                stockHistory = stockHistory,
                onLoadHistory = { symbol -> viewModel.loadHistory(symbol) },
                onBuyClick = { stock -> viewModel.buyStock(stock) },
                onSellClick = { stock -> viewModel.sellStock(stock) }
            )
        }
    }
}