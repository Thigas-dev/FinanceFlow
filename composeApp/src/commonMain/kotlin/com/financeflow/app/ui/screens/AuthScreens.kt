package com.financeflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financeflow.app.ui.MainViewModel
import com.financeflow.app.ui.components.FFCard
import com.financeflow.app.ui.components.FFTextField
import com.financeflow.app.ui.components.PrimaryButton
import com.financeflow.app.ui.components.ScreenHeader
import com.financeflow.app.ui.theme.FF

private val emailRegex = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

private fun validEmail(e: String) = emailRegex.matches(e.trim())

@Composable
private fun Logo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(FF.Emerald),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = FF.Ink) }
        Spacer(Modifier.size(12.dp))
        Text("FinanceFlow", style = MaterialTheme.typography.headlineLarge, color = FF.EmeraldLight)
    }
}

@Composable
fun LoginScreen(vm: MainViewModel, onLogged: () -> Unit, onRegister: () -> Unit, onRecover: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Logo()
        Text(
            "Quanto tenho hoje? Quanto vou receber? Quanto tenho para pagar?",
            color = FF.TextSecondary, style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
        FFCard(background = FF.Surface1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Entrar", style = MaterialTheme.typography.headlineSmall, color = FF.TextPrimary)
                FFTextField(email, { email = it; error = null }, "E-mail", leadingIcon = Icons.Outlined.Email, keyboardType = KeyboardType.Email)
                FFTextField(password, { password = it; error = null }, "Senha", leadingIcon = Icons.Outlined.Lock, password = true)
                error?.let { Text(it, color = FF.Crimson, style = MaterialTheme.typography.bodySmall) }
                PrimaryButton(
                    if (loading) "Entrando..." else "Entrar",
                    onClick = {
                        if (!validEmail(email) || password.isEmpty()) {
                            error = "Informe e-mail e senha válidos"
                        } else {
                            loading = true
                            vm.login(email, password) { ok ->
                                loading = false
                                if (ok) onLogged() else error = "E-mail ou senha incorretos"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(), enabled = !loading,
                )
                TextButton(onClick = onRecover, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Esqueci minha senha", color = FF.Sky)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Ainda não tem conta?", color = FF.TextSecondary)
            TextButton(onClick = onRegister) { Text("Criar conta", color = FF.EmeraldLight) }
        }
    }
}

@Composable
fun RegisterScreen(vm: MainViewModel, onDone: () -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var recovery by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Criar conta", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FFTextField(name, { name = it }, "Nome", leadingIcon = Icons.Outlined.Person)
            FFTextField(email, { email = it }, "E-mail", leadingIcon = Icons.Outlined.Email, keyboardType = KeyboardType.Email)
            FFTextField(password, { password = it }, "Senha (mín. 6 caracteres)", leadingIcon = Icons.Outlined.Lock, password = true)
            FFTextField(confirm, { confirm = it }, "Confirmar senha", leadingIcon = Icons.Outlined.Lock, password = true)
            FFTextField(recovery, { recovery = it }, "Palavra de recuperação", leadingIcon = Icons.Outlined.Key)
            Text(
                "A palavra de recuperação permite redefinir sua senha. Seus dados ficam armazenados apenas neste aparelho, com a senha protegida por hash (PBKDF2).",
                color = FF.TextSecondary, style = MaterialTheme.typography.bodySmall,
            )
            error?.let { Text(it, color = FF.Crimson, style = MaterialTheme.typography.bodySmall) }
            PrimaryButton(
                if (loading) "Criando..." else "Criar conta",
                onClick = {
                    error = when {
                        name.isBlank() -> "Informe seu nome"
                        !validEmail(email) -> "E-mail inválido"
                        password.length < 6 -> "A senha deve ter ao menos 6 caracteres"
                        password != confirm -> "As senhas não conferem"
                        recovery.isBlank() -> "Informe uma palavra de recuperação"
                        else -> null
                    }
                    if (error == null) {
                        loading = true
                        vm.register(name, email, password, recovery) { err ->
                            loading = false
                            if (err == null) onDone() else error = err
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(), enabled = !loading,
            )
        }
    }
}

@Composable
fun RecoverScreen(vm: MainViewModel, onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var recovery by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Recuperar senha", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Informe seu e-mail e a palavra de recuperação cadastrada para definir uma nova senha.", color = FF.TextSecondary)
            FFTextField(email, { email = it }, "E-mail", leadingIcon = Icons.Outlined.Email, keyboardType = KeyboardType.Email)
            FFTextField(recovery, { recovery = it }, "Palavra de recuperação", leadingIcon = Icons.Outlined.Key)
            FFTextField(password, { password = it }, "Nova senha", leadingIcon = Icons.Outlined.Lock, password = true)
            message?.let { Text(it.second, color = if (it.first) FF.EmeraldLight else FF.Crimson) }
            PrimaryButton("Redefinir senha", onClick = {
                if (password.length < 6) {
                    message = false to "A nova senha deve ter ao menos 6 caracteres"
                } else {
                    vm.resetPassword(email, recovery, password) { ok ->
                        message = if (ok) true to "Senha redefinida! Volte e faça login." else false to "Dados de recuperação inválidos"
                    }
                }
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}
