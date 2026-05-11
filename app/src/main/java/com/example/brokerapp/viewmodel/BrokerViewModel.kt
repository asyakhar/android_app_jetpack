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

    private val _stockHistory = MutableStateFlow<Map<String, List<PriceHistoryCandle>>>(emptyMap())
    val stockHistory = _stockHistory.asStateFlow()

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            try {
                api.login(LoginRequest(user, pass))
                _isLoggedIn.value = true
                _username.value = user

                loadData()
                startWebSocket()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                api.register(username, email, password)
                _isLoggedIn.value = true
                _username.value = username
                loadData()
                startWebSocket()
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
                api.executeTrade(TradeRequest(stock.symbol, 1, "buy"))
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sellStock(stock: Stock) {
        viewModelScope.launch {
            try {
                api.executeTrade(TradeRequest(stock.symbol, 1, "sell"))
                loadData()
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
        _stockHistory.value = emptyMap()
        api.authToken = null
    }

//    fun loadHistory(symbol: String, timeframe: ChartTimeframe = ChartTimeframe.DAY_1) {
//        viewModelScope.launch {
//            try {
//                // Используем простой запрос без from/to
//                val history = api.getStockHistorySimple(
//                    symbol = symbol,
//                    interval = timeframe.interval
//                )
//
//                val currentMap = _stockHistory.value.toMutableMap()
//                currentMap[symbol] = history
//                _stockHistory.value = currentMap
//
//                println("✅ Загружено ${history.size} свечей для $symbol")
//            } catch (e: Exception) {
//                e.printStackTrace()
//                println("❌ Ошибка загрузки истории для $symbol: ${e.message}")
//            }
//        }
//    }

    private fun startWebSocket() {
        viewModelScope.launch {
            try {
                api.observeLivePrices { update ->
                    // 1. Обновляем цену в списке
                    val currentStocks = _stocks.value.toMutableList()
                    val index = currentStocks.indexOfFirst { it.symbol == update.symbol }
                    if (index != -1) {
                        currentStocks[index] = currentStocks[index].copy(price = update.price)
                        _stocks.value = currentStocks
                    }

                    // 2. Обновляем последнюю свечу в истории
                    updateLastCandle(update.symbol, update.price)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateLastCandle(symbol: String, newPrice: Double) {
        val currentMap = _stockHistory.value.toMutableMap()
        val currentHistory = currentMap[symbol]?.toMutableList() ?: return
        if (currentHistory.isEmpty()) return

        val lastCandle = currentHistory.last()
        val updatedCandle = lastCandle.copy(
            high = maxOf(lastCandle.high, newPrice),
            low = minOf(lastCandle.low, newPrice),
            close = newPrice,
            volume = lastCandle.volume + 1
        )

        currentHistory[currentHistory.size - 1] = updatedCandle
        currentMap[symbol] = currentHistory
        _stockHistory.value = currentMap
    }
    fun loadHistory(symbol: String) {
        viewModelScope.launch {
            try {
                val history = api.getStockHistoryMinute(symbol)

                val currentMap = _stockHistory.value.toMutableMap()
                currentMap[symbol] = history
                _stockHistory.value = currentMap

                println("✅ Загружено ${history.size} свечей для $symbol")

                if (history.isNotEmpty()) {
                    println("📊 Первая свеча: ${history.first()}")
                    println("📊 Последняя свеча: ${history.last()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Ошибка: ${e.message}")
            }
        }
    }
}