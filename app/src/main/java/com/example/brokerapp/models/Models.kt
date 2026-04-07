package com.example.brokerapp.models

data class Stock(
    val symbol: String,
    val name: String,
    var price: Double,
    var changePercent: Double
)

data class PortfolioItem(
    val stock: Stock,
    var quantity: Int
)