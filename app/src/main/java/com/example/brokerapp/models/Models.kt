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
    val price: Double,
    val change: Double? = null,        // опционально
    val changePercent: Double? = null, // опционально
    val volume: Long? = null,          // опционально
    val timestamp: String? = null      // опционально
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
enum class ChartInterval(val value: String, val label: String, val minutes: Long) {
    M15("15m", "15 мин", 15),
    M30("30m", "30 мин", 30),
    H1("1h", "1 час", 60),
    H4("4h", "4 часа", 240),
    D1("1d", "1 день", 1440),
    W1("1w", "1 неделя", 10080);

    companion object {
        fun fromValue(value: String): ChartInterval {
            return entries.find { it.value == value } ?: D1
        }
    }
}

enum class ChartTimeframe(val label: String, val durationMillis: Long, val interval: String) {
    MINUTE_1("1м", 60 * 60 * 1000L, "1m");  // только 1 минута

    // Временно закомментируем остальные
    // HOUR_1("1Ч", 60 * 60 * 1000L, "5m"),
    // DAY_1("1Д", 24 * 60 * 60 * 1000L, "15m"),
    // WEEK_1("1Н", 7 * 24 * 60 * 60 * 1000L, "1h"),
    // MONTH_1("1М", 30L * 24 * 60 * 60 * 1000L, "1d")
}

@Serializable
data class PriceHistoryResponse(
    val symbol: String? = null,
    val interval: String? = null,
    val data: List<PriceHistoryCandle>? = emptyList()
)