# SpamBlocker: hierarquia e acessibilidade sem perder densidade

## Contexto e objetivo

SpamBlocker é um bloqueador de chamadas e SMS orientado a regras. O mantenedor quer muitos controles localizáveis de relance, inclusive em celulares compactos e idiomas largos; a discussão [#684](https://github.com/aj3423/SpamBlocker/issues/684) rejeita uma navegação em camadas e cartões grandes. Esta spec transforma os dez achados da auditoria de UI em mudanças pequenas e revisáveis. O resultado deve manter o tema escuro, a personalização e o acesso direto às regras.

## Decisão de design

Usar os componentes atuais (`Section`, `LabeledRow`, `SwitchBox`, `Button`, `G.palette`) como base de uma linha compacta e consistente. Cabeçalhos leves organizam a seção Quick Settings na mesma tela. O estado global ganha um controle compacto nas telas iniciais de chamadas e SMS. A alternativa de criar novas páginas ou cartões para cada grupo consumiria espaço e memória de navegação; a alternativa de corrigir cada linha isoladamente manteria as inconsistências.

Material 3 serve apenas como referência para papéis de cor, tipografia, estados e acessibilidade. Nenhuma mudança deve substituir a identidade visual do app por uma tela Material convencional.

## Escopo e limites

- Alterar somente `app/src/main/java/spam/blocker/ui/` e `app/src/main/res/`. Manter intactos `db/`, `service/`, lógica de regras e formato de preferências.
- Reutilizar as dependências e os recursos existentes. Nenhuma dependência nova. Não introduzir APIs Compose alpha ou experimentais que o projeto ainda não use; conferir assinaturas na versão do BOM antes da implementação.
- Preservar o estado salvo das seções recolhíveis, listas recolhíveis, opções ocultáveis, cores e demais escolhas do usuário. Não renomear chaves de preferências nem mudar valores padrão de regras.
- Todo texto novo vai para `res/values` em inglês. Não editar traduções manualmente. Registrar as chaves novas no relatório final. Preferir rótulos curtos.
- Um commit por tema. Cada commit deve compilar e manter os fluxos existentes utilizáveis.

## Comportamento e componentes

### 1. Controle principal acessível na tela inicial

Nas abas de histórico de chamadas e SMS, exibir uma linha compacta de status e switch global acima da lista, visível sem abrir Configurações. Reutilizar `G.globallyEnabled` e a gravação existente em `spf.Global`; o switch de `GloballyEnabled` continua disponível e ambos refletem a mesma alteração imediatamente. A ação de desligar não altera os switches individuais de chamada, SMS ou MMS. A linha deve caber em celular compacto sem cobrir busca, histórico ou FABs.

**Aceitação:** alternar em qualquer aba atualiza a outra aba e Configurações; sair e reabrir o app preserva o estado; as opções individuais permanecem como estavam. **Risco:** dois controles com estado divergente se cada um mantiver uma cópia local.

### 2. Alvos de toque compactos e suficientes

O alvo interativo de `SwitchBox`, botões de valor, controles de seção e ações por ícone terá pelo menos 48×48dp, mesmo com glifos menores. A expansão do alvo não pode invadir o alvo vizinho nem impedir o toque no rótulo. Linhas interativas adjacentes terão pelo menos 48dp de altura; em fonte grande ou idioma largo elas crescem conforme necessário.

**Aceitação:** inspeção das áreas clicáveis confirma 48×48dp para cada ação e ausência de sobreposição em telas compactas. **Risco:** aumentar áreas sem rever a disposição pode criar toques ambíguos.

### 3. Texto essencial completo em largura estreita

`SettingLabel` deixa de impor uma linha aos nomes essenciais. A linha de configuração reserva espaço para o switch e permite que o rótulo quebre; quando rótulo, valor e switch não couberem, o valor secundário passa para outra linha. Não truncar o nome da opção nem esconder o estado. Resumos longos podem usar elipse quando o nome da opção continuar visível.

**Aceitação:** telefone compacto com fonte 1,3× e inglês, alemão e russo apresenta rótulos completos sem sobreposição. **Risco:** linhas mais altas reduzem a quantidade visível nessas condições; limitar esse crescimento aos itens que precisarem.

### 4. Semântica e ordem de foco

`SwitchBox` anuncia nome, papel e estado ligado/desligado; o rótulo e switch formam uma ação compreensível no TalkBack sem duplicação. Quando a linha também tem editor de valor, ele é uma segunda ação identificada pelo nome da opção, na ordem visual anterior ao switch. Os FABs de ajustes, teste e exclusão recebem nomes específicos de `res/values`. Ícones decorativos próximos de texto não criam foco separado. Cabeçalhos recolhíveis anunciam título e estado expandido/recolhido; nas listas, o registro precede suas ações.

**Aceitação:** navegar por TalkBack não encontra ações sem nome, cliques sem efeito nem duas ações indistinguíveis na mesma linha. **Risco:** mesclar semântica indiscriminadamente pode ocultar o editor de valor.

### 5. Diálogos em celular compacto

`PopupSize` limita a largura mínima ao espaço disponível depois das margens da janela. O comportamento vale para os diálogos de histórico, teste e edição que hoje pedem 320 ou 340dp mínimos. Conteúdo interno pode rolar e campos permanecem acessíveis com o teclado aberto.

**Aceitação:** em janela de 320dp e 360dp, nenhuma borda ou ação do diálogo fica fora da área utilizável; com IME aberto, o campo focado permanece visível. **Risco:** linhas de formulários com conteúdo largo podem precisar quebrar.

### 6. Papéis de cor semânticos

Usar `G.palette.textGrey` para rótulos comuns; `teal200` ou `infoBlue` apenas para estados ativos, seleção e ações de destaque; `warning` para alertas; `error` para bloqueio e erro. Fazer o mapeamento na camada `ui/theme` e aplicá-lo aos componentes compartilhados antes de ajustar casos particulares. Preservar as cores configuradas pelo usuário e não tocar no armazenamento de `Palette`.

**Aceitação:** rótulos inativos deixam de parecer todos selecionados; cores de permissão, prioridade e resultado conservam seus significados. Verificar contraste WCAG AA no tema padrão: 4,5:1 para texto comum e 3:1 para ícones e controles significativos. **Risco:** paletas arbitrárias escolhidas pelo usuário podem não satisfazer esses índices; a implementação não deve sobrescrevê-las silenciosamente.

### 7. Uma linguagem para linhas de configuração

Consolidar `LabeledRow` em um componente de linha com três apresentações explícitas: switch para ligar/desligar, valor secundário clicável para abrir configuração e chevron para expansão ou navegação. Um valor como duração ou contagem não usa borda de botão, salvo quando for realmente um editor inline. Linha sem ação não recebe `clickable`. Migrar primeiro `RepeatedCall` e `OffTime`, depois os demais usos equivalentes em Quick Settings e nos diálogos de Histórico; preservar o diálogo e callback de cada configuração.

**Aceitação:** tocar no switch só altera o estado; tocar no valor abre a configuração; tocar no chevron expande ou navega; nenhum elemento aparenta ação sem responder. **Risco:** um componente genérico com ações implícitas mudaria comportamentos; as ações devem ser parâmetros distintos e opcionais.

### 8. Estados vazios de histórico e resumos

`HistoryList` mostra uma mensagem curta quando não há registros visíveis após a primeira carga. Se ambos os filtros de permitidos e bloqueados estiverem desligados, a mensagem orienta a ativar um filtro; caso contrário, uma busca ativa indica ausência de resultados, e a lista sem busca indica ausência de registros. Um estado de carga restrito à UI evita mensagem vazia antes da primeira leitura. `ApiSummary` e `BotSummary` mostram indicação discreta de ausência de itens ativos em vez de espaço em branco. A mensagem não impede criar uma regra ou API nem altera carregamento ou filtros.

**Aceitação:** o usuário distingue filtros desligados, lista vazia, busca sem resultado e primeira carga; resumos vazios não parecem falha de desenho. **Risco:** a recarga atual limpa e repovoa a lista; renderizar o estado vazio entre essas operações pode produzir um piscar.

### 9. Dependências visíveis no próprio item

Na linha de IA local, mostrar estado curto derivado dos dados existentes de treinamento; quando não houver amostras, indicar que é preciso treinar, sem afirmar que existe um modelo externo a configurar. Na linha RCS desabilitada, mostrar um motivo curto de indisponibilidade. Manter a ajuda detalhada atual acessível. Não desabilitar funcionalidade adicional nem inventar suporte a RCS.

**Aceitação:** a linha comunica por que a função ainda não está pronta ou disponível; o texto muda quando o estado de treinamento muda. **Risco:** status obtido de leitura antiga pode ficar desatualizado; reutilizar o mecanismo de atualização que já alimenta a UI de IA.

### 10. Hierarquia leve em Quick Settings

Dentro da seção Quick Settings atual, separar visualmente sinais de triagem, exceções, contexto e comportamento com cabeçalhos pequenos e espaçamento consistente. Manter a ordem das opções dentro de cada conjunto, todos os controles na mesma tela e a seção recolhível atual. Não criar páginas, cartões grandes nem uma camada nova de navegação. Ícones à esquerda só entram nas linhas que comportarem ícone e rótulo sem prejudicar idiomas largos.

**Aceitação:** todas as opções atuais continuam alcançáveis com a mesma quantidade de navegações; expandir/recolher e configurações previamente ocultadas conservam seu estado. **Risco:** cabeçalhos demais consomem altura; validar a leitura rápida em celular compacto antes de manter cada separador.

## Sequência de entrega

1. **Cores e linha compartilhada:** papéis semânticos, alvos de toque, semântica e apresentações switch/valor/chevron. Aplicar primeiro aos itens de menor complexidade. Um commit.
2. **Acesso global e Quick Settings:** controle principal nas abas iniciais, migração das linhas restantes, rótulos adaptativos, dependências e cabeçalhos leves. Um ou mais commits pequenos por tema.
3. **Diálogos e histórico:** largura compacta, estados vazios, nomes das ações e foco. Um commit por tema.

Cada etapa deve atualizar os chamadores do componente alterado e quaisquer recursos/previews relacionados. Não alterar comportamento de bloqueio, banco ou serviços.

## Verificação e critérios de conclusão

Após cada commit: executar `./gradlew assembleDebug` e lint; abrir `@Preview` para cada componente alterado em tema escuro, fonte 1,3× e idioma largo; executar testes de UI existentes, se houver. Verificar manualmente os cenários de TalkBack, celular compacto, janela de 320dp, IME, estado vazio, filtros, cores padrão e paleta personalizada. As assinaturas de APIs Compose usadas devem ser conferidas contra a versão instalada antes de escrever código.

O relatório final informa alterações por commit, o que ficou fora, novas chaves de string e riscos residuais. A aprovação desta spec autoriza a criação de um plano de implementação; a implementação começa após revisão desse plano.
