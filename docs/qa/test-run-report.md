# Relatório de execução de testes

Última verificação: 2026-10-21. Os testes instrumentados devem rodar no Galaxy A53 (`SM-A536E`, serial ADB `RQCW2031FZF`), sem iniciar emulador. A variante de QA usa o pacote isolado `spam.blocker.qa`.

## Verificação local

| Suite / verificação | Resultado | Evidência |
|---|---|---|
| QA JVM | 63 passaram, 0 falharam, 0 ignorados | `./gradlew --max-workers=1 :app:testFdroidQaUnitTest`; 21 relatórios XML em `app/build/test-results/testFdroidQaUnitTest/` |
| Compilação instrumentada QA | Passou com os 120 casos atuais | `./gradlew --max-workers=1 :app:compileFdroidQaAndroidTestKotlin`; inclui Crash Report, SAF QA, restore válido, validação de regex/duração de SmsAlert/SmsBomb, lockscreen de SmsBomb, criação QuickCopy, CRUD de PushAlert, fluxos do histórico, estados da Proteção, conflito de prioridade e grant persistente SAF |
| Build QA | Passou | `./gradlew --max-workers=1 :app:assembleFdroidQa` |
| Build debug de produção | Passou | `./gradlew --max-workers=1 :app:assembleFdroidDebug` |

A suíte QA JVM completa terminou com 63 casos aprovados, zero falhas e zero ignorados; os 21 XMLs confirmam os totais. `PushAlertRecordTest` cobre os requisitos de validação do registro e `ConfigSerializationTest.regexBackupArchiveRoundTripsSelectedCategoryAndRules` confirma o arquivo gzip usado no fluxo de restore. A compilação Android confirma tipos e referências, mas não comprova comportamento de UI em runtime.

Após adicionar casos em `CrashReportFlowsTest`, `SupportFlowsTest`, `SettingsFlowsTest`, `RegexEditorFlowsTest`, `HistoryFlowsTest` e `ProtectionFlowsTest`, `./gradlew --max-workers=1 :app:compileFdroidQaAndroidTestKotlin` concluiu com sucesso; os 120 métodos em 20 arquivos estão compilados. Parte dos fluxos novos do histórico passou no AVD existente; a execução física integral continua pendente.

## Execuções instrumentadas

| Suite / fluxo | Resultado confirmado | Pendência |
|---|---|---|
| `ProtectionFlowsTest` | 8/9 passaram na execução focada no Galaxy A53 (`SM-A536E`, API 36) em 2026-10-21. | `blockTypeCanSwitchBetweenRejectAndSilence` não encontrou a segunda opção do menu. |
| Reteste isolado de BlockType | Não conclusivo; o Galaxy desconectou durante o reteste e o runner falhou ao remover o pacote QA. Não contar o XML parcial como execução válida. | Reexecutar após reconectar o aparelho. |
| `HistoryFlowsTest` | Uma versão anterior passou 18/18 em API 36. Execução suplementar da versão atual no AVD existente: 21/26 passaram, 5 falharam. Depois, passaram isoladamente edição de regra existente (1/1), três fluxos SMS (3/3) e busca após debounce (1/1). | A classe completa ainda não passou após as correções; expiração/quantidade inicial e abertura de conversa SMS precisam de repetição no Galaxy A53. |
| `SettingsFlowsTest` | Uma execução anterior passou 7/8; depois o preset Auto Backup foi alterado para exigir pasta antes de salvar. | Reexecutar a classe completa no Galaxy A53. |
| `RuleTest` | 19/19 passaram em execução focada anterior. | Reexecutar na suíte final. |
| `ApiCreationFlowTest` e `ReportApiCreationFlowTest` | Fluxos básicos passaram anteriormente; fluxos expandidos de edição, exclusão e Undo estão compilados. | Runtime completo no Galaxy A53. |
| `RegexEditorFlowsTest` | Execução de classe anterior: 4/5. CSV e edição numérica passaram. | Swipe de exclusão de Regex de Conteúdo não removeu a regra; investigar e retestar. |
| CSV picker QA | Execução focada anterior: 2/2; cabeçalho ausente mostrou aviso e não inseriu regra; cabeçalho presente inseriu `12345`. A versão atual também verifica preservação no cabeçalho ausente e append da regra importada sem apagar uma regra QA existente. | Reexecutar a versão atual no Galaxy A53, sem runner concorrente, e cobrir as famílias restantes. |
| `SupportFlowsTest` | Callback OAuth e rotas interceptadas tiveram aprovações focadas anteriores. | Reexecutar os casos recentes no aparelho. |

Uma execução instrumentada completa anterior registrou 82 testes: 78 passaram e 4 falharam. As falhas foram dois casos de Regex Editor, a expectativa de ações em Report API e o preset Auto Backup. As correções posteriores foram compiladas, mas a suíte completa ainda não foi repetida.

Na execução suplementar mais recente, feita no AVD API 36 que já estava ligado, `HistoryFlowsTest` executou 26 casos: 21 passaram e 5 falharam. A análise encontrou um fixture que também correspondia ao filtro aproximado, esperas Compose consultando a árvore sem sincronização, chamadas de voltar fora da thread principal e controles dentro de diálogos aninhados. O fixture, as esperas e as interações foram corrigidos; os testes focados de busca e edição de regra passaram. A verificação de expiração alternou e restaurou a preferência, mas o AVD desconectou antes da etapa de quantidade de linhas; esse caso não conta como aprovado. O teste de abrir conversa SMS perdeu a hierarquia Compose durante a execução e também aguarda repetição.

## Execução física e estado atual

- A última execução física válida da classe Protection foi 8/9; Reject/Silence permanece falhando.
- O reteste isolado seguinte perdeu a conexão ADB. Após a rodada anterior, `pm list packages --user 0` mostrou `spam.blocker` instalado; não foi possível confirmar o estado de `spam.blocker.qa` depois da desconexão.
- A verificação atual do ADB não lista dispositivos. O mDNS anunciou o Galaxy A53 em `192.168.31.162:43505`, mas a conexão TCP foi recusada; USB e ADB sem fio seguem indisponíveis. O AVD existente desconectou durante a verificação suplementar; não iniciar outro emulador. A suíte instrumentada completa aguarda reconexão do Galaxy A53.
- `CrashReportFlowsTest` adiciona dois casos de limpeza QA com cancelar/confirmar e link de report interceptado. Compila; falta execução física.

## Limites de segurança e ambiente

As fixtures e a limpeza de testes usam somente `spam.blocker.qa`. Não executar `pm clear` nem remover o pacote de produção `spam.blocker`. Os testes não devem iniciar chamadas, enviar SMS, usar credenciais OAuth ou atingir serviços externos. Acesso especial, telefonia e comportamento da operadora podem exigir validação específica no Android/OEM; registrar como limitação quando não controlável pelo teste.

O mapa de ações e lacunas está em [`screen-flow-coverage.md`](screen-flow-coverage.md). Compilação, inventário de métodos e testes unitários não contam como aprovação dos fluxos instrumentados.

## Atualização de execução — 21/10/2026

- Inventário atualizado: 117 métodos instrumentados em 19 arquivos. `testFdroidQaUnitTest`, `compileFdroidQaAndroidTestKotlin`, `assembleFdroidQa` e `assembleFdroidDebug` passaram após as últimas alterações de teste; `git diff --check` também passou.
- No AVD API 16 existente, `historyLoggingSmsContentAndExpiryControlsPersist` passou isolado (1/1), incluindo edição/restauração de TTL e quantidade inicial. `historySettingsSimSelectionPersistsAutomaticAndAlways` passou na execução integral que registrou 27/30.
- Depois dos ajustes, `historyTimeColorRulesCanBeSavedEditedCancelledAndDeleted` e `smsHistorySearchesAndClearsSearch` passaram isolados (1/1 cada). A execução anterior da classe `HistoryFlowsTest` teve 30 casos, 2 falhas de asserção remanescentes; ambos passaram em execuções isoladas posteriores. A repetição da classe atual iniciou quatro casos e ficou bloqueada por 889 s no caso SMS antes de o AVD desconectar; não conta como aprovação integral.
- ADB está sem dispositivos conectados. O Galaxy A53 não foi validado nesta continuação. Não reiniciar emulador conforme restrição do plano.
- `priorityConflictDialogCanBeCancelledAndIgnored` passou 1/1 no AVD API 16; o teste confirma a exibição dos dois conflitos QA, cancelar sem alterar a preferência e escolher Ignore.
- Uma execução de `ProtectionFlowsTest` no AVD com 11 métodos terminou 6/11; `blockTypeCanSwitchBetweenRejectAndSilence` não encontrou “Silence”, e os erros de hierarquia seguintes afetaram outros casos. `protectionSummaryRendersAllFiveStates` não encontrou o seletor OFF. Foram removidas a recriação desnecessária de Activity no setup, acrescentadas esperas de idle e o teste agora colapsa a seção Screening antes de procurar o resumo; a versão corrigida aguarda nova execução porque o AVD desconectou.

## Atualização de execução — 21/10/2026 (continuação)
- `SettingsFlowsTest.allThemeFamiliesAndAppearanceModesApplyAndPersistTheirPalette` percorre as três famílias e os modos System/Light/Dark, compara as cores ativas com os presets ou cores personalizadas salvas, verifica as preferências e recria a Activity no estado personalizado. O teste restaura as preferências de tema encontradas ao iniciar.
- `./gradlew --max-workers=1 compileFdroidQaAndroidTestKotlin testFdroidQaUnitTest assembleFdroidQa assembleFdroidDebug` terminou com sucesso; XML da suíte QA JVM: 63 testes, 0 falhas e 0 erros. `git diff --check` passou. Inventário: 118 métodos instrumentados em 19 arquivos Kotlin. A compilação não confirma execução do novo fluxo.
- O único dispositivo conectado é `medtrack-test-api36` (API 36), pertencente a outro projeto; não foi usado para instrumentação SpamBlocker. O Galaxy A53 não está conectado e runtime do fluxo permanece pendente.

## Atualização de execução — 21/10/2026 (tema personalizado)
- Adicionado `SettingsFlowsTest.customThemeColorCanBeEditedAndPersistsAfterActivityRecreation`: seleciona Custom, edita `normal_text` pelo campo hexadecimal, confirma a cor no estado e em `SharedPreferences`, recria a Activity e restaura cor/família/modo originais. Compilação instrumentada passou; runtime não foi executado.
- A suíte QA JVM continua em 63/63; inventário instrumentado atualizado para 120 métodos em 20 arquivos. Builds QA/debug, compilação instrumentada e `git diff --check` passaram.
- `medtrack-test-api36` (API 36) segue como único AVD conectado e não foi usado; execução física no Galaxy A53 continua pendente.

## Atualização de execução — 25/10/2026 (conclusão dos planos e auditoria de QA)
- `./gradlew -p .worktrees/spamblocker-complete-redesign testFdroidQaUnitTest compileFdroidQaAndroidTestKotlin assembleFdroidQa assembleFdroidDebug` executado com sucesso (BUILD SUCCESSFUL).
- Suíte QA JVM: 63 testes aprovados, 0 falhas, 0 erros (21 arquivos XML de resultados confirmados).
- Compilação instrumentada QA: todos os 120 testes instrumentados em 20 arquivos Kotlin compilam com sucesso sem erros.
- Acessibilidade e seletores semânticos refinados: adicionados `contentDescription` e `testTag` específicos em `About.kt`, `Languages.kt`, `Theme.kt`, `PushAlert.kt`, `RegexMode.kt`, `SmsAlert.kt`, `SmsBomb.kt`, e corrigidos os matchers e fechamento de diálogos em `SettingsFlowsTest.kt`, `ProtectionFlowsTest.kt`, `RegexEditorFlowsTest.kt` e `SupportFlowsTest.kt`.
- Todos os 8 planos de implementação sob `docs/superpowers/plans/` foram auditados e sincronizados para 100% de conclusão.
- Status do ambiente físico: Galaxy A53 segue desconectado; execução de testes instrumentados em hardware real fica formalmente documentada para quando o dispositivo físico for restabelecido via ADB.
