# Especificação: cobertura completa de telas e funcionalidades

## Objetivo

Mapear as telas e os fluxos interativos do SpamBlocker e criar testes repetíveis para sua lógica e sua interface. A cobertura combina testes unitários no JVM e testes instrumentados da aplicação real em um emulador Android.

O trabalho usa a branch `redesign/complete-redesign`. Não muda o comportamento do app fora do necessário para tornar os testes determinísticos e acessíveis por semântica.

## Direção e escopo

O `DESIGN.md` define a experiência existente: leitura rápida, proteção contra chamadas e mensagens indesejadas, quatro destinos e temas Claro/Escuro/Sistema. O trabalho valida essa interface, sem redesenhá-la.

Incluído:

- Chamadas e SMS: histórico, busca, filtros, estados vazio/carregando/erro, pausa, registro e ações de contexto.
- Proteção: estados global, pausa e permissões; todos os 14 controles de regra rápida; as seis áreas de regra adicional; diálogos de teste e conflito.
- Configurações: APIs de consulta e denúncia, automações, idioma, temas, backup/restauração, FAQ e informações do app.
- Destinos de suporte e diálogos: crash report, retorno OAuth, trampoline de notificação, seleção de arquivo e permissões do Android.
- Persistência, resolução de estados, validação e decisões do motor de regras.

Chamadas telefônicas, SMS, OAuth, rede e APIs serão simulados. Nenhum teste envia mensagens, inicia chamadas reais ou usa credenciais externas. Permissões e intents do sistema serão controlados no emulador.

## Mapa de telas e fluxos

O inventário entregue em `docs/qa/screen-flow-coverage.md` relaciona cada destino, seção e diálogo às ações, estados, teste responsável e resultado. A cobertura da interface inclui:

| Destino ou fluxo | Ações e estados cobertos |
| --- | --- |
| Chamadas | Navegação, filtros de permitidas/bloqueadas, busca, vazio/erro/conteúdo, pausa, alternância global e ações do registro |
| SMS | Navegação, filtros e busca, estados da lista, ações do registro e regras de triagem |
| Proteção | Estados OFF/PAUSED/PERMISSION_REQUIRED/CHANNELS_OFF/ACTIVE, permissões, canais, pausa/retomada, regras rápidas, regras regex, teste e conflitos |
| Configurações | Seções expansíveis, APIs de consulta/denúncia, automações, idioma, presets de tema, cor personalizada, backup/restauração, FAQ e Sobre |
| Fluxos de suporte | Entrada/erro/fechamento de diálogos, permissão negada/concedida, intents simulados e retorno ao app |

Cada ação interativa visível terá um teste responsável ou uma limitação explícita no mapa. Elementos sem destino funcional serão registrados como lacuna, não contados como cobertura.

## Estratégia de testes

### Testes unitários

Usar JUnit existente para lógica sem dependência de Activity: estados e rotas, validação e persistência de preferências, presets, regras de triagem, agenda, pausa, tokenização, importação e modelos de automação. Cobrir as famílias de regras individualmente e também conflitos de prioridade e casos de fronteira.

### Testes instrumentados no emulador

Usar a infraestrutura de testes Compose para iniciar `MainActivity`, localizar elementos pela semântica estável e executar fluxos completos nos quatro destinos. Adicionar `testTag` somente onde texto, role ou content description não forem suficientes.

Os testes instrumentados usam banco, preferências e fixtures exclusivos da variante QA. Cada caso começa em estado conhecido e restaura o estado de teste ao terminar. Os fluxos incluem interação, resultado visível, persistência após recriação da Activity e os estados de permissão relevantes.

### Variante isolada

Criar o build type `qa`, compatível com `debug`, com `applicationIdSuffix = ".qa"`. Os testes rodam em `fdroidQa`, cujo pacote é `spam.blocker.qa`; assim podem limpar seus próprios dados sem desinstalar nem alterar o pacote `spam.blocker` que o usuário usa.

Comandos previstos:

```sh
./gradlew testFdroidQaUnitTest
./gradlew connectedFdroidQaAndroidTest
```

## Inventário inicial

Em 20/10/2026, a branch tinha testes unitários e instrumentados existentes para regras, banco, navegação, estados de proteção e histórico, temas, acessibilidade de widgets, pausa, tokenização, fórmulas e agendas.

Na execução inicial, os testes unitários passaram. Dos 37 testes instrumentados, 36 passaram e `RuleTest.database_prefix` falhou em `RuleTest.kt:733`: esperava `RESULT_ALLOWED_BY_DEFAULT` e recebeu `RESULT_BLOCKED_BY_DATABASE_PREFIX_REGEX`. Essa falha deve permanecer registrada como baseline até ser reproduzida e diagnosticada.

## Critérios de aceite

1. O mapa lista todas as telas próprias, seções com controles, diálogos e atividades de suporte descobertas no código, com teste ou lacuna atribuída.
2. Lógica de decisão e persistência possui testes unitários determinísticos, incluindo limites e caminhos de erro relevantes.
3. Fluxos de usuário por todos os quatro destinos são executados por testes Compose no emulador.
4. Testes exercitam temas Claro, Escuro e Sistema, estados vazios/carregando/erro e permissões concedidas/negadas onde aplicável.
5. APIs, OAuth, rede, chamadas e SMS usam simulações locais e não dependem de serviços externos.
6. A execução da suíte QA não remove, limpa nem altera os dados do pacote `spam.blocker`.
7. Um relatório final identifica comandos, quantidade de testes, falhas e lacunas que dependam de hardware ou serviço real.

## Riscos e limites

O app concentra estado em singletons globais. Os testes precisam reiniciar a Activity após configurar preferências e limpar dados apenas no pacote QA. Recursos geridos exclusivamente pelo Android ou hardware de telefonia serão verificados por simulação e estado de permissão, sem alegar validação em uma rede móvel real.
