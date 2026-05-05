package com.example.brokerapp.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val tokenType: String = "Bearer", val expiresIn: Int)

@Serializable
data class Stock(
    val symbol: String,
    val name: String,
    val price: Double, // Изменили на val
    val changePercent: Double // Изменили на val
)

@Serializable
data class PaginatedStocksResponse(val data: List<Stock>)

@Serializable
data class PortfolioItem(
    val stock: Stock,
    val quantity: Int, // Изменили на val
    val averageBuyPrice: Double = 0.0,
    val totalValue: Double = 0.0,
    val profitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0
)

@Serializable
data class AccountResponse(val balance: Double, val availableBalance: Double? = null)

@Serializable
data class TradeRequest(val symbol: String, val quantity: Int, val tradeType: String, val orderType: String = "market")

// --- НОВЫЕ КЛАССЫ ДЛЯ WEBSOCKET ---
@Serializable
data class WsMessage(
    val type: String,
    val data: JsonElement, // Оставляем сырой JSON, чтобы парсить в зависимости от type
    val timestamp: String
)

@Serializable
data class PriceUpdate(
    val symbol: String,
    val price: Double
)

@Serializable
data class PriceHistoryCandle(
    val timestamp: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

@Serializable
data class PriceHistoryResponse(
    val symbol: String,
    val interval: String,
    val data: List<PriceHistoryCandle>
)