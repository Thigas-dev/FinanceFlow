# FinanceFlow

App de gestão financeira pessoal (ver `prd.md`) para **Android e iPhone**, escrito em Kotlin com
Compose Multiplatform: telas, regras de negócio e banco (Room) são um único código em `composeApp/src/commonMain`.

```
composeApp/src/commonMain   código compartilhado (UI, regras, banco)
composeApp/src/androidMain  Activity, notificações via WorkManager
composeApp/src/iosMain      ponto de entrada do iOS (MainViewController)
iosApp/                     app Swift que hospeda a UI + notificações do iOS (projeto via XcodeGen)
.github/workflows/build.yml build na nuvem: .ipa (macOS) e APK + testes (Linux)
```

## Android

```bash
./gradlew :composeApp:testDebugUnitTest :composeApp:assembleRelease
```

O APK assinado sai em `composeApp/build/outputs/apk/release/`. A assinatura usa `keystore.properties` +
`financeflow-release.jks` (fora do git — **guarde uma cópia**, sem eles não é possível atualizar o app).

## iPhone

A Apple só permite compilar apps iOS no macOS. Sem Mac, use o GitHub Actions (gratuito para repositórios públicos;
repositórios privados têm cota mensal de minutos, e minutos de macOS contam em dobro).

### 1. Gerar o .ipa na nuvem

1. Crie um repositório em <https://github.com/new> (ex.: `financeflow`).
2. Nesta pasta:
   ```bash
   git init
   git add .
   git commit -m "FinanceFlow"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/financeflow.git
   git push -u origin main
   ```
3. No GitHub, abra **Actions → Build**. Quando o job **iOS** terminar (~15–25 min), baixe o artefato
   **FinanceFlow-iOS** (um .zip contendo `FinanceFlow-unsigned.ipa`).

### 2. Instalar no iPhone com Apple ID gratuita (Sideloadly)

1. No PC com Windows, instale o **iTunes** e o **iCloud** (versões do site da Apple, não da Microsoft Store)
   e o **Sideloadly** (<https://sideloadly.io>).
2. Conecte o iPhone por cabo e toque em **Confiar neste computador**.
3. Abra o Sideloadly, arraste o `FinanceFlow-unsigned.ipa`, informe sua Apple ID e clique em **Start**.
   O Sideloadly assina o app com a sua conta.
4. No iPhone: **Ajustes → Geral → VPN e Gerenciamento de Dispositivos** → confie no seu Apple ID.
   No iOS 16+, ative também **Ajustes → Privacidade e Segurança → Modo de Desenvolvedor** (o iPhone reinicia).
5. Abra o FinanceFlow e permita as notificações.

**Limitações da Apple ID gratuita:** a assinatura vale **7 dias** — depois o app não abre até ser reinstalado
pelo Sideloadly (os dados são mantidos se você reinstalar por cima, sem apagar o app). No máximo 3 apps assim
por aparelho. Para eliminar isso: conta Apple Developer (US$ 99/ano) com TestFlight.

### Com um Mac (opcional)

```bash
brew install xcodegen
cd iosApp && xcodegen generate && open FinanceFlow.xcodeproj
```

Selecione seu time em *Signing & Capabilities* e rode no iPhone.

## Notificações

- **Android:** verificação a cada 12 h (WorkManager): contas vencendo em breve, no dia e atrasadas.
- **iOS:** ao abrir o app e após cada alteração, os lembretes das próximas datas são agendados no próprio iPhone
  (N dias antes e no dia do vencimento, às 9h), então chegam mesmo com o app fechado.
