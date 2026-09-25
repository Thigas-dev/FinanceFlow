# PRD — Aplicativo de Gestão Financeira Pessoal

**Nome provisório:** FinanceFlow
**Versão:** 1.0
**Status:** Planejamento
**Tipo de produto:** Aplicativo Web/Mobile de gestão financeira pessoal
**Público-alvo:** Pessoas que precisam controlar receitas, despesas, parcelas, contas recorrentes e valores a receber.

---

## 1. Visão do Produto

O FinanceFlow será um aplicativo de gestão financeira pessoal criado para centralizar, em um único lugar, todas as informações financeiras do usuário.

O sistema deverá permitir registrar e acompanhar:

* Dinheiro recebido;
* Dinheiro que ainda será recebido;
* Contas a pagar;
* Contas já pagas;
* Compras parceladas;
* Parcelas futuras;
* Contas recorrentes;
* Cartões de crédito;
* Saldo disponível;
* Previsão de saldo futuro;
* Categorias de receitas e despesas.

O principal objetivo é permitir que o usuário responda rapidamente:

> **Quanto tenho hoje? Quanto ainda vou receber? Quanto tenho para pagar? Quanto ficará disponível depois dos próximos pagamentos?**

---

# 2. Problema

Muitas pessoas controlam suas finanças utilizando anotações, planilhas, aplicativos diferentes ou simplesmente memória.

Isso gera problemas como:

* Esquecimento de contas;
* Perda de controle sobre parcelas;
* Dificuldade para saber quanto realmente está comprometido;
* Confusão entre contas pagas e futuras;
* Falta de visão sobre o saldo dos próximos meses;
* Dificuldade para controlar dinheiro emprestado ou valores a receber;
* Falta de histórico financeiro organizado.

O produto deverá solucionar esse problema oferecendo uma visão financeira simples, visual e centralizada.

---

# 3. Objetivos

## 3.1 Objetivo principal

Permitir que o usuário tenha uma visão clara de sua situação financeira atual e futura.

## 3.2 Objetivos específicos

O aplicativo deverá permitir que o usuário:

1. Registre receitas e despesas.
2. Registre contas parceladas.
3. Controle pagamentos futuros.
4. Controle valores que tem a receber.
5. Marque contas como pagas ou recebidas.
6. Visualize seu saldo atual.
7. Visualize receitas e despesas futuras.
8. Consulte o histórico financeiro.
9. Categorize movimentações.
10. Visualize relatórios e gráficos.
11. Receba alertas de vencimentos.
12. Controle diferentes contas bancárias e carteiras.
13. Controle cartões de crédito.
14. Consulte quanto ainda está comprometido em parcelas futuras.

---

# 4. Escopo do MVP

O MVP deverá priorizar a funcionalidade central do produto.

### Incluído no MVP

* Cadastro/login;
* Dashboard financeiro;
* Cadastro de receitas;
* Cadastro de despesas;
* Cadastro de contas a pagar;
* Cadastro de valores a receber;
* Parcelamento;
* Contas recorrentes;
* Categorias;
* Status de pagamento;
* Status de recebimento;
* Controle de contas financeiras;
* Histórico;
* Filtros;
* Calendário financeiro;
* Relatórios básicos;
* Notificações de vencimento;
* Previsão de saldo.

### Fora do MVP

Inicialmente não será obrigatório implementar:

* Open Finance;
* Integração automática com bancos;
* Investimentos;
* Criptomoedas;
* Emissão de boletos;
* Pagamentos bancários;
* Gestão empresarial;
* Multiusuário familiar;
* Inteligência artificial financeira avançada.

Esses recursos poderão entrar em versões futuras.

---

# 5. Público-Alvo

## Persona principal

**Pessoa física que deseja controlar sua vida financeira.**

Exemplos:

* Estudantes;
* Profissionais CLT;
* Autônomos;
* Freelancers;
* Pessoas que possuem financiamentos;
* Pessoas que realizam compras parceladas;
* Pessoas que recebem valores parcelados.

### Necessidade principal

Ter clareza sobre:

**Entradas + Saídas + Compromissos futuros = Situação financeira real.**

---

# 6. Conceito central do sistema

O sistema deverá trabalhar com dois conceitos diferentes:

### Movimentação

Representa qualquer entrada ou saída financeira.

Exemplos:

* Salário;
* Compra;
* Pix recebido;
* Transferência;
* Pagamento de conta.

### Compromisso financeiro

Representa algo que ainda será pago ou recebido.

Exemplos:

* Conta de energia;
* Parcela de financiamento;
* Compra parcelada;
* Salário futuro;
* Dinheiro emprestado para alguém.

Isso permitirá que o sistema diferencie:

**"Eu tenho R$ 3.000 hoje"**

de

**"Eu tenho R$ 3.000 hoje, mas já tenho R$ 1.800 comprometidos nos próximos dias."**

---

# 7. Funcionalidades

## 7.1 Dashboard

O Dashboard será a tela principal.

Deverá apresentar:

### Saldo atual

Valor disponível considerando movimentações efetivamente realizadas.

### A receber

Total previsto de receitas futuras.

### A pagar

Total previsto de despesas futuras.

### Saldo projetado

Fórmula:

**Saldo atual + valores a receber - valores a pagar**

### Resumo do mês

Exemplo:

| Indicador |    Valor |
| --------- | -------: |
| Receitas  | R$ 5.000 |
| Despesas  | R$ 3.200 |
| Resultado | R$ 1.800 |

### Próximos vencimentos

Mostrar contas próximas do vencimento.

Exemplo:

**Internet — vence em 2 dias — R$ 99,90**

### Próximos recebimentos

Exemplo:

**Salário — 05/10 — R$ 2.500**

---

# 8. Cadastro de receita

O usuário poderá cadastrar uma entrada.

### Campos

* Descrição;
* Valor;
* Data;
* Categoria;
* Conta de destino;
* Tipo;
* Status;
* Observação.

### Tipos

* Receita única;
* Receita parcelada;
* Receita recorrente.

### Exemplos

**Salário**

R$ 3.000
Data: 05/10/2026
Categoria: Salário

**Freelance**

R$ 800
Data: 15/10/2026
Categoria: Trabalho extra

---

# 9. Cadastro de despesa

O usuário poderá registrar uma saída.

### Campos

* Descrição;
* Valor;
* Data de vencimento;
* Categoria;
* Conta;
* Forma de pagamento;
* Status;
* Observação.

### Exemplos

* Aluguel;
* Energia;
* Internet;
* Combustível;
* Alimentação;
* Faculdade;
* Assinaturas;
* Compras.

---

# 10. Parcelamentos

Essa será uma das principais funcionalidades do sistema.

Ao cadastrar uma compra parcelada, o sistema deverá permitir:

**Valor total:** R$ 1.200
**Parcelas:** 12
**Valor da parcela:** R$ 100
**Primeiro vencimento:** 10/10/2026

O sistema deverá gerar automaticamente:

* Parcela 1/12;
* Parcela 2/12;
* Parcela 3/12;
* ...
* Parcela 12/12.

### Informações exibidas

**Notebook**

R$ 1.200

12x de R$ 100

Pagas: 3/12

Restantes: 9

Total restante: R$ 900

### Regras

Ao marcar uma parcela como paga:

* O sistema deverá atualizar a quantidade de parcelas pagas;
* Atualizar o valor restante;
* Registrar a movimentação financeira;
* Atualizar o saldo da conta utilizada.

---

# 11. Contas recorrentes

O sistema deverá permitir cadastrar despesas ou receitas recorrentes.

Exemplos:

* Aluguel;
* Internet;
* Netflix;
* Academia;
* Faculdade;
* Salário.

### Configuração

* Valor;
* Frequência;
* Data de início;
* Data final opcional;
* Dia de vencimento;
* Categoria;
* Conta;
* Ativo/Inativo.

### Frequências

* Mensal;
* Semanal;
* Quinzenal;
* Anual.

---

# 12. Valores a receber

O usuário poderá registrar dinheiro que outras pessoas ou empresas precisam pagar.

Exemplo:

**João**

Valor: R$ 500

Data prevista: 20/10/2026

Descrição:

"Empréstimo"

Status:

**Pendente**

Ao receber:

**Marcar como recebido**

O sistema deverá registrar automaticamente a entrada.

### Status

* Pendente;
* Recebido;
* Atrasado;
* Cancelado.

---

# 13. Contas a pagar

O sistema deverá apresentar uma lista específica para compromissos financeiros.

### Status

* Pendente;
* Pago;
* Atrasado;
* Cancelado.

### Informações

| Conta     | Vencimento |    Valor | Status   |
| --------- | ---------- | -------: | -------- |
| Internet  | 05/10      | R$ 99,90 | Pendente |
| Energia   | 08/10      |   R$ 180 | Pendente |
| Faculdade | 10/10      |   R$ 450 | Pago     |

---

# 14. Controle de contas financeiras

O usuário poderá cadastrar diferentes locais onde possui dinheiro.

Exemplos:

* Banco;
* Carteira;
* Poupança;
* Conta digital;
* Dinheiro físico.

### Campos

* Nome;
* Tipo;
* Saldo inicial;
* Instituição;
* Cor/identificação;
* Ativo/inativo.

Exemplo:

**Nubank**

Saldo: R$ 2.500

**Itaú**

Saldo: R$ 1.200

**Dinheiro**

Saldo: R$ 350

---

# 15. Transferências

O usuário poderá transferir dinheiro entre suas próprias contas.

Exemplo:

Nubank → Itaú

R$ 500

A transferência não deverá ser contabilizada como receita ou despesa.

Ela apenas altera os saldos das contas envolvidas.

---

# 16. Categorias

O sistema deverá permitir categorias padrão e categorias personalizadas.

### Receitas

* Salário;
* Freelance;
* Investimentos;
* Venda;
* Reembolso;
* Outros.

### Despesas

* Moradia;
* Alimentação;
* Transporte;
* Educação;
* Saúde;
* Lazer;
* Assinaturas;
* Compras;
* Impostos;
* Outros.

O usuário poderá criar suas próprias categorias.

---

# 17. Cartão de crédito

O aplicativo deverá possuir uma estrutura para controle de cartão.

### Cadastro

* Nome do cartão;
* Banco;
* Limite;
* Dia de fechamento;
* Dia de vencimento;
* Conta utilizada para pagamento.

### Compra

Exemplo:

Compra: R$ 1.200
Cartão: Nubank
Parcelas: 12
Parcela: R$ 100

O sistema deverá associar as parcelas ao cartão.

### Informações

**Limite:** R$ 5.000

**Utilizado:** R$ 1.800

**Disponível:** R$ 3.200

Também deverá apresentar as próximas faturas.

---

# 18. Calendário financeiro

O usuário poderá visualizar seus compromissos em formato de calendário.

Exemplo:

**05/10**

* Salário R$ 3.000

- Internet R$ 100

**08/10**

* Energia R$ 180

**10/10**

* Faculdade R$ 450

Isso permitirá identificar rapidamente dias com muitas movimentações.

---

# 19. Relatórios

O sistema deverá possuir relatórios financeiros.

### Relatório mensal

Mostrar:

* Total recebido;
* Total gasto;
* Total pendente;
* Resultado;
* Maior categoria de despesa;
* Quantidade de transações.

### Relatório por categoria

Exemplo:

Alimentação: R$ 900

Transporte: R$ 500

Lazer: R$ 300

Educação: R$ 700

### Relatório de parcelas

Mostrar:

* Total parcelado;
* Total já pago;
* Total restante;
* Quantidade de parcelas futuras.

---

# 20. Filtros

O usuário deverá conseguir filtrar movimentações por:

* Data;
* Tipo;
* Categoria;
* Conta;
* Status;
* Forma de pagamento;
* Cartão;
* Pessoa;
* Valor.

Também deverá existir busca por descrição.

---

# 21. Notificações

O sistema deverá alertar o usuário sobre compromissos.

### Exemplos

**3 dias antes**

"Você possui 2 contas vencendo nos próximos 3 dias."

**No dia**

"A conta de Internet de R$ 99,90 vence hoje."

**Após vencimento**

"A conta de Energia de R$ 180 está atrasada."

As notificações poderão futuramente ser enviadas por:

* Push;
* E-mail;
* WhatsApp.

---

# 22. Previsão financeira

Uma das funcionalidades mais importantes será a previsão.

Exemplo:

### Hoje

Saldo: R$ 2.500

### Próximas entradas

* R$ 3.000 salário

* R$ 500 freelance

### Próximas saídas

* R$ 1.200 cartão

* R$ 500 aluguel

* R$ 300 faculdade

### Saldo projetado

**R$ 4.000**

O usuário deverá conseguir visualizar essa projeção por:

* 7 dias;
* 30 dias;
* 60 dias;
* 90 dias;
* Personalizado.

---

# 23. Fluxo principal

### Adicionar despesa

Dashboard → Adicionar → Despesa

↓

Informar valor

↓

Escolher categoria

↓

Escolher data

↓

Escolher conta/cartão

↓

Escolher "À vista" ou "Parcelado"

↓

Salvar

↓

Sistema registra a movimentação.

---

# 24. Fluxo de parcelamento

Adicionar compra

↓

Selecionar "Parcelado"

↓

Informar valor total

↓

Informar quantidade de parcelas

↓

Informar primeira data de vencimento

↓

Sistema calcula parcelas

↓

Usuário confirma

↓

Sistema gera compromissos futuros.

---

# 25. Fluxo de pagamento

Conta pendente

↓

Usuário seleciona "Marcar como pago"

↓

Sistema confirma conta utilizada

↓

Atualiza saldo

↓

Altera status para "Pago"

↓

Registra movimentação no histórico.

---

# 26. Modelo de dados

Principais entidades:

### User

* id
* name
* email
* password
* created_at

### Account

* id
* user_id
* name
* type
* initial_balance
* current_balance

### Category

* id
* user_id
* name
* type

### Transaction

* id
* user_id
* account_id
* category_id
* description
* amount
* type
* date
* status
* payment_method
* notes

### Installment

* id
* transaction_id
* installment_number
* total_installments
* due_date
* amount
* status

### Recurrence

* id
* user_id
* description
* amount
* frequency
* start_date
* end_date
* active

### CreditCard

* id
* user_id
* name
* limit
* closing_day
* due_day
* account_id

### Receivable

* id
* user_id
* person
* description
* amount
* due_date
* status

### Notification

* id
* user_id
* type
* title
* message
* date
* read

---

# 27. Regras de negócio

### RN01 — Saldo

O saldo da conta deverá considerar somente movimentações efetivadas.

### RN02 — Contas futuras

Contas futuras não deverão reduzir imediatamente o saldo atual.

Elas deverão compor o **saldo projetado**.

### RN03 — Conta paga

Ao pagar uma conta, o sistema deverá registrar uma saída na conta escolhida.

### RN04 — Receita recebida

Ao marcar uma receita como recebida, o valor deverá entrar no saldo.

### RN05 — Parcelamento

O valor total de uma compra parcelada deverá ser dividido entre as parcelas conforme configuração definida pelo usuário.

### RN06 — Transferência

Transferências entre contas próprias não deverão impactar o resultado financeiro geral.

### RN07 — Cancelamento

Movimentações canceladas não deverão entrar no cálculo de saldo.

### RN08 — Atraso

Uma conta pendente cuja data de vencimento seja anterior à data atual deverá ser classificada como atrasada.

### RN09 — Recorrências

O sistema deverá gerar os próximos lançamentos conforme a periodicidade configurada.

### RN10 — Exclusão

Ao excluir uma movimentação já efetivada, o sistema deverá recalcular o saldo correspondente.

---

# 28. Telas

## Tela 1 — Login

* E-mail;
* Senha;
* Entrar;
* Criar conta;
* Recuperar senha.

## Tela 2 — Dashboard

Componentes:

* Saldo atual;
* Entradas;
* Saídas;
* A receber;
* A pagar;
* Saldo projetado;
* Gráfico;
* Próximos vencimentos;
* Últimas movimentações.

## Tela 3 — Movimentações

Lista completa das movimentações.

Filtros e busca.

## Tela 4 — Nova movimentação

Formulário de receita/despesa.

## Tela 5 — Parcelamentos

Lista de todas as compras parceladas.

## Tela 6 — Contas a pagar

Lista de compromissos financeiros.

## Tela 7 — A receber

Lista de valores pendentes de recebimento.

## Tela 8 — Contas

Lista de contas bancárias e dinheiro.

## Tela 9 — Cartões

Cartões, limite e faturas.

## Tela 10 — Relatórios

Gráficos e indicadores.

## Tela 11 — Calendário

Visualização das movimentações por data.

## Tela 12 — Configurações

* Perfil;
* Categorias;
* Notificações;
* Segurança;
* Preferências.

---

# 29. Dashboard — Estrutura sugerida

```text
┌─────────────────────────────────────────────┐
│ Olá, Thiago                                 │
│ Setembro/2026                               │
├─────────────────────────────────────────────┤
│                                             │
│ SALDO ATUAL                                 │
│ R$ 3.250,00                                 │
│                                             │
├──────────────┬──────────────┬───────────────┤
│ A RECEBER    │ A PAGAR     │ PROJETADO     │
│ R$ 2.100     │ R$ 1.850    │ R$ 3.500      │
├──────────────┴──────────────┴───────────────┤
│                                             │
│ Receitas x Despesas                         │
│             GRÁFICO                         │
│                                             │
├─────────────────────────────────────────────┤
│ PRÓXIMOS VENCIMENTOS                        │
│                                             │
│ Internet       05/10       R$ 99,90         │
│ Faculdade     10/10       R$ 450,00         │
│ Cartão        12/10       R$ 1.200,00       │
├─────────────────────────────────────────────┤
│ ÚLTIMAS MOVIMENTAÇÕES                       │
│                                             │
│ + Salário                 R$ 3.000         │
│ - Combustível             R$ 120           │
│ - Alimentação             R$ 45            │
└─────────────────────────────────────────────┘
```

---

# 30. UX/UI

A interface deverá priorizar:

* Simplicidade;
* Poucos cliques;
* Informações financeiras importantes visíveis;
* Responsividade;
* Boa visualização em celular;
* Navegação intuitiva;
* Feedback visual das ações.

### Princípio principal

O usuário deverá conseguir registrar uma despesa em poucos segundos.

---

# 31. Status visuais

As movimentações deverão possuir identificação clara.

### Receita

Entrada financeira.

### Despesa

Saída financeira.

### Pendente

Ainda não efetivada.

### Pago

Despesa concluída.

### Recebido

Receita concluída.

### Atrasado

Vencimento ultrapassado.

### Cancelado

Movimentação anulada.

---

# 32. Requisitos não funcionais

## Segurança

* Senhas armazenadas utilizando hash seguro;
* Autenticação;
* Sessões protegidas;
* HTTPS;
* Controle de acesso por usuário;
* Dados financeiros isolados entre usuários.

## Performance

O Dashboard deverá carregar rapidamente mesmo quando o usuário possuir milhares de movimentações.

## Responsividade

A aplicação deverá funcionar em:

* Desktop;
* Tablet;
* Smartphone.

## Disponibilidade

O sistema deverá possuir mecanismos de backup e recuperação de dados.

---

# 33. Critérios de aceite do MVP

### CA01

O usuário consegue criar uma conta e acessar o sistema.

### CA02

O usuário consegue cadastrar uma conta financeira.

### CA03

O usuário consegue registrar uma receita.

### CA04

O usuário consegue registrar uma despesa.

### CA05

O usuário consegue criar uma despesa parcelada.

### CA06

O sistema gera automaticamente as parcelas futuras.

### CA07

O usuário consegue marcar uma parcela como paga.

### CA08

O saldo da conta é atualizado após o pagamento.

### CA09

O usuário consegue cadastrar valores a receber.

### CA10

O usuário consegue marcar um valor como recebido.

### CA11

O sistema apresenta o saldo atual.

### CA12

O sistema apresenta o saldo projetado.

### CA13

O usuário consegue visualizar contas futuras.

### CA14

O usuário consegue filtrar movimentações.

### CA15

O sistema apresenta relatório mensal.

### CA16

O sistema identifica automaticamente contas atrasadas.

---

# 34. Backlog inicial

## Épico 1 — Autenticação

**US01:** Como usuário, quero criar uma conta para acessar o aplicativo.

**US02:** Como usuário, quero realizar login.

**US03:** Como usuário, quero recuperar minha senha.

---

## Épico 2 — Contas financeiras

**US04:** Como usuário, quero cadastrar minhas contas bancárias.

**US05:** Como usuário, quero visualizar o saldo de cada conta.

**US06:** Como usuário, quero realizar transferências entre minhas contas.

---

## Épico 3 — Receitas

**US07:** Como usuário, quero cadastrar receitas.

**US08:** Como usuário, quero visualizar receitas futuras.

**US09:** Como usuário, quero marcar receitas como recebidas.

---

## Épico 4 — Despesas

**US10:** Como usuário, quero cadastrar despesas.

**US11:** Como usuário, quero controlar contas pendentes.

**US12:** Como usuário, quero marcar contas como pagas.

---

## Épico 5 — Parcelamentos

**US13:** Como usuário, quero cadastrar uma compra parcelada.

**US14:** Como usuário, quero visualizar parcelas pagas e futuras.

**US15:** Como usuário, quero saber quanto ainda devo de uma compra.

---

## Épico 6 — Dashboard

**US16:** Como usuário, quero visualizar meu saldo atual.

**US17:** Como usuário, quero visualizar quanto tenho a pagar.

**US18:** Como usuário, quero visualizar quanto tenho a receber.

**US19:** Como usuário, quero visualizar meu saldo projetado.

---

## Épico 7 — Relatórios

**US20:** Como usuário, quero visualizar meus gastos por categoria.

**US21:** Como usuário, quero comparar receitas e despesas.

**US22:** Como usuário, quero visualizar meu histórico financeiro.

---

# 35. Priorização

## P0 — Obrigatório no MVP

* Cadastro/Login;
* Contas;
* Receitas;
* Despesas;
* Parcelamentos;
* Contas a pagar;
* Valores a receber;
* Dashboard;
* Saldo atual;
* Saldo projetado;
* Categorias;
* Histórico.

## P1 — Próxima versão

* Cartão de crédito;
* Calendário;
* Notificações;
* Relatórios avançados;
* Recorrências avançadas.

## P2 — Futuro

* Open Finance;
* Integração bancária;
* Inteligência artificial;
* Previsões automáticas;
* Gestão familiar;
* Investimentos;
* Integração com WhatsApp.

---

# 36. Métricas do produto

Os principais indicadores poderão ser:

### Ativação

Percentual de usuários que cadastram pelo menos uma movimentação após criar a conta.

### Retenção

Quantidade de usuários que continuam utilizando o aplicativo após 7, 30 e 90 dias.

### Movimentações

Quantidade média de lançamentos registrados por usuário.

### Controle financeiro

Percentual de contas pagas/recebidas dentro do prazo.

### Engajamento

Quantidade de acessos ao Dashboard por usuário.

---

# 37. Roadmap

## Fase 1 — MVP

Autenticação + contas + receitas + despesas + parcelamentos + dashboard.

## Fase 2 — Controle avançado

Cartões + recorrências + calendário + notificações.

## Fase 3 — Inteligência financeira

Relatórios avançados + projeções + análise de gastos.

## Fase 4 — Integrações

Open Finance + bancos + importação automática de transações.

---

# 38. Visão futura

O produto poderá evoluir de um simples gerenciador financeiro para um **assistente financeiro pessoal**.

Exemplos futuros:

> "Quanto vou ter disponível no final do mês?"

> "Quanto estou gastando com alimentação?"

> "Quais contas vencem esta semana?"

> "Quanto ainda tenho para pagar em parcelas?"

> "Se eu comprar um celular de R$ 2.400 em 12 vezes, como ficará meu orçamento?"

O sistema poderá responder essas perguntas utilizando os dados registrados pelo próprio usuário.

---

# 39. Definição de sucesso do MVP

O MVP será considerado funcional quando um usuário conseguir:

**Cadastrar suas contas → registrar suas entradas → registrar suas despesas → lançar parcelamentos → acompanhar contas futuras → marcar pagamentos/recebimentos → visualizar seu saldo atual e projetado.**

O principal valor entregue pelo produto será transformar informações financeiras dispersas em uma **visão única, organizada e previsível da vida financeira do usuário**.
