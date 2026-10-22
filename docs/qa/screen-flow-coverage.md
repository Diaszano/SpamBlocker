# Inventário de telas e fluxos

Este inventário distingue teste existente, execução instrumentada confirmada e lacuna. Um teste compilado não comprova comportamento em runtime. Os testes instrumentados devem usar `fdroidQa` (`spam.blocker.qa`) no Galaxy A53; não iniciar emulador. Nenhum teste deve originar chamada, SMS, OAuth real ou requisição externa.

## Navegação e histórico

| Fluxo | Evidência no código | Ainda falta validar |
|---|---|---|
| Navegação principal | `MainNavigationTest.allFourTabsCanBeSelectedAndHistoryReselectionKeepsSelection` seleciona as quatro abas e toca novamente na aba atual. | Execução física depois das alterações atuais; rota do SMS padrão depende dos apps e da configuração do aparelho. |
| Histórico de chamadas | `HistoryFlowsTest` cobre fixtures QA, busca e limpeza, filtro Allowed, pausa/retomada, menu de teste de regra, cópia, busca web interceptada, discador `tel:` interceptado, inclusão na base de spam, salvar regra de prefixo/regex, edição de regra existente, excluir uma linha, abrir configurações e confirmar exclusão total. Edição existente e ações locais passaram em AVD API 36; discador e classe completa aguardam execução. | Filtro de bloqueados e execução física no Galaxy A53. |
| Histórico de SMS | `HistoryFlowsTest` cobre fixtures QA, busca/limpeza/no-match, filtro de bloqueados, intent `smsto:` interceptado, marcação como lida, inclusão na base de spam, diálogo de teste de regra, busca web, salvar regex, excluir apenas a linha escolhida e abrir o painel. Os três fluxos novos passaram em AVD API 36; nenhum SMS foi enviado. | Estados loading/erro/retry no Galaxy A53, busca por prefixo, confirmação do intent no Galaxy A53 e leitura confiável da área de transferência. |
| Estados das listas | `HistoryEmptyTest` testa seleção de mensagem vazia, loading inicial e precedência. `HistoryFlowsTest` adiciona loading, erro, retry, vazio e conteúdo Compose para chamadas e SMS. | Execução física desses estados no Galaxy A53. |
| Configurações do histórico | `HistoryFlowsTest` exercita abrir o painel, visibilidade de Allowed, confirmação de excluir tudo, Indicador, Localização, Operadora, Cor por horário, logging, conteúdo SMS e expiração. A alternância de expiração passou parcialmente no AVD; a execução perdeu o AVD antes de concluir a edição da quantidade inicial. | Persistência de Bloqueados, quantidade inicial e demais controles no Galaxy A53. Seleção SIM e CRUD/cancelamento de cores passaram focados no AVD; validação física ainda pendente. |

## Proteção e regras

`ProtectionFlowsTest` tem dez casos. A execução focada mais recente no Galaxy A53 passou 8/9 antes da adição do teste dos cinco estados; `blockTypeCanSwitchBetweenRejectAndSilence` não encontrou a segunda opção do menu. Um reteste isolado posterior foi interrompido pela desconexão do aparelho e não é evidência válida.

| Área | Evidência no código | Ainda falta validar |
|---|---|---|
| Resumo e canais | `protectionSummaryRendersAllFiveStates`, `protectionSummaryShowsPermissionRequiredWhenEnabledChannelLacksPermission`, `globalProtectionSwitchPersistsOffAndOnInQaPackage`, `callAndSmsProtectionChannelsCanBeDisabledLocally` e `ProtectionSummaryTest`. OFF e ACTIVE usam tags semânticas de teste porque só exibem ícones; os outros estados são verificados pelo texto. | Executar os cinco estados na interface no Galaxy A53; permissões concedidas, negadas e revogadas em runtime. |
| Pausa e horário | `protectionCanBePausedAndResumedLocally`, `offTimeTogglePersistsAndOpensTimeRangeEditor`, `PauseLogicTest`, `ScheduleTest` e `RuleTest.off_time`. | Edição de início/fim e validação do efeito de bloqueio no período pela UI. |
| Regras rápidas | `protectionScreenExposesAllQuickAndAdditionalRuleAreas` verifica acesso às áreas e `RuleTest` cobre parte da decisão de regra para contatos, chamadas repetidas, discagem, apps recentes, agenda, operadora e localização. | Controles e configurações das 14 regras rápidas, incluindo reset, cancelar e salvar. Faltam fluxos de LocalAI, exceções, notificações, CallerID e permissões. |
| Tipo de bloqueio | `blockTypeCanSwitchBetweenRejectAndSilence` tenta alterar Reject/Silence e verificar persistência local; permanece falhando em runtime. `RuleTest` cobre parte da decisão. | Corrigir e aprovar a alternância na UI. Hang Up e permissões de chamada não devem iniciar chamada real. |
| Seções | `protectionRuleSectionsCanBeCollapsedAndExpanded` verifica recolher e expandir seções; passou na execução focada 8/9. | Repetir na suíte física completa. |
| Teste de regra | `ruleTestingFabOpensDialogWithoutRunningTests` abre e fecha o diálogo sem disparar a triagem. | Conflitos de prioridade: abrir, resolver e cancelar na UI. |
| Regex numérica | `RegexEditorFlowsTest.invalidNumberRegexCannotBeSavedOrPersisted` e `validNumberRegexCanBeSavedToQaDatabase`; há também importação CSV com cabeçalho presente/ausente. Testes JVM cobrem estado, presets e parser. | Busca, aplicação de preset pela UI, exclusão e edição iniciada pelo histórico; execução física após alterações atuais. |
| Regex de conteúdo | `contentRegexCanBeCreatedEditedAndDeletedThroughUi` cria e edita regra local. O swipe de exclusão não removeu o item nas execuções focadas anteriores. | Consertar e provar exclusão; busca, presets e ações de QuickCopy continuam sem cobertura de UI. |
| QuickCopy, PushAlert, SmsAlert e SmsBomb | `RegexEditorFlowsTest.quickCopyRegexCanBeCreatedEditedAndDeletedThroughUi` cria/edita/exclui uma regex QuickCopy e verifica flags de número, conteúdo, permitido e cópia automática. `SettingsFlowsTest.smsAlertAndSmsBombSwitchesPersistInQaPreferences` alterna os switches, verifica regex inválida não persistir, regex válida e duração para SmsAlert/SmsBomb, e alterna a proteção de lockscreen de SmsBomb; permissões são simuladas e restauradas. `SettingsFlowsTest.pushAlertCanBeCreatedEditedAndDeletedWithQaAppSelection` faz CRUD e seleciona o próprio pacote QA. `PushAlertRecordTest` cobre validação local. | Executar fisicamente esses fluxos. Não enviar SMS nem depender de notificação de operadora. |
| Base de spam | Histórico e `RuleTest.database_prefix` exercitam inclusão e decisão local. | Remoção e expiração pela UI. |
| Acesso especial / telefonia | Testes não originam chamadas ou mensagens. | Handoff de Usage Access, overlay, listener, papel padrão e alarmes; comportamento de SIM/operadora e overlay em chamada exige ambiente compatível. Painéis e rede da operadora são limitações externas, não resultados aprovados. |

## Configurações e integrações

| Fluxo | Evidência no código | Ainda falta validar |
|---|---|---|
| Seções e aparência | `SettingsFlowsTest.settingsSectionsAreReachableAndThemeChoiceCanBeChanged`, `SettingsFlowsTest.allThemeFamiliesAndAppearanceModesApplyAndPersistTheirPalette`, `SettingsFlowsTest.customThemeColorCanBeEditedAndPersistsAfterActivityRecreation`, `settingsSectionsPersistCollapseAndExpandActions` e testes JVM de presets/seleção. | Executar os fluxos no Galaxy A53 e validar reinício real do processo. |
| Idioma | `languagePickerShowsSystemAndSupportedLocalesWithoutApplyingOne` abre o seletor e verifica Sistema e um idioma disponível. | Selecionar idioma e confirmar atualização/persistência da Activity no aparelho. |
| APIs de consulta e denúncia | `ApiCreationFlowTest` e `ReportApiCreationFlowTest` cobrem CRUD/Undo local e cancelar sem gravar; `ReportApiRulesTest` cobre lógica local. | Presets, validações completas, configuração OAuth e confirmação de ausência de chamadas de rede em cada ação. |
| Bots e automação | `SettingsFlowsTest` cobre lista/abertura de templates, salvar bot manual, excluir/restaurar por snackbar e exige pasta configurada antes de salvar Auto Backup. Testes JVM cobrem templates e sequência de ações. | Aplicar templates, editar/excluir os demais tipos, validar agenda/ações e consultar logs. Retestar os fluxos recentes no aparelho. |
| CSV | Testes JVM cobrem parser, escaping, campos finais, cabeçalho e ação do bot. `RegexEditorFlowsTest` e `SettingsFlowsTest` cobrem picker via provider QA para cabeçalho presente/ausente; a ausência de `pattern` mantém uma regra QA preexistente, e a importação válida verifica que `12345` é acrescentado sem remover a regra anterior. | Executar as asserções atuais no Galaxy A53 e cobrir as famílias restantes. A execução física anterior confirmou inserção, mas não verificou o valor nem preservação/append de regras preexistentes. |
| Backup e restauração | `SettingsFlowsTest` cobre diálogo de categorias, exportação para URI QA, importação malformada com sentinelas QA em Bot e NumberRegex preservadas, e `validBackupImportRestoresSelectedRegexRulesFromQaDocumentUri` para restore válido. `ConfigSerializationTest.regexBackupArchiveRoundTripsSelectedCategoryAndRules` valida o gzip e a regra pela suíte JVM; restore UI compila, runtime pendente. | Preservar e afirmar todas as categorias em cada falha de importação; executar no Galaxy A53. |
| FAQ, Sobre e versão | `SettingsFlowsTest.faqAndAboutDialogsCanBeOpenedAndDismissed`. | Todos os links externos com destino interceptado, cópia, versão e ações restantes. |
| Logcat | Seção pode ser alcançada por Settings. | Exportar/copiar e validar conteúdo sem depender de logs pessoais. |

## Suporte e fluxos do Android

| Fluxo | Evidência no código | Ainda falta validar |
|---|---|---|
| Relatório de crash | `SupportFlowsTest.crashReportActivityAcceptsLocalStackTrace` verifica stack trace sintético local. `CrashReportFlowsTest` verifica cópia do stack trace, cancelar/confirmar limpeza de chamadas e SMS QA e intercepta o destino do link de report; compila, aguarda execução física. | Reproduzir os casos em runtime no Galaxy A53. |
| OAuth | `SupportFlowsTest` cobre callback válido, ausente, desconhecido e malformado com token local, sem navegador nem credenciais reais. | Nenhum login externo será executado; confirmar isolamento em execução física completa. |
| Notification trampoline | `SupportFlowsTest` intercepta rota de chamadas, SMS desbloqueado e tipo desconhecido. | SMS bloqueado, cancelamento de notificação e comportamento do app padrão são dependentes do Android/OEM. |
| SAF / seletor de arquivos | `SupportFlowsTest.documentPickerReadAndWriteIntentsReturnControlledCancellation` verifica cancelamento de abrir/criar documento. `documentPickerReturnsSelectedQaUrisAndStoresDirectoryTags` verifica leitura/gravação por provider QA e tags de URI; `safDirectoryGrantPersistsAndCanBeRevoked` cobre negação, persistência read/write e revogação. Compilação passou; runtime pendente. | Executar no Galaxy A53 ou AVD QA isolado e confirmar persistência e limpeza do grant. |
| Permissões de runtime | Um teste verifica estado de permissão necessária; não há matriz completa de conceder/negar/revogar. | Exercitar cenários suportados por `UiAutomation`; testar fallback de acesso especial sem afirmar cobertura de telas Android/OEM. |

## Isolamento e execução

- `QaPackageIsolationTest.instrumentationTargetsOnlyTheQaApplication` afirma `spam.blocker.qa`; `QaTestEnvironment` usa o contexto alvo e fixtures QA.
- O inventário atual contém 120 métodos instrumentados em 20 arquivos Kotlin de teste e 63 testes JVM. A última suíte JVM completa passou: 63 passaram, 0 falharam e 0 foram ignorados.
- `ProtectionFlowsTest` teve resultado físico 8/9 na execução focada mais recente; o caso Reject/Silence continua pendente. O Galaxy A53 desconectou no reteste seguinte. A suíte instrumentada completa de 120 casos não foi executada após as alterações recentes.
- O estado do pacote `spam.blocker.qa` após a desconexão não pôde ser confirmado. A última verificação antes dela encontrou o pacote de produção `spam.blocker` instalado.
- Consulte [`test-run-report.md`](test-run-report.md) para resultados históricos, comandos e limitações. Build ou compilação não contam como teste de runtime.

## Atualização de validação — 21/10/2026

`HistoryFlowsTest` agora inclui edição da regex existente, interceptação do discador, persistência de visibilidade Allowed/Blocked, seleção automática/sempre de SIM, edição/cancelamento/exclusão de cores, edição de TTL/quantidade de linhas, e fluxos de SMS de busca online, salvar regex, exclusão e abertura das configurações. Os casos recentes de TTL, SIM, cores e busca SMS passaram individualmente no AVD. A classe completa ainda não passou na versão atual porque o AVD desconectou durante uma repetição; a execução física no Galaxy A53 continua pendente.

`ProtectionFlowsTest.protectionSummaryRendersAllFiveStates` cobre OFF, PAUSED, PERMISSION_REQUIRED, CHANNELS_OFF e ACTIVE em código e compila. Ainda falta executar os estados e as permissões no Galaxy A53.
