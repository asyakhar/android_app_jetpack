package com.example.brokerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokerapp.models.*
import com.example.brokerapp.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.brokerapp.models.ChartTimeframe

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
                    // 1. Обновляем цену в списке (уже есть)
                    val currentStocks = _stocks.value.toMutableList()
                    val index = currentStocks.indexOfFirst { it.symbol == update.symbol }
                    if (index != -1) {
                        currentStocks[index] = currentStocks[index].copy(price = update.price)
                        _stocks.value = currentStocks
                    }

                    addNewPriceToHistory(update.symbol, update.price)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _stockHistory = MutableStateFlow<Map<String, List<PriceHistoryCandle>>>(emptyMap())
    val stockHistory = _stockHistory.asStateFlow()


    fun loadHistory(symbol: String, timeframe: ChartTimeframe = ChartTimeframe.DAY_1) {
        viewModelScope.launch {
            try {
                // Вычисляем from и to в секундах
                val to = System.currentTimeMillis() / 1000
                val from = (System.currentTimeMillis() - timeframe.durationMillis) / 1000

                // Загружаем данные с бэкенда
                val history = api.getStockHistory(symbol, from, to, timeframe.interval)

                // Обновляем Map
                val currentMap = _stockHistory.value.toMutableMap()
                currentMap[symbol] = history
                _stockHistory.value = currentMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    fun loadHistoryIfNeed(symbol: String) {
//        if (_stockHistory.value.containsKey(symbol)) return
//
//        viewModelScope.launch {
//            try {
//                val history = api.getStockHistory(symbol, interval = "1m")
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
private fun addNewPriceToHistory(symbol: String, newPrice: Double) {
    val currentMap = _stockHistory.value.toMutableMap()

    // Берем текущую историю. Если её еще нет (не успела загрузиться), то пока ничего не делаем
    val currentHistory = currentMap[symbol]?.toMutableList() ?: return
    val lastCandle = currentHistory.lastOrNull() ?: return

    // Просто обновляем самую последнюю (текущую) свечу нужного таймфрейма
    val updatedCandle = lastCandle.copy(
        high = maxOf(lastCandle.high, newPrice),
        low = minOf(lastCandle.low, newPrice),
        close = newPrice
        // open и timestamp оставляем без изменений!
    )

    currentHistory[currentHistory.size - 1] = updatedCandle

    // Обновляем состояние, чтобы Canvas перерисовался
    currentMap[symbol] = currentHistory
    _stockHistory.value = currentMap
}
}