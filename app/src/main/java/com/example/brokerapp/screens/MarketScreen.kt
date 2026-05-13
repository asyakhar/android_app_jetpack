package com.example.brokerapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brokerapp.models.PortfolioItem
import com.example.brokerapp.models.PriceHistoryCandle
import com.example.brokerapp.models.Stock
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
fun MarketScreen(
    stocks: List<Stock>,
    balance: Double,
    portfolio: List<PortfolioItem>,
    stockHistory: Map<String, List<PriceHistoryCandle>>,
    onLoadHistory: (String) -> Unit,
    onBuyClick: (Stock) -> Unit,
    onSellClick: (Stock) -> Unit
) {
    val portfolioValue = portfolio.sumOf { it.stock.price * it.quantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Общий портфель",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        NumberFormat.getCurrencyInstance(Locale.US).format(balance + portfolioValue),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatChip(
                            label = "Доступно",
                            value = NumberFormat.getCurrencyInstance(Locale.US).format(balance)
                        )
                        StatChip(
                            label = "Инвестиции",
                            value = NumberFormat.getCurrencyInstance(Locale.US).format(portfolioValue)
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Акции",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        items(stocks) { stock ->
            ModernStockCard(
                stock = stock,
                history = stockHistory[stock.symbol],
                onLoadHistory = { onLoadHistory(stock.symbol) },
                onBuyClick = { onBuyClick(stock) },
                onSellClick = { onSellClick(stock) }
            )
        }
    }
}

@Composable
fun StatChip(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ModernStockCard(
    stock: Stock,
    history: List<PriceHistoryCandle>?,
    onLoadHistory: () -> Unit,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("1ч") }
    val changeColor = if (stock.changePercent >= 0) Color(0xFF00C853) else Color(0xFFD32F2F)

    fun getFilteredHistory(): List<PriceHistoryCandle>? {
        val fullHistory = history ?: return null
        return when (selectedRange) {
            "1ч" -> fullHistory.takeLast(40)
            "6ч" -> fullHistory.takeLast(100)
            "1д" -> fullHistory
            else -> fullHistory.takeLast(60)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
                if (expanded) onLoadHistory()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stock.symbol.take(1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stock.symbol, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(stock.name, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
                            .format(stock.price), fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            if (stock.changePercent >= 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = changeColor
                        )
                        Text(
                            String.format("%+.2f%%", stock.changePercent),
                            color = changeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = expanded,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScaleButton(label = "1ч", isSelected = selectedRange == "1ч") {
                            selectedRange = "1ч"
                        }
                        ScaleButton(label = "6ч", isSelected = selectedRange == "6ч") {
                            selectedRange = "6ч"
                        }
                        ScaleButton(label = "1д", isSelected = selectedRange == "1д") {
                            selectedRange = "1д"
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // График
                    val displayHistory = getFilteredHistory()
                    if (displayHistory != null && displayHistory.isNotEmpty()) {
                        CandlestickChart(
                            history = displayHistory,
                            modifier = Modifier.fillMaxWidth().height(160.dp).padding(vertical = 8.dp)
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onBuyClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Купить", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onSellClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD32F2F))
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Продать", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScaleButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(80.dp)
            .height(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable
fun CandlestickChart(history: List<PriceHistoryCandle>, modifier: Modifier = Modifier) {
    if (history.isEmpty()) return

    val maxPrice = history.maxOf { it.high }.toFloat()
    val minPrice = history.minOf { it.low }.toFloat()
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.001f)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val candleWidth = canvasWidth / history.size.coerceAtLeast(1).toFloat()
        val bodyWidth = candleWidth * 0.6f

        history.forEachIndexed { index, candle ->
            val xCenter = index * candleWidth + candleWidth / 2f

            val highY = canvasHeight - ((candle.high.toFloat() - minPrice) / priceRange) * canvasHeight
            val lowY = canvasHeight - ((candle.low.toFloat() - minPrice) / priceRange) * canvasHeight
            val openY = canvasHeight - ((candle.open.toFloat() - minPrice) / priceRange) * canvasHeight
            val closeY = canvasHeight - ((candle.close.toFloat() - minPrice) / priceRange) * canvasHeight

            val isBullish = candle.close >= candle.open
            val candleColor = if (isBullish) Color(0xFF00C853) else Color(0xFFD32F2F)

            drawLine(
                color = candleColor,
                start = Offset(xCenter, highY),
                end = Offset(xCenter, lowY),
                strokeWidth = 2.dp.toPx()
            )

            val bodyTop = minOf(openY, closeY)
            val bodyBottom = maxOf(openY, closeY)
            val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(1f)

            drawRect(
                color = candleColor,
                topLeft = Offset(xCenter - bodyWidth / 2f, bodyTop),
                size = Size(bodyWidth, bodyHeight)
            )
        }
    }
}