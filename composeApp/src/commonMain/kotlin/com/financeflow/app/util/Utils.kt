package com.financeflow.app.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.io.encoding.Base64
import kotlin.math.abs
import kotlin.uuid.Uuid

fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

object Money {
    /** Formata centavos como "R$ 1.234,56" (negativos como "-R$ 45,90"). */
    fun format(cents: Long): String {
        val a = abs(cents)
        val reais = (a / 100).toString().reversed().chunked(3).joinToString(".").reversed()
        return (if (cents < 0) "-" else "") + "R$ " + reais + "," + (a % 100).toString().padStart(2, '0')
    }

    fun signed(cents: Long, positive: Boolean): String = (if (positive) "+ " else "- ") + format(abs(cents))

    /** "150,00" a partir de dígitos digitados (máscara de centavos). */
    fun fromDigits(digits: String): Long = digits.filter { it.isDigit() }.take(13).toLongOrNull() ?: 0L
}

/** Mês/ano (equivalente ao java.time.YearMonth, que não existe no Kotlin Multiplatform). */
data class YearMonth(val year: Int, val month: Int) {
    fun plusMonths(n: Long): YearMonth {
        val total = year * 12L + (month - 1) + n
        return YearMonth(Math.floorDiv(total, 12L).toInt(), Math.floorMod(total, 12L).toInt() + 1)
    }

    fun minusMonths(n: Long) = plusMonths(-n)
    fun atDay(day: Int) = LocalDate(year, month, day)
    fun lengthOfMonth(): Int = (plusMonths(1).atDay(1).toEpochDays() - atDay(1).toEpochDays())
    fun atEndOfMonth() = atDay(lengthOfMonth())

    companion object {
        fun now() = from(Dates.today())
        fun from(d: LocalDate) = YearMonth(d.year, d.monthNumber)
    }
}

private object Math {
    fun floorDiv(a: Long, b: Long): Long = (a / b).let { if ((a % b != 0L) && ((a < 0) != (b < 0))) it - 1 else it }
    fun floorMod(a: Long, b: Long): Long = a - floorDiv(a, b) * b
}

fun LocalDate.epochDay(): Long = toEpochDays().toLong()

object Dates {
    private val months = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
    )

    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    fun todayEpoch(): Long = today().epochDay()
    fun of(epochDay: Long): LocalDate = LocalDate.fromEpochDays(epochDay.toInt())

    private fun two(n: Int) = n.toString().padStart(2, '0')
    fun short(epochDay: Long): String = of(epochDay).let { "${two(it.dayOfMonth)}/${two(it.monthNumber)}" }
    fun full(epochDay: Long): String = of(epochDay).let { "${two(it.dayOfMonth)}/${two(it.monthNumber)}/${it.year}" }

    fun monthName(ym: YearMonth): String = months[ym.month - 1]
    fun monthYear(ym: YearMonth): String = "${monthName(ym)} ${ym.year}"
    fun monthShort(ym: YearMonth): String = months[ym.month - 1].take(3)

    /** "25 SET" */
    fun dayMonthUpper(epochDay: Long): String = of(epochDay).let { "${two(it.dayOfMonth)} ${months[it.monthNumber - 1].take(3).uppercase()}" }

    fun dateTime(millis: Long): String {
        val t = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${two(t.dayOfMonth)}/${two(t.monthNumber)} ${two(t.hour)}:${two(t.minute)}"
    }

    fun friendly(epochDay: Long): String = when (epochDay - todayEpoch()) {
        0L -> "Hoje, ${full(epochDay)}"
        -1L -> "Ontem, ${full(epochDay)}"
        1L -> "Amanhã, ${full(epochDay)}"
        else -> full(epochDay)
    }

    fun daysUntil(epochDay: Long): Long = epochDay - todayEpoch()

    fun dueText(epochDay: Long): String {
        val d = daysUntil(epochDay)
        return when {
            d < -1 -> "Atrasada há ${-d} dias"
            d == -1L -> "Atrasada desde ontem"
            d == 0L -> "Vence hoje"
            d == 1L -> "Vence amanhã"
            d <= 7 -> "Vence em $d dias"
            else -> "Vence ${short(epochDay)}"
        }
    }

    /** Soma meses preservando o dia (ajustado ao fim do mês quando necessário). */
    fun plusMonths(epochDay: Long, months: Long, preferredDay: Int? = null): Long {
        val start = of(epochDay)
        val ym = YearMonth.from(start).plusMonths(months)
        val day = minOf(preferredDay ?: start.dayOfMonth, ym.lengthOfMonth())
        return ym.atDay(day).epochDay()
    }

    fun plusYears(epochDay: Long, years: Int): Long = of(epochDay).plus(years, DateTimeUnit.YEAR).epochDay()

    /** Conversões para o DatePicker do Material 3 (milissegundos UTC à meia-noite). */
    fun toPickerMillis(epochDay: Long): Long = epochDay * 86_400_000L
    fun fromPickerMillis(millis: Long): Long = Math.floorDiv(millis, 86_400_000L)
}

/** PBKDF2-HMAC-SHA256 em Kotlin puro (roda igual no Android e no iOS). */
object Passwords {
    private const val ITERATIONS = 60_000

    fun newSalt(): String = Base64.encode(Uuid.random().toByteArray())

    fun hash(secret: String, salt: String): String =
        Base64.encode(Crypto.pbkdf2Sha256(secret.encodeToByteArray(), Base64.decode(salt), ITERATIONS, 32))

    fun verify(secret: String, salt: String, expected: String): Boolean {
        val a = hash(secret, salt).encodeToByteArray()
        val b = expected.encodeToByteArray()
        if (a.size != b.size) return false
        var r = 0
        for (i in a.indices) r = r or (a[i].toInt() xor b[i].toInt())
        return r == 0
    }
}

object Crypto {
    private val K = intArrayOf(
        1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075,
        -670586216, 310598401, 607225278, 1426881987, 1925078388, -2132889090, -1680079193, -1046744716,
        -459576895, -272742522, 264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986,
        -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585, 113926993, 338241895,
        666307205, 773529912, 1294757372, 1396182291, 1695183700, 1986661051, -2117940946, -1838011259,
        -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344,
        430227734, 506948616, 659060556, 883997877, 958139571, 1322822218, 1537002063, 1747873779,
        1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998,
    )

    fun sha256(data: ByteArray): ByteArray {
        val h = intArrayOf(1779033703, -1150833019, 1013904242, -1521486534, 1359893119, -1694144372, 528734635, 1541459225)
        val bitLen = data.size.toLong() * 8
        val padLen = ((data.size + 9 + 63) / 64) * 64
        val msg = ByteArray(padLen)
        data.copyInto(msg)
        msg[data.size] = 0x80.toByte()
        for (i in 0 until 8) msg[padLen - 1 - i] = (bitLen ushr (8 * i)).toByte()
        val w = IntArray(64)
        for (chunk in 0 until padLen / 64) {
            for (i in 0 until 16) {
                val o = chunk * 64 + i * 4
                w[i] = (msg[o].toInt() and 0xff shl 24) or (msg[o + 1].toInt() and 0xff shl 16) or
                    (msg[o + 2].toInt() and 0xff shl 8) or (msg[o + 3].toInt() and 0xff)
            }
            for (i in 16 until 64) {
                val s0 = w[i - 15].rotateRight(7) xor w[i - 15].rotateRight(18) xor (w[i - 15] ushr 3)
                val s1 = w[i - 2].rotateRight(17) xor w[i - 2].rotateRight(19) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }
            var a = h[0]; var b = h[1]; var c = h[2]; var d = h[3]
            var e = h[4]; var f = h[5]; var g = h[6]; var hh = h[7]
            for (i in 0 until 64) {
                val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
                val ch = (e and f) xor (e.inv() and g)
                val t1 = hh + s1 + ch + K[i] + w[i]
                val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val t2 = s0 + maj
                hh = g; g = f; f = e; e = d + t1; d = c; c = b; b = a; a = t1 + t2
            }
            h[0] += a; h[1] += b; h[2] += c; h[3] += d; h[4] += e; h[5] += f; h[6] += g; h[7] += hh
        }
        val out = ByteArray(32)
        for (i in 0 until 8) for (j in 0 until 4) out[i * 4 + j] = (h[i] ushr (24 - 8 * j)).toByte()
        return out
    }

    fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
        val k = (if (key.size > 64) sha256(key) else key).copyOf(64)
        val ipad = ByteArray(64) { (k[it].toInt() xor 0x36).toByte() }
        val opad = ByteArray(64) { (k[it].toInt() xor 0x5c).toByte() }
        return sha256(opad + sha256(ipad + message))
    }

    fun pbkdf2Sha256(password: ByteArray, salt: ByteArray, iterations: Int, length: Int): ByteArray {
        val out = ByteArray(length)
        var block = 1
        var offset = 0
        while (offset < length) {
            var u = hmacSha256(password, salt + byteArrayOf((block ushr 24).toByte(), (block ushr 16).toByte(), (block ushr 8).toByte(), block.toByte()))
            val t = u.copyOf()
            repeat(iterations - 1) {
                u = hmacSha256(password, u)
                for (i in t.indices) t[i] = (t[i].toInt() xor u[i].toInt()).toByte()
            }
            val n = minOf(32, length - offset)
            t.copyInto(out, offset, 0, n)
            offset += n
            block++
        }
        return out
    }
}
