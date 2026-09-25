package com.financeflow.app.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.semantics.getOrNull
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.financeflow.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Percorre o fluxo de sucesso do MVP (seção 39 do PRD) na UI real. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w411dp-h1400dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppSmokeTest {
    @get:Rule(order = 0)
    val permission: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun field(label: String) = rule.onNode(hasSetTextAction() and hasText(label, substring = true))

    private fun click(text: String) =
        rule.onNode(hasText(text) and hasClickAction() and !hasSetTextAction()).performClick()

    private fun waitText(text: String) {
        try {
            rule.waitUntil(10_000) { rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
        } catch (e: Throwable) {
            val texts = rule.onRoot().fetchSemanticsNode().let { root ->
                fun walk(n: androidx.compose.ui.semantics.SemanticsNode): List<String> =
                    (n.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.map { it.text } ?: emptyList()) +
                        n.children.flatMap { walk(it) }
                walk(root)
            }
            throw AssertionError("'$text' não encontrado. Tela: $texts", e)
        }
    }

    private fun back() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun fluxoPrincipal() {
        // CA01: cadastro e acesso
        click("Criar conta")
        waitText("Palavra de recuperação")
        field("Nome").performTextInput("Thiago Silva")
        field("E-mail").performTextInput("thiago@teste.com")
        field("Senha (mín").performTextInput("segredo123")
        field("Confirmar senha").performTextInput("segredo123")
        field("Palavra de recuperação").performTextInput("azul")
        click("Criar conta")
        waitText("Saldo Atual")
        rule.onNodeWithText("Olá, Thiago").assertExists()

        // CA04: despesa à vista paga, debita da Carteira
        click("Novo")
        waitText("Nova Movimentação")
        rule.onNode(hasSetTextAction() and hasText("R$ 0,00")).performTextInput("4590")
        field("Descrição").performTextInput("Almoço")
        click("Salvar Movimentação")
        waitText("Saldo Atual")
        waitText("- R$ 45,90")

        // CA05/CA06: parcelado 1.200,00 em 2x (padrão), 1ª pendente
        click("Novo")
        waitText("Nova Movimentação")
        rule.onNode(hasSetTextAction() and hasText("R$ 0,00")).performTextInput("120000")
        click("Parcelado")
        field("Descrição").performTextInput("Notebook")
        click("1ª parcela já paga")
        click("Salvar Movimentação")
        waitText("Saldo Atual")


        click("Parcelas")
        waitText("Notebook")
        rule.onNodeWithText("2x de R$ 600,00").assertExists()
        rule.onNodeWithText("0/2 pagas (0%)").assertExists()

        // CA07/CA08: pagar parcela 1
        click("Pagar 1")
        waitText("Marcar como pago")
        click("Confirmar")
        waitText("1/2 pagas (50%)")


        // Telas secundárias abrem sem crash
        click("Contas a Pagar")
        click("A Receber")
        waitText("Novo valor a receber")
        click("Extrato")
        waitText("Extrato & Histórico")

        click("Mais")
        waitText("Relatórios")
        click("Relatórios"); waitText("Relatório mensal"); back()
        click("Calendário financeiro"); waitText("Hoje, "); back()
        click("Previsão de saldo"); waitText("Projetado em"); back()
        click("Cartões de crédito"); waitText("Cadastrar cartão"); back()
        click("Contas recorrentes"); waitText("Impacto mensal"); back()
        click("Categorias"); waitText("Alimentação"); back()
        click("Configurações"); waitText("Segurança"); back()
        click("Contas"); waitText("Saldo total em contas ativas")
        // Carteira: 0 - 45,90 - 600,00
        waitText("-R$ 645,90")
    }
}

