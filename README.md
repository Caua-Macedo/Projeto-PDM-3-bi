# 🏃 ActiveTrack — Pedometer

Aplicativo Android desenvolvido em **Kotlin** que conta passos, calcula ritmo (cadência), distância percorrida e calorias estimadas, usando os sensores nativos do smartphone. O histórico diário é salvo localmente e exibido em um gráfico de evolução semanal.

Projeto desenvolvido com fins didáticos, priorizando código simples e fácil de entender.

---

## 📱 Funcionalidades

- Contagem de passos em tempo real
- Detecção de movimento via acelerômetro
- Cálculo de:
  - Cadência (passos por minuto — SPM)
  - Distância percorrida (km)
  - Calorias estimadas (kcal)
- Início/encerramento de treino
- Painel de estatísticas (liga/desliga)
- Histórico diário persistido localmente (Room)
- Gráfico de evolução semanal (últimos 7 dias)

---

## 🛠️ Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| Interface | XML (`LinearLayout`, `TextView`, `Button`, `ProgressBar`) |
| Sensores | `SensorManager`, `Sensor.TYPE_STEP_COUNTER`, `Sensor.TYPE_ACCELEROMETER` |
| Persistência | Room Database |
| Concorrência | Kotlin Coroutines |
| Gráficos | `Canvas` nativo (sem bibliotecas externas) |
| Build | Gradle (Kotlin DSL) com Version Catalog (`libs.versions.toml`) |

---

## 🔑 Permissões

| Permissão | Motivo |
|---|---|
| `ACTIVITY_RECOGNITION` | Necessária a partir do Android 10 para acessar o sensor de contagem de passos |

A permissão é solicitada em tempo de execução (`ActivityCompat.requestPermissions`).

---

## 🗂️ Estrutura do projeto

```
app/src/main/java/com/example/activetrack/
├── MainActivity.kt          # Tela principal, sensores e lógica do treino
├── HistoricoPassos.kt       # Entidade Room (registro diário)
├── HistoricoDao.kt          # Interface DAO (salvar / consultar histórico)
├── AppDatabase.kt           # Configuração do banco Room
└── GraficoSemanalView.kt    # View customizada do gráfico de barras

app/src/main/res/
├── layout/activity_main.xml # Interface da tela principal
├── drawable/bg_card.xml     # Fundo arredondado dos cards
└── values/colors.xml        # Paleta de cores do app
```

---

## 📊 Banco de dados (Room)

Cada dia gera **um único registro** na tabela `historico_passos`, identificado pela própria data (`yyyy-MM-dd`). Sempre que os passos são atualizados, o registro do dia é sobrescrito — formando uma série temporal simples que alimenta o gráfico semanal.

| Campo | Tipo | Descrição |
|---|---|---|
| `data` | `String` (chave primária) | Data do registro |
| `passos` | `Int` | Total de passos no dia |
| `distancia` | `Double` | Distância estimada (km) |
| `calorias` | `Int` | Calorias estimadas (kcal) |

---

## ⚙️ Configuração do ambiente

- **Android Studio**: versão compatível com AGP 8.7.x
- **AGP (Android Gradle Plugin)**: `8.7.3`
- **Kotlin**: `2.0.21`
- **KSP**: `2.0.21-1.0.28`
- **compileSdk / targetSdk**: `35`
- **minSdk**: `26`

Todas as versões são gerenciadas centralmente em `gradle/libs.versions.toml`.

### Rodando o projeto

1. Clone ou baixe o projeto.
2. Abra no Android Studio.
3. Aguarde o **Gradle Sync** finalizar (baixa as dependências do Room, KSP e Coroutines).
4. Rode em um dispositivo físico — sensores como `TYPE_STEP_COUNTER` geralmente **não funcionam em emuladores**.
5. Aceite a permissão de reconhecimento de atividade quando solicitado.

---

## ⚠️ Observações e limitações

- O cálculo de calorias e distância é uma **estimativa simples**, não substitui um dispositivo médico ou calibrado.
- A contagem de passos é feita desde a abertura do app (não reinicia automaticamente à meia-noite).
- Projeto não utiliza arquitetura MVVM, ViewModel ou LiveData — o foco é manter o código direto e fácil de acompanhar.
- Testado em dispositivo físico com Android 13+.

---

## 📄 Licença

Projeto de uso educacional/acadêmico.
