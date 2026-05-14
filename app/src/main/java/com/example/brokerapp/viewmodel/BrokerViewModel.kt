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


    // Статус авторизации
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    // Имя текущего пользователя
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    // Баланс счета пользователя
    private val _balance = MutableStateFlow(0.0)
    val balance = _balance.asStateFlow()

    // Список всех акций с биржи
    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks = _stocks.asStateFlow()

    // Портфель пользователя (какие акции и в каком количестве)
    private val _portfolio = MutableStateFlow<List<PortfolioItem>>(emptyList())
    val portfolio = _portfolio.asStateFlow()

    // История цен для графиков (ключ - символ акции, значение - список свечей)
    private val _stockHistory = MutableStateFlow<Map<String, List<PriceHistoryCandle>>>(emptyMap())
    val stockHistory = _stockHistory.asStateFlow()


    // Вход в аккаунт
    fun login(user: String, pass: String) {
        viewModelScope.launch {  // Запускаем в корутине
            try {
                api.login(LoginRequest(user, pass))  // Отправляем запрос на сервер
                _isLoggedIn.value = true              // Обновляем статус
                _username.value = user                // Сохраняем имя

                loadData()      // Загружаем данные пользователя
                startWebSocket() // Подключаем WebSocket для живых цен
            } catch (e: Exception) {
                e.printStackTrace()  // Логируем ошибку
            }
        }
    }

    // Регистрация нового пользователя
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                api.register(username, email, password)  // Отправляем запрос на сервер
                _isLoggedIn.value = true
                _username.value = username
                loadData()
                startWebSocket()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Загрузка всех данных (акции, портфель, баланс)
    fun loadData() {
        viewModelScope.launch {
            try {
                val allStocks = api.getStocks()
                val allowedSymbols = listOf("AAPL", "AMZN", "MSFT", "GOOGL", "TSLA")
                _stocks.value = allStocks.filter { it.symbol in allowedSymbols }

                _portfolio.value = api.getPortfolio()
                _balance.value = api.getBalance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Покупка акции
    fun buyStock(stock: Stock) {
        viewModelScope.launch {
            try {
                api.executeTrade(TradeRequest(stock.symbol, 1, "buy"))  // Отправляем запрос на покупку
                loadData()  // Обновляем данные после покупки
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Продажа акции
    fun sellStock(stock: Stock) {
        viewModelScope.launch {
            try {
                api.executeTrade(TradeRequest(stock.symbol, 1, "sell"))  // Отправляем запрос на продажу
                loadData()  // Обновляем данные после продажи
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Выход из аккаунта
    fun logout() {
        // Сбрасываем все состояния
        _isLoggedIn.value = false
        _username.value = ""
        _stocks.value = emptyList()
        _portfolio.value = emptyList()
        _balance.value = 0.0
        _stockHistory.value = emptyMap()
        api.authToken = null  // Удаляем токен авторизации
    }


    // Запуск WebSocket для получения цен в реальном времени
    private fun startWebSocket() {
        viewModelScope.launch {
            try {
                api.observeLivePrices { update ->  // Подписываемся на обновления
                    // Обновляем цену в списке акций
                    val currentStocks = _stocks.value.toMutableList()
                    val index = currentStocks.indexOfFirst { it.symbol == update.symbol }
                    if (index != -1) {
                        currentStocks[index] = currentStocks[index].copy(price = update.price)
                        _stocks.value = currentStocks
                    }

                    // Обновляем последнюю свечу в истории для графика
                    updateLastCandle(update.symbol, update.price)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Обновление последней свечи в истории (для отображения на графике)
    private fun updateLastCandle(symbol: String, newPrice: Double) {
        val currentMap = _stockHistory.value.toMutableMap()
        val currentHistory = currentMap[symbol]?.toMutableList() ?: return
        if (currentHistory.isEmpty()) return

        // Берем последнюю свечу и обновляем ее значения
        val lastCandle = currentHistory.last()
        val updatedCandle = lastCandle.copy(
            high = maxOf(lastCandle.high, newPrice),    // Максимальная цена за период
            low = minOf(lastCandle.low, newPrice),      // Минимальная цена за период
            close = newPrice,                           // Текущая цена
            volume = lastCandle.volume + 1              // Увеличиваем объем
        )

        currentHistory[currentHistory.size - 1] = updatedCandle
        currentMap[symbol] = currentHistory
        _stockHistory.value = currentMap
    }

    // Загрузка истории цен для графика (вызывается при раскрытии карточки акции)
    fun loadHistory(symbol: String) {
        viewModelScope.launch {
            try {
                val history = api.getStockHistoryMinute(symbol)  // Запрашиваем историю с сервера

                val currentMap = _stockHistory.value.toMutableMap()
                currentMap[symbol] = history
                _stockHistory.value = currentMap

                // Логируем результат для отладки
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