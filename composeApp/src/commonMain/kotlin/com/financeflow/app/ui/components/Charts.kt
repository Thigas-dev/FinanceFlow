package com.financeflow.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Money

data class BarGroup(val label: String, val income: Long, val expense: Long)

/** Receitas x Despesas por mês. */
@Composable
fun IncomeExpenseChart(groups: List<BarGroup>, modifier: Modifier = Modifier) {
    val max = (groups.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val n = groups.size.coerceAtLeast(1)
            val slot = size.width / n
            val barW = (slot * 0.28f).coerceAtMost(18.dp.toPx())
            val gap = 4.dp.toPx()
            for (i in 1..3) {
                val y = size.height * i / 4f
                drawLine(FF.Border.copy(alpha = 0.4f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            groups.forEachIndexed { i, g ->
                val cx = slot * i + slot / 2
                val hi = size.height * (g.income.toFloat() / max)
                val he = size.height * (g.expense.toFloat() / max)
                drawRoundRect(FF.Emerald, Offset(cx - barW - gap / 2, size.height - hi), Size(barW, hi), CornerRadius(4.dp.toPx()))
                drawRoundRect(FF.Crimson, Offset(cx + gap / 2, size.height - he), Size(barW, he), CornerRadius(4.dp.toPx()))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            groups.forEach {
                Text(it.label, Modifier.weight(1f), color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Legend("Receitas", FF.Emerald)
            Legend("Despesas", FF.Crimson)
        }
    }
}

@Composable
fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}

/** Distribuição por categoria. */
@Composable
fun DonutChart(values: List<Pair<String, Long>>, modifier: Modifier = Modifier) {
    val total = values.sumOf { it.second }.coerceAtLeast(1L)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(130.dp)) {
            val stroke = 22.dp.toPx()
            var start = -90f
            if (values.isEmpty()) {
                drawArc(FF.Surface1, 0f, 360f, false, style = Stroke(stroke), topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke))
            }
            values.forEachIndexed { i, (_, v) ->
                val sweep = 360f * v / total
                drawArc(
                    Color(FF.palette[i % FF.palette.size]), start, (sweep - 1.5f).coerceAtLeast(0.5f), false,
                    style = Stroke(stroke, cap = StrokeCap.Butt),
                    topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke),
                )
                start += sweep
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            values.take(6).forEachIndexed { i, (label, v) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(FF.palette[i % FF.palette.size])))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "$label ${(v * 100 / total)}%", color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Linha de evolução do saldo projetado. */
@Composable
fun BalanceLineChart(points: List<Long>, modifier: Modifier = Modifier) {
    if (points.size < 2) return
    val min = points.min()
    val max = points.max()
    val range = (max - min).coerceAtLeast(1L)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(120.dp).padding(vertical = 6.dp)) {
            val stepX = size.width / (points.size - 1)
            fun y(v: Long) = size.height - size.height * ((v - min).toFloat() / range)
            if (min < 0 && max > 0) {
                val zero = y(0)
                drawLine(FF.Crimson.copy(alpha = 0.5f), Offset(0f, zero), Offset(size.width, zero), strokeWidth = 1.dp.toPx())
            }
            val path = Path()
            points.forEachIndexed { i, v ->
                val p = Offset(stepX * i, y(v))
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, FF.Sky, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
        Row(Modifier.fillMaxWidth()) {
            Text(Money.format(min), color = FF.TextTertiary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text(Money.format(max), color = FF.TextTertiary, style = MaterialTheme.typography.labelSmall)
        }
    }
}
