# SpamBlocker: redesign completo da interface

## Objetivo e decisões aprovadas

O mantenedor quer redesenhar toda a experiência de UI/UX do SpamBlocker, preservando as funções atuais. O [Truecaller](https://www.truecaller.com/blog/features/the-all-new-truecaller-bringing-communication-home) é referência para acesso rápido a chamadas, mensagens e proteção, não uma lista de recursos a reproduzir. O SpamBlocker mantém nome e comportamento de bloqueio próprios.

Decisões aprovadas na conversa de 18/10/2026:

- Navegação principal com quatro destinos: **Chamadas, SMS, Proteção e Configurações**.
- Duas famílias de tema, ambas em claro e escuro: **Catppuccin**, com [Latte e Macchiato](https://github.com/catppuccin/catppuccin/blob/main/docs/specs.md), e uma família azul inspirada no Truecaller.
- Novo ícone e identidade derivados do sino com filtro atual, com forma simplificada.
- As funções existentes permanecem disponíveis. O redesign não adiciona discador, caixa de mensagens própria, serviço comunitário de identificação nem outra função ausente do app.

O `DESIGN.md` e a spec `2026-10-18-spamblocker-ui-ux-design.md` descrevem uma direção anterior, contida e sem nova navegação. Esta spec substitui essa direção para o redesign. O plano de implementação deverá atualizar `DESIGN.md` antes de alterar telas, preservando ali as decisões de identidade aprovadas aqui.

## Leitura de design

Aplicativo Android de proteção para pessoas que precisam verificar rapidamente o que foi bloqueado, por quê e como ajustar a proteção. Linguagem de comunicação cotidiana, com informação densa e legível. **ENERGY 2 / RHYTHM 2 / MOTION 1.**

O ponto focal de Chamadas e SMS é o registro mais recente e seu resultado. Em Proteção é o estado global. Em Configurações é a tarefa ou categoria selecionada. A composição usa listas para histórico, grupos para ajustes e espaço maior apenas onde uma decisão precisa de atenção. Transições indicam mudança de tela ou estado; não há animação decorativa.

Razões visuais:

- **Cor:** uma cor de ação por família de tema destaca seleção e ação principal; vermelho, verde e amarelo ficam reservados para resultado, erro e alerta.
- **Tipografia:** escala nativa do Android e contraste entre título, número ou contato, motivo e dado secundário ajudam a ler registros rapidamente.
- **Espaçamento:** linhas de histórico continuam densas; controles e ações ganham área de toque suficiente e separação onde há risco de erro.
- **Componentes:** listas representam registros; grupos representam configurações relacionadas; cartões aparecem só para o estado principal ou uma decisão com consequência.
- **Ícones:** o sino com filtro identifica o produto; ícones de chamada, SMS, regra e estado só aparecem quando tornam a ação mais fácil de reconhecer.

## Estrutura de navegação

A barra inferior contém os quatro destinos aprovados, com rótulo e estado selecionado perceptíveis visualmente e por leitor de tela. O destino inicial de novas instalações é Chamadas. Ao atualizar, as rotas salvas `call`, `sms` e `setting` continuam válidas e abrem seus destinos correspondentes; `protection` passa a ser uma nova rota. Reabrir o app preserva a última aba válida. Abas não criam histórico de volta entre si; diálogos e telas internas voltam ao destino que os abriu.

### Chamadas e SMS

Cada aba conserva seu `HistoryViewModel`, tabela e filtros. O topo mostra título, estado compacto da proteção e ações identificadas de busca e filtro. Busca usa a pesquisa existente. O filtro oferece os dois controles independentes atuais, mostrar permitidos e mostrar bloqueados, sem alterar automaticamente as preferências salvas. Um resumo indica quando ambos estão desligados.

O registro usa contato ou número em cor de texto comum, resultado em rótulo semântico, motivo em texto secundário e horário visível. A expansão continua oferecendo detalhes e logs. Fotos reais de contato continuam quando disponíveis; a alternativa sem foto usa um símbolo neutro. Ações de abrir conversa, testar, copiar, criar regra, bloquear e excluir ficam disponíveis por controle ou menu nomeado. Gestos de deslizar e toque longo podem continuar como atalhos, mas não são o único acesso à ação. Exclusão mantém confirmação ou desfazer onde já existe.

As listas apresentam estados distintos de carregamento, vazio, filtro sem resultados e erro com nova tentativa. A recarga não deve mostrar um vazio transitório enquanto troca registros. Badges de não lidos respeitam a escala de fonte do sistema.

### Proteção

Uma tela própria reúne estado global, pausa temporária e estado de chamada, SMS e MMS, usando as mesmas fontes de estado e ações que existem hoje. A tela mostra quando a proteção está pausada ou depende de permissão, sem confundir isso com bloqueio desligado. Abaixo ficam entradas para regras de número e conteúdo, base de spam e os controles de triagem atuais, agrupados por tarefa. O usuário chega à edição existente das regras sem perder opções avançadas.

O conteúdo atual de **GloballyEnabled**, **Quick Settings** e **Regex Settings** passa a ser encontrado em Proteção, incluindo sinais de triagem, exceções, regras de contexto, comportamento, alertas e o teste de regras hoje aberto pelo FAB de Configurações. As opções de histórico continuam nos filtros e ajustes de Chamadas ou SMS.

Não haverá duas cópias independentes do mesmo switch. Um controle exposto em mais de uma tela compartilha a ação e reflete a alteração imediatamente. A tela Proteção não modifica valores de regras, prioridades ou permissões por conta própria.

### Configurações

Configurações concentra aparência, idioma, backup, integrações, automação, ajuda e opções avançadas que não precisam ocupar a tela de proteção. Os itens atuais continuam acessíveis por grupos com cabeçalhos claros e recolhíveis, que mostram seu estado. Diálogos existentes mantêm validação, erro e ações de salvar ou cancelar.

**Instant Query**, **Report Number**, **Automation** e **Miscellaneous** permanecem encontráveis em Configurações. O ajuste de notificações ligado ao bloqueio fica em Proteção, junto dos demais controles de comportamento; não aparece como cópia independente em Configurações.

## Sistema de temas e migração

O seletor apresenta família **Catppuccin** ou **Truecaller inspirado**, com aparência **Sistema, Claro ou Escuro**. Para novas instalações, a família inicial é Truecaller inspirado e a aparência segue o sistema. Catppuccin usa Latte no claro e Macchiato no escuro. A família Truecaller inspirada usa superfícies claras ou escuras e azul como cor principal, com cores próprias do SpamBlocker e sem copiar marca ou ilustrações de terceiros.

**Personalizado** continua disponível para as cores que o app já permite editar. Preferências de cor existentes permanecem gravadas e não são sobrescritas ao escolher um preset. Instalações com alguma cor personalizada ativa entram inicialmente em Personalizado; instalações sem cores personalizadas recebem o novo padrão. O editor de cores existente edita somente Personalizado. Trocar de preset e voltar restaura as escolhas personalizadas anteriores. A seleção de família e aparência é persistida separadamente das cores legadas. O modo Sistema reage à mudança do tema do Android sem reiniciar o app.

Componentes usam papéis semânticos compartilhados para fundo, superfície, texto, texto secundário, ação, seleção, sucesso, alerta e erro. O esquema Material claro ou escuro acompanha a aparência escolhida. Todas as quatro combinações precisam de contraste WCAG AA: 4,5:1 em texto comum, 3:1 em texto grande e controles significativos. A paleta Personalizado não pode salvar uma nova combinação ilegível sem aviso e correção; cores antigas não são apagadas silenciosamente. Elementos que usam `G.palette` diretamente precisam refletir a seleção efetiva, inclusive diálogos, status bar e navegação.

## Ícone e identidade

O novo ícone mantém o conceito aprovado de sino com filtro. A forma proposta é um sino sólido simplificado, com o filtro legível em espaço negativo no centro e sem detalhes finos que desapareçam em tamanhos pequenos. O desenho usa a cor principal da identidade azul com contraste suficiente sobre fundo claro ou escuro; o ícone do launcher é estável e não troca conforme o tema da UI. Produzir variantes adaptativa, redonda e monocromática compatíveis com os recursos Android atuais. O nome SpamBlocker permanece. Nenhuma foto, avatar fictício, estatística ou selo é criado para preencher a interface.

## Limites técnicos e dados

O redesign atua na camada `ui/`, recursos Android e preferências de apresentação. A lógica de triagem em `service/`, os dados e tabelas em `db/`, regras, relatórios e automações preservam seu comportamento. Não há migração destrutiva de banco. A implementação deve reutilizar componentes Compose e recursos existentes antes de adicionar outros. Não há dependência nova prevista.

Pontos de integração conhecidos: `MainActivity` e `BottomNavView` para navegação; `HistoryScreen`, `HistoryList`, `HistoryCard` e `HistoryContextMenu` para histórico; `SettingScreen` e `GloballyEnabled` para redistribuir proteção e preferências; `Theme.kt`, `SharedPref.Palette` e o editor de tema para seleção e preservação de cores; recursos `mipmap` para o launcher. A reorganização precisa alcançar chamadores dos componentes compartilhados, strings e previews afetados.

## Verificação e critérios de aceitação

1. **Navegação:** os quatro destinos abrem conteúdo real; seleção, retorno, rota salva e badges funcionam em telas compactas e com fonte ampliada.
2. **Histórico:** chamadas e SMS preservam busca, filtros, detalhes, ações, exclusão com recuperação e contagens existentes. Todas as ações são possíveis sem gesto oculto.
3. **Proteção:** ligar, desligar, pausar e retomar refletem o mesmo estado em todas as telas e persistem como hoje; opções de chamada, SMS e MMS mantêm suas dependências e permissões.
4. **Temas:** as quatro combinações e Personalizado cobrem telas, diálogos, barras do sistema, estados e contraste. Modo Sistema acompanha o Android. Trocar preset não apaga cores personalizadas.
5. **Acessibilidade:** controles têm alvo de pelo menos 48 dp, nome e estado anunciados, ordem de foco lógica e operação por teclado e TalkBack. Fonte ampliada e idiomas longos não cortam texto essencial.
6. **Estados:** histórico e listas redesenhadas mostram carregamento, vazio e erro com ação útil; nenhuma ação visual fica sem comportamento.
7. **Identidade:** o novo ícone é reconhecível em tamanhos de launcher e as duas famílias mostram o mesmo produto sem imitar ativos do Truecaller.

Executar build e testes relevantes, abrir o app em emulador ou dispositivo, percorrer cada controle alterado e registrar o resultado por tela e tema. Revisar telefone compacto, fonte ampliada, orientação e teclado aberto quando aplicável. Se não houver ambiente Android executável, a entrega não será declarada visualmente validada.

## Riscos e respostas

- **Perda de cores do usuário:** manter seleção de preset separada das chaves existentes e verificar ida e volta de Personalizado.
- **Desvio na lógica de bloqueio:** redistribuir controles da UI sem duplicar persistência nem alterar regras de triagem.
- **Configurações difíceis de achar:** mapear cada item atual para Proteção ou Configurações e testar a descoberta de tarefas frequentes e avançadas.
- **Quatro temas inconsistentes:** aplicar papéis de cor nos componentes compartilhados e validar cada combinação antes de ajustar telas isoladas.
- **Ações escondidas após simplificar o histórico:** conferir a lista completa de ações existentes em chamadas e SMS e manter acesso por menu ou controle.

A aprovação desta spec autoriza escrever o plano de implementação. A implementação começa após a revisão desse plano.
