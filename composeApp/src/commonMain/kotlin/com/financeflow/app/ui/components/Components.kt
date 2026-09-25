package com.financeflow.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeflow.app.data.AccountType
import com.financeflow.app.data.DisplayStatus
import com.financeflow.app.ui.theme.FF
import com.financeflow.app.util.Dates
import com.financeflow.app.util.Money

// ---------- Ícones ----------

val categoryIconKeys = listOf(
    "home", "restaurant", "car", "school", "health", "movie", "subscriptions", "shopping", "receipt", "work",
    "laptop", "trending", "sell", "undo", "bolt", "wifi", "gas", "pets", "fitness", "flight", "gift", "phone",
    "coffee", "game", "clothes", "savings", "paid", "more",
)

fun categoryIcon(key: String?): ImageVector = when (key) {
    "home" -> Icons.Outlined.Home
    "restaurant" -> Icons.Outlined.Restaurant
    "car" -> Icons.Outlined.DirectionsCar
    "school" -> Icons.Outlined.School
    "health" -> Icons.Outlined.LocalHospital
    "movie" -> Icons.Outlined.Movie
    "subscriptions" -> Icons.Outlined.Subscriptions
    "shopping" -> Icons.Outlined.ShoppingCart
    "receipt" -> Icons.Outlined.Receipt
    "work" -> Icons.Outlined.Work
    "laptop" -> Icons.Outlined.Laptop
    "trending" -> Icons.AutoMirrored.Filled.TrendingUp
    "sell" -> Icons.Outlined.Sell
    "undo" -> Icons.Outlined.Undo
    "bolt" -> Icons.Outlined.Bolt
    "wifi" -> Icons.Outlined.Wifi
    "gas" -> Icons.Outlined.LocalGasStation
    "pets" -> Icons.Outlined.Pets
    "fitness" -> Icons.Outlined.FitnessCenter
    "flight" -> Icons.Outlined.Flight
    "gift" -> Icons.Outlined.CardGiftcard
    "phone" -> Icons.Outlined.Smartphone
    "coffee" -> Icons.Outlined.LocalCafe
    "game" -> Icons.Outlined.SportsEsports
    "clothes" -> Icons.Outlined.Checkroom
    "savings" -> Icons.Outlined.Savings
    "paid" -> Icons.Outlined.Paid
    "transfer" -> Icons.Outlined.SwapHoriz
    "card" -> Icons.Outlined.CreditCard
    "income" -> Icons.AutoMirrored.Filled.TrendingUp
    "expense" -> Icons.AutoMirrored.Filled.TrendingDown
    else -> Icons.Outlined.MoreHoriz
}

fun accountIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CHECKING, AccountType.DIGITAL -> Icons.Outlined.AccountBalance
    AccountType.SAVINGS -> Icons.Outlined.Savings
    AccountType.WALLET, AccountType.CASH -> Icons.Outlined.AccountBalanceWallet
    AccountType.OTHER -> Icons.Outlined.Paid
}

fun statusColor(s: DisplayStatus): Color = when (s) {
    DisplayStatus.PAID, DisplayStatus.RECEIVED, DisplayStatus.DONE -> FF.Emerald
    DisplayStatus.PENDING -> FF.Sky
    DisplayStatus.LATE -> FF.Crimson
    DisplayStatus.CANCELLED -> FF.TextTertiary
}

// ---------- Estruturas ----------

@Composable
fun FFCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    borderColor: Color = FF.Border,
    background: Color = FF.Surface2,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        if (accent != null) Box(Modifier.fillMaxWidth().height(3.dp).background(accent))
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier, icon: ImageVector? = null, filled: Boolean = false) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color else color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (filled) FF.Ink else color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = if (filled) FF.Ink else color, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
fun StatusPill(status: DisplayStatus, extra: String? = null) {
    Pill(if (extra != null) "${status.label} • $extra" else status.label, statusColor(status))
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action, color = FF.Sky, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onAction).padding(6.dp),
            )
        }
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 40.dp, bg: Color = FF.Surface1) {
    Box(
        modifier.size(size).clip(CircleShape).background(bg).border(1.dp, FF.Border, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f)) }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = FF.TextPrimary) }
        } else Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = FF.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FF.TextSecondary)
        }
        actions()
    }
    HorizontalDivider(color = FF.Border.copy(alpha = 0.6f))
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
        Text(text, color = FF.TextTertiary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun LabeledValue(label: String, value: String, color: Color = FF.TextPrimary, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium)
        Text(value, color = color, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ProgressBar(fraction: Float, color: Color = FF.Emerald, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(FF.Surface1)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(6.dp).clip(RoundedCornerShape(50)).background(color))
    }
}

// ---------- Botões ----------

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FF.Emerald, contentColor = FF.Ink,
            disabledContainerColor = FF.Surface2, disabledContentColor = FF.TextTertiary,
        ),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = FF.TextPrimary, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick, modifier = modifier.heightIn(min = 44.dp), shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (color == FF.TextPrimary) FF.Border else color),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = FF.Surface2, contentColor = color),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/** Controle segmentado (Despesa/Receita/Transferência, À vista/Parcelado/Recorrente...). */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, colors: List<Color>? = null) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FF.Surface1)
            .border(1.dp, FF.Border, RoundedCornerShape(12.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selected
            val color = colors?.getOrNull(i) ?: FF.TextPrimary
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (active) FF.Surface2 else Color.Transparent)
                    .then(if (active) Modifier.border(1.dp, if (colors != null) color.copy(alpha = 0.5f) else FF.Border, RoundedCornerShape(8.dp)) else Modifier)
                    .clickable { onSelect(i) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label, color = if (active) color else FF.TextSecondary,
                    style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ChipRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, label -> FFChip(label, i == selected) { onSelect(i) } }
    }
}

@Composable
fun FFChip(label: String, selected: Boolean, icon: ImageVector? = null, color: Color = FF.Emerald, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.15f) else FF.Surface1)
            .border(1.dp, if (selected) color else FF.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (selected) color else FF.TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = if (selected) color else FF.TextSecondary, style = MaterialTheme.typography.labelLarge)
    }
}

// ---------- Campos ----------

@Composable
fun FFTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    singleLine: Boolean = true,
    placeholder: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = FF.TextTertiary) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = FF.TextSecondary) } },
        trailingIcon = trailing,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        shape = RoundedCornerShape(8.dp),
        colors = fieldColors(),
    )
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FF.Emerald,
    unfocusedBorderColor = FF.Border,
    focusedContainerColor = FF.Surface1,
    unfocusedContainerColor = FF.Surface1,
    focusedLabelColor = FF.Emerald,
    unfocusedLabelColor = FF.TextSecondary,
    cursorColor = FF.Emerald,
    focusedTextColor = FF.TextPrimary,
    unfocusedTextColor = FF.TextPrimary,
)

/** Campo de valor com máscara de centavos: digitar 15000 exibe R$ 150,00. */
@Composable
fun MoneyField(cents: Long, onChange: (Long) -> Unit, label: String, modifier: Modifier = Modifier) {
    val text = Money.format(cents)
    OutlinedTextField(
        value = TextFieldValue(text, TextRange(text.length)),
        onValueChange = { onChange(Money.fromDigits(it.text)) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(8.dp),
        colors = fieldColors(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
    )
}

/** Valor em destaque (tela Nova Movimentação). */
@Composable
fun BigMoneyInput(cents: Long, onChange: (Long) -> Unit, color: Color) {
    val text = Money.format(cents)
    BasicTextField(
        value = TextFieldValue(text, TextRange(text.length)),
        onValueChange = { onChange(Money.fromDigits(it.text)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(color = color, fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(color),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Campo de seleção que abre um menu suspenso. */
@Composable
fun <T> SelectField(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    placeholder: String = "Selecionar",
) {
    var open by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
        Box {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(8.dp)).background(FF.Surface1)
                    .border(1.dp, if (open) FF.Emerald else FF.Border, RoundedCornerShape(8.dp))
                    .clickable { open = true }.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, tint = FF.Sky, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    selected?.let(optionLabel) ?: placeholder,
                    color = if (selected == null) FF.TextTertiary else FF.TextPrimary,
                    style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.ExpandMore, null, tint = FF.TextSecondary)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.background(FF.Surface2)) {
                options.forEach { opt ->
                    DropdownMenuItem(text = { Text(optionLabel(opt), color = FF.TextPrimary) }, onClick = { onSelect(opt); open = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, epochDay: Long?, onChange: (Long) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Selecionar data") {
    var open by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, color = FF.TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(8.dp)).background(FF.Surface1)
                .border(1.dp, FF.Border, RoundedCornerShape(8.dp)).clickable { open = true }.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = FF.TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                epochDay?.let { Dates.friendly(it) } ?: placeholder,
                color = if (epochDay == null) FF.TextTertiary else FF.TextPrimary, style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = Dates.toPickerMillis(epochDay ?: Dates.todayEpoch()),
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onChange(Dates.fromPickerMillis(it))
                    }
                    open = false
                }) { Text("OK", color = FF.Emerald) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar", color = FF.TextSecondary) } },
        ) { DatePicker(state = state) }
    }
}

@Composable
fun ToggleRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit, icon: ImageVector? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FF.Surface2)
            .border(1.dp, FF.Border, RoundedCornerShape(12.dp)).clickable { onChange(!checked) }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (checked) FF.Emerald else FF.TextSecondary)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = FF.TextPrimary, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) Text(subtitle, color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = FF.Emerald, checkedThumbColor = FF.TextPrimary,
                uncheckedTrackColor = FF.Surface1, uncheckedBorderColor = FF.Border, uncheckedThumbColor = FF.TextSecondary,
            ),
        )
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String = "Confirmar", destructive: Boolean = false, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FF.Surface2,
        title = { Text(title, color = FF.TextPrimary) },
        text = { Text(text, color = FF.TextSecondary) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirm, color = if (destructive) FF.Crimson else FF.Emerald) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = FF.TextSecondary) } },
    )
}

@Composable
fun ColorPicker(selected: Long, onSelect: (Long) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FF.palette.forEach { c ->
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(Color(c))
                    .border(if (c == selected) 3.dp else 0.dp, FF.TextPrimary, CircleShape).clickable { onSelect(c) },
            )
        }
    }
}
