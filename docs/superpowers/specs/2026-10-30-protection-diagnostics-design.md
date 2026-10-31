# Diagnóstico guiado da proteção — desenho

## Intenção

Explicar por que chamadas ou SMS não estão sendo tratados como esperado e orientar a próxima ação. O app já calcula estados gerais de proteção e tem uma cadeia para pedir permissões.

## Objetivos

- Apresentar estado por recurso (chamadas, SMS e notificações) com causa verificável.
- Indicar a ação mínima necessária e abrir a tela de sistema correspondente quando aplicável.
- Revalidar o estado ao retornar das Configurações do Android.

## Fora de escopo

- Pedir ou ativar permissões sem ação explícita do usuário.
- Alterar automaticamente o app de SMS padrão, regras ou preferências do usuário.
- Prometer funcionamento em aparelhos cuja política do fabricante impeça o serviço.

## Abordagens

- **Lista estática de permissões:** simples, mas não explica o efeito por recurso.
- **Diagnóstico acionável na tela Proteção (proposta):** reutiliza resumo e estado atual, com passos contextuais.
- **Assistente obrigatório no primeiro início:** interrompe configuração parcial e é mais invasivo; rejeitada.

## Proposta

Adicionar um cartão `Verificar proteção` na tela Proteção. Ao executar, avaliar separadamente proteção global/pausa, permissões runtime, acessos especiais, canais de notificação e aviso de notificações SMS duplicadas. Exibir estado `Ativo`, `Ação necessária` ou `Opcional`, o recurso afetado e um botão para a tela de sistema certa. Recalcular no retorno ao app; não guardar estado calculado como fonte de verdade.

## Restrições e critérios de aceite

- Só afirmar `Ativo` quando as condições relevantes forem verdadeiras agora.
- Permissão negada não impede usar outros recursos independentes.
- Não lançar intents de sistema sem clique e tratar ausência de tela compatível.
- Mensagens distinguem permissão opcional de bloqueio real de funcionalidade.
- O diagnóstico pode ser repetido e atualiza após mudança externa.

## Testes requeridos

Testes unitários para combinações de estados; testes Android para ações de Configurações, retorno, permissão negada, notificação desligada e múltiplas funções com estados diferentes.

## Decisões propostas para aprovação

Cartão acionável dentro da tela Proteção; execução sob demanda e ao retornar do sistema; sem assistente obrigatório nem mudanças automáticas.
