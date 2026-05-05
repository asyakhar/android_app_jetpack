package com.example.brokerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokerapp.models.*
import com.example.brokerapp.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrokerViewModel : ViewModel() {
    val api = ApiClient()

    // Состояния для UI
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance = _balance.asStateFlow()

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks = _stocks.asStateFlow()

    private val _portfolio = MutableStateFlow<List<PortfolioItem>>(emptyList())
    val portfolio = _portfolio.asStateFlow()

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            try {
                api.login(LoginRequest(user, pass))
                _isLoggedIn.value = true
                _username.value = user

                loadData()
                startWebSocket() // <--- ЗАПУСКАЕМ ВЕБСОКЕТЫ ЗДЕСЬ
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _stocks.value = api.getStocks()
                _portfolio.value = api.getPortfolio()
                _balance.value = api.getBalance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun buyStock(stock: Stock) {
        viewModelScope.launch {
            try {
                // Пока покупаем по 1 штуке для простоты, потом можно добавить выбор количества
                api.executeTrade(TradeRequest(stock.symbol, 1, "buy"))
                loadData() // Обновляем портфель и баланс после сделки
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sellStock(stock: Stock) {
        viewModelScope.launch {
            try {
                api.executeTrade(TradeRequest(stock.symbol, 1, "sell"))
                loadData() // Обновляем портфель и баланс после сделки
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _username.value = ""
        _stocks.value = emptyList()
        _portfolio.value = emptyList()
        _balance.value = 0.0
        api.authToken = null // Сбрасываем токен
    }

    private fun startWebSocket() {
        viewModelScope.launch {
            try {
                api.observeLivePrices { update ->
                    // 1. Берем текущий список
                    val currentStocks = _stocks.value.toMutableList()
                    // 2. Ищем акцию, цена которой обновилась
                    val index = currentStocks.indexOfFirst { it.symbol == update.symbol }

                    if (index != -1) {
                        val oldStock = currentStocks[index]
                        // 3. Создаем КОПИЮ с новой ценой (чтобы Compose заметил изменение)
                        currentStocks[index] = oldStock.copy(price = update.price)
                        // 4. Пушим обновленный список в UI
                        _stocks.value = currentStocks
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}