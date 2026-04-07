package com.example.brokerapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.example.brokerapp.components.MainTabs
import com.example.brokerapp.models.PortfolioItem
import com.example.brokerapp.models.Stock
import com.example.brokerapp.screens.LoginScreen
import kotlinx.coroutines.delay

@Composable
fun BrokerAppContent(modifier: Modifier = Modifier) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var currentUser by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    // Данные для рынка
    val stocks = remember {
        mutableStateListOf(
            Stock("AAPL", "Apple Inc.", 175.50, 2.5),
            Stock("GOOGL", "Alphabet Inc.", 142.30, 1.2),
            Stock("MSFT", "Microsoft Corp.", 380.20, 0.8),
            Stock("AMZN", "Amazon.com Inc.", 145.80, -0.5),
            Stock("TSLA", "Tesla Inc.", 240.15, 5.3),
            Stock("META", "Meta Platforms", 312.45, 1.8)
        )
    }

    var balance by remember { mutableStateOf(10000.0) }
    val portfolio = remember { mutableStateListOf<PortfolioItem>() }

    // Автоматическое обновление цен
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            stocks.forEach { stock ->
                val randomChange = (Math.random() * 6 - 3) / 100
                stock.price *= (1 + randomChange)
                stock.changePercent = (Math.random() * 8 - 4)
            }
        }
    }

    // Функции для покупки/продажи
    fun onBuyClick(stock: Stock) {
        if (balance >= stock.price) {
            balance -= stock.price
            val existingItem = portfolio.find { it.stock.symbol == stock.symbol }
            if (existingItem != null) {
                existingItem.quantity++
            } else {
                portfolio.add(PortfolioItem(stock.copy(), 1))
            }
        }
    }

    fun onSellClick(stock: Stock) {
        val portfolioItem = portfolio.find { it.stock.symbol == stock.symbol }
        if (portfolioItem != null && portfolioItem.quantity > 0) {
            balance += stock.price
            portfolioItem.quantity--
            if (portfolioItem.quantity == 0) {
                portfolio.remove(portfolioItem)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (!isLoggedIn) {
            LoginScreen(
                username = username,
                password = password,
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onLoginClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        isLoggedIn = true
                        currentUser = username
                    }
                }
            )
        } else {
            MainTabs(
                username = currentUser,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onLogoutClick = {
                    isLoggedIn = false
                    username = ""
                    password = ""
                    currentUser = ""
                    selectedTab = 0
                    portfolio.clear()
                },
                stocks = stocks,
                balance = balance,
                portfolio = portfolio,
                onBuyClick = { stock -> onBuyClick(stock) },
                onSellClick = { stock -> onSellClick(stock) }
            )
        }
    }
}