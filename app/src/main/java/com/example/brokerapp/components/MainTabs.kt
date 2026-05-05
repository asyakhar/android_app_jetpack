package com.example.brokerapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brokerapp.models.PortfolioItem
import com.example.brokerapp.models.Stock
import com.example.brokerapp.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(
    username: String,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onLogoutClick: () -> Unit,
    stocks: List<Stock>,
    balance: Double,
    portfolio: List<PortfolioItem>,
    stockHistory: Map<String, List<com.example.brokerapp.models.PriceHistoryCandle>>,
    onLoadHistory: (String) -> Unit,
    onBuyClick: (Stock) -> Unit,
    onSellClick: (Stock) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            username.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Аккаунт", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            },
            actions = {
                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Выход")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                label = { Text("Рынок") },
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                label = { Text("Портфель") },
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Профиль") },
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }

        when (selectedTab) {
            0 -> MarketScreen(stocks, balance, portfolio, stockHistory, onLoadHistory, onBuyClick, onSellClick)
            1 -> PortfolioScreen(username, portfolio)
            2 -> ProfileScreen(username, onLogoutClick)
        }
    }
}