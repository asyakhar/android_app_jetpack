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
import kotlinx.coroutines.delay
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.random.Random

class ApiClient {
    var isMockMode = false  // true = демо-режим, false = реальный сервер

    private val baseUrl = "http://100.69.47.75:9090"
    var authToken: String? = null

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(WebSockets) {
            pingInterval = 20_000
        }
    }

    private fun HttpRequestBuilder.authHeader() {
        authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    // ========== MOCK-ДАННЫЕ ==========

    private fun getMockStocks(): List<Stock> {
        return listOf(
            Stock("AAPL", "Apple Inc.", 175.50 + Random.nextDouble(-5.0, 5.0), Random.nextDouble(-3.0, 3.0)),
            Stock("GOOGL", "Alphabet Inc.", 138.20 + Random.nextDouble(-4.0, 4.0), Random.nextDouble(-3.0, 3.0)),
            Stock("MSFT", "Microsoft Corp.", 330.80 + Random.nextDouble(-8.0, 8.0), Random.nextDouble(-3.0, 3.0)),
            Stock("AMZN", "Amazon.com Inc.", 145.90 + Random.nextDouble(-5.0, 5.0), Random.nextDouble(-3.0, 3.0)),
            Stock("TSLA", "Tesla Inc.", 240.50 + Random.nextDouble(-15.0, 15.0), Random.nextDouble(-5.0, 5.0)),
            Stock("META", "Meta Platforms", 310.20 + Random.nextDouble(-7.0, 7.0), Random.nextDouble(-3.0, 3.0)),
            Stock("NVDA", "NVIDIA Corp.", 450.30 + Random.nextDouble(-20.0, 20.0), Random.nextDouble(-4.0, 4.0)),
            Stock("JPM", "JPMorgan Chase", 140.60 + Random.nextDouble(-3.0, 3.0), Random.nextDouble(-2.0, 2.0)),
            Stock("V", "Visa Inc.", 245.40 + Random.nextDouble(-4.0, 4.0), Random.nextDouble(-2.0, 2.0)),
            Stock("WMT", "Walmart Inc.", 160.80 + Random.nextDouble(-2.0, 2.0), Random.nextDouble(-1.5, 1.5))
        )
    }

    private fun getMockPortfolio(): List<PortfolioItem> {
        val stocks = getMockStocks()
        return listOf(
            PortfolioItem(stocks[0], 10, 170.0, 1755.0, 55.0, 3.2),
            PortfolioItem(stocks[2], 5, 320.0, 1654.0, 54.0, 3.4),
            PortfolioItem(stocks[4], 2, 230.0, 481.0, 21.0, 4.6)
        )
    }

    private fun getMockBalance(): Double {
        return 10000.0 + Random.nextDouble(-500.0, 500.0)
    }

    private fun getMockHistory(symbol: String): List<PriceHistoryCandle> {
        val history = mutableListOf<PriceHistoryCandle>()
        val now = System.currentTimeMillis()
        var price = 100.0

        for (i in 0 until 100) {
            val change = Random.nextDouble(-3.0, 3.0)
            val open = price
            val close = price + change
            val high = maxOf(open, close) + Random.nextDouble(0.0, 2.0)
            val low = minOf(open, close) - Random.nextDouble(0.0, 2.0)

            history.add(
                PriceHistoryCandle(
                    timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                        .format(java.util.Date(now - (100 - i) * 60000)),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = Random.nextLong(10000, 1000000)
                )
            )
            price = close
        }
        return history
    }

    // ========== РЕАЛЬНЫЕ МЕТОДЫ С MOCK-РЕЖИМОМ ==========

    suspend fun login(request: LoginRequest): LoginResponse {
        if (isMockMode) {
            delay(1000) // Имитация задержки сети
            authToken = "mock-token-12345"
            return LoginResponse(
                token = "mock-token-12345",
                tokenType = "Bearer",
                expiresIn = 3600
            )
        }

        val response = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val loginResponse: LoginResponse = response.body()
        authToken = loginResponse.token
        return loginResponse
    }

    suspend fun getStocks(): List<Stock> {
        if (isMockMode) {
            delay(500)
            return getMockStocks()
        }

        val response: PaginatedStocksResponse = client.get("$baseUrl/market/stocks?limit=50") {
            authHeader()
        }.body()
        return response.data
    }

    suspend fun getPortfolio(): List<PortfolioItem> {
        if (isMockMode) {
            delay(500)
            return getMockPortfolio()
        }

        return client.get("$baseUrl/portfolio") {
            authHeader()
        }.body()
    }

    suspend fun getBalance(): Double {
        if (isMockMode) {
            delay(500)
            return getMockBalance()
        }

        val account: AccountResponse = client.get("$baseUrl/account") {
            authHeader()
        }.body()
        return account.balance
    }

    suspend fun executeTrade(request: TradeRequest) {
        if (isMockMode) {
            delay(800)
            println("✅ [MOCK] Торговля: ${request.tradeType} ${request.quantity} ${request.symbol}")
            return
        }

        client.post("$baseUrl/trades") {
            contentType(ContentType.Application.Json)
            authHeader()
            setBody(request)
        }
    }

    suspend fun observeLivePrices(onPriceUpdate: (PriceUpdate) -> Unit) {
        if (isMockMode) {
            // Имитация WebSocket: обновляем цены каждые 3 секунды
            while (true) {
                delay(3000)
                val stocks = getMockStocks()
                stocks.forEach { stock ->
                    val newPrice = stock.price + Random.nextDouble(-2.0, 2.0)
                    onPriceUpdate(
                        PriceUpdate(
                            symbol = stock.symbol,
                            price = newPrice,
                            change = newPrice - stock.price,
                            changePercent = ((newPrice - stock.price) / stock.price) * 100,
                            timestamp = System.currentTimeMillis().toString()
                        )
                    )
                }
            }
        }

        val token = authToken ?: return
        client.webSocket(method = HttpMethod.Get, host = "10.0.2.2", port = 9090, path = "/ws?token=$token") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val jsonParser = Json { ignoreUnknownKeys = true }
                        val message = jsonParser.decodeFromString<WsMessage>(text)
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

    suspend fun getStockHistoryMinute(symbol: String): List<PriceHistoryCandle> {
        if (isMockMode) {
            delay(300)
            return getMockHistory(symbol)
        }

        val response: PriceHistoryResponse = client.get("$baseUrl/market/stocks/$symbol/history") {
            authHeader()
            parameter("interval", "1m")
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun register(username: String, email: String, password: String): LoginResponse {
        if (isMockMode) {
            delay(1000)
            authToken = "mock-token-${username}"
            return LoginResponse(
                token = "mock-token-${username}",
                tokenType = "Bearer",
                expiresIn = 3600
            )
        }

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
}