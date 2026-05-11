package com.example.brokerapp.network

import com.example.brokerapp.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.decodeFromJsonElement

class ApiClient {
    // В эмуляторе Android IP 10.0.2.2 перенаправляет на localhost компьютера
    private val baseUrl = "http://100.69.47.75:9090"
    var authToken: String? = null

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        // ДОБАВЛЯЕМ ПЛАГИН:
        install(WebSockets) {
            pingInterval = 20_000 // Поддерживаем соединение живым
        }
    }

    // Утилита для подстановки токена во все запросы
    private fun HttpRequestBuilder.authHeader() {
        authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val loginResponse: LoginResponse = response.body()
        authToken = loginResponse.token
        return loginResponse
    }

    suspend fun getStocks(): List<Stock> {
        val response: PaginatedStocksResponse = client.get("$baseUrl/market/stocks?limit=50") {
            authHeader()
        }.body()
        return response.data
    }

    suspend fun getPortfolio(): List<PortfolioItem> {
        return client.get("$baseUrl/portfolio") {
            authHeader()
        }.body()
    }

    suspend fun getBalance(): Double {
        val account: AccountResponse = client.get("$baseUrl/account") {
            authHeader()
        }.body()
        return account.balance
    }

    suspend fun executeTrade(request: TradeRequest) {
        client.post("$baseUrl/trades") {
            contentType(ContentType.Application.Json)
            authHeader()
            setBody(request)
        }
    }
    suspend fun observeLivePrices(onPriceUpdate: (PriceUpdate) -> Unit) {
        val token = authToken ?: return

        // Подключаемся к эндпоинту, который мы видели в твоем mobile-handler
        client.webSocket(method = HttpMethod.Get, host = "10.0.2.2", port = 9090, path = "/ws?token=$token") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val jsonParser = Json { ignoreUnknownKeys = true }
                        val message = jsonParser.decodeFromString<WsMessage>(text)

                        // Ловим обновления цен
                        if (message.type == "price_update") {
                            val priceData = jsonParser.decodeFromJsonElement<PriceUpdate>(message.data)
                            onPriceUpdate(priceData)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    suspend fun getStockHistory(
        symbol: String,
        from: Long,
        to: Long,
        interval: String
    ): List<PriceHistoryCandle> {
        // Конвертируем Long timestamp в ISO 8601 (ожидает бэкенд)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val fromIso = dateFormat.format(java.util.Date(from))
        val toIso = dateFormat.format(java.util.Date(to))

        println("📊 Запрос истории: symbol=$symbol, from=$fromIso, to=$toIso, interval=$interval")

        val response: PriceHistoryResponse = client.get("$baseUrl/market/stocks/$symbol/history") {
            authHeader()
            parameter("from", fromIso)
            parameter("to", toIso)
            parameter("interval", interval)
        }.body()

        return response.data ?: emptyList()
    }
    suspend fun getStockHistorySimple(
        symbol: String,
        interval: String = "1m"
    ): List<PriceHistoryCandle> {
        val response: PriceHistoryResponse = client.get("$baseUrl/market/stocks/$symbol/history") {
            authHeader()
            parameter("interval", interval)
        }.body()
        return response.data ?: emptyList()
    }
    suspend fun register(username: String, email: String, password: String): LoginResponse {
        val response = client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "username" to username,
                "email" to email,
                "password" to password
            ))
        }
        val loginResponse: LoginResponse = response.body()
        authToken = loginResponse.token
        return loginResponse
    }
    suspend fun getStockHistoryMinute(symbol: String): List<PriceHistoryCandle> {
        println("📡 [REQUEST] GET $baseUrl/market/stocks/$symbol/history?interval=1m")

        val response: PriceHistoryResponse = client.get("$baseUrl/market/stocks/$symbol/history") {
            authHeader()
            parameter("interval", "1m")  // жёстко 1 минута
        }.body()

        val size = response.data?.size ?: 0
        println("📡 [RESPONSE] Получено $size свечей для $symbol")

        return response.data ?: emptyList()
    }
}