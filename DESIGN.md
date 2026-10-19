# Direção visual do SpamBlocker

## Origem

Escolha inicial do mantenedor em 18/10/2026: **calmo e legível**. Mais espaço para controles e textos, menos cores em destaque.

Atualização aprovada em 18/10/2026 para o **redesign completo da interface**: evolução da experiência de UI/UX mantendo todas as funções de bloqueio existentes, regras, tabelas do banco de dados e lógica de triagem intactas.

## Produto e público

Aplicativo Android de proteção contra chamadas e mensagens indesejadas. A interface atende a quem precisa verificar rapidamente o que foi bloqueado, por quê e ajustar a proteção sem atrito ou risco de erro. Linguagem cotidiana, informativa, densa e de alta legibilidade.

## Navegação principal (Quatro destinos)

A navegação global é composta por quatro destinos na barra inferior:

1. **Chamadas (`call`)**: histórico de chamadas, busca, filtros independentes de permitidas/bloqueadas e ações rápidas no registro. Destino padrão em novas instalações.
2. **SMS (`sms`)**: histórico de mensagens SMS/MMS, filtros dedicados, pesquisa e ações de triagem e bloqueio.
3. **Proteção (`protection`)**: centro de comando com estado global, pausa temporária, canais ativos, permissões e acesso organizado às regras rápidas e expressões regulares.
4. **Configurações (`setting`)**: preferências gerais, backup/restauração, integrações (APIs), automação e aparência/tema.

Ao atualizar, as rotas salvas `call`, `sms` e `setting` continuam válidas e `protection` integra a navegação. Reabrir o app preserva a última aba válida.

## Sistema de temas

O seletor de temas oferece duas famílias de temas pré-configurados e suporte a cores personalizadas:

- **Truecaller inspirado**: família azul própria do SpamBlocker com superfícies dedicadas para modo claro e escuro. Padrão inicial em novas instalações.
- **Catppuccin**: paleta com suporte oficial a **Latte** (modo claro) e **Macchiato** (modo escuro).
- **Personalizado (`Custom`)**: preserva integralmente as preferências de cor existentes do usuário. O editor de cores modifica exclusivamente o tema Personalizado, permitindo alternar para presets e retornar sem perder as cores salvas.

### Seletor de aparência

Disponível para as famílias predefinidas:
- **Sistema (`System`)**: acompanha dinamicamente o modo claro/escuro do sistema operacional Android sem exigir reinicialização do app.
- **Claro (`Light`)**: força superfícies e contraste de modo claro.
- **Escuro (`Dark`)**: força superfícies e contraste de modo escuro.
- O tema Personalizado deriva seu esquema claro/escuro da luminância da cor de fundo configurada.

## Identidade e ícone

- **Símbolo**: sino com filtro simplificado em desenho sólido, destacando o filtro em espaço negativo no centro, sem traços finos que desapareçam em telas pequenas.
- **Variantes**: launcher icon em variantes adaptativa, redonda e monocromática compatíveis com as diretrizes do Android.
- **Nome**: o nome do produto permanece estritamente **SpamBlocker**.

## Intensidade

- **ENERGY 2**: presença visual nítida, com destaque deliberado para estados de proteção e ações principais; vermelho, amarelo e verde reservados para resultados, alertas e permissões.
- **RHYTHM 2**: organização previsível e hierárquica entre listas de histórico e cartões de estado/configuração, garantindo contraste e alvos de toque adequados.
- **MOTION 1**: transições discretas para navegação e alternância de estados, sem animações decorativas.

## Razão das decisões

- **Cor e contraste**: conformidade WCAG AA (mínimo 4.5:1 para texto normal, 3:1 para texto grande e controles essenciais). Uma cor de ação principal por família temática evita poluição visual.
- **Ergonomia e toque**: controles interativos e botões de ajuda respeitam área de toque mínima de 48 dp.
- **Hierarquia e densidade**: tipografia nativa do Android valoriza a leitura rápida do histórico e regras sem desperdício de área útil.
- **Estabilidade de dados**: a seleção de temas usa chaves próprias (`theme_family` e `theme_appearance`) desacopladas das cores do usuário (`color_*`), garantindo que nenhuma personalização anterior seja perdida.
