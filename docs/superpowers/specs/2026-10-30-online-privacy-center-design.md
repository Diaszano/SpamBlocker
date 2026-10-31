# Transparência das integrações online — desenho

## Intenção

Mostrar, antes de usar consultas e denúncias online, quais integrações estão ativas e que dados cada configuração pode enviar. A documentação já informa que servidores online veem IP, dados de rede e o número reportado.

## Objetivos

- Listar consultas e serviços de denúncia habilitados, seus domínios e finalidade.
- Incluir ações HTTP configuradas em workflows, além das APIs de consulta e denúncia.
- Explicar quais campos de número/mensagem podem ser enviados conforme ações configuradas.
- Levar diretamente à configuração da integração para revisar ou desativá-la.

## Fora de escopo

- Coletar ou enviar telemetria nova.
- Capturar histórico de navegação/requisições ou armazenar corpos de rede.
- Alterar configuração, consentimento ou destino automaticamente.

## Abordagens

- **Página de texto estática:** fácil, mas fica desatualizada quando usuário edita endpoints.
- **Tela local derivada das configurações (proposta):** lê APIs e ações cadastradas, sem fazer requisições.
- **Proxy central para registrar tráfego:** adiciona backend e coleta de dados; incompatível com o objetivo de privacidade.

## Proposta

Criar uma seção `Privacidade online` nas configurações que lista cada API de consulta/denúncia e cada ação HTTP de workflow configurada, ligada/desligada, domínio e finalidade. Para ações HTTP, listar nomes de variáveis conhecidas sem exibir seus valores; se não for possível interpretar uma expressão personalizada, marcar essa parte como não inspecionada e dizer que o payload pode conter dados inseridos na regra. Diferenciar consulta automática de denúncia automática/manual/workflow e linkar para editar cada integração.

## Restrições e critérios de aceite

- A tela funciona offline e não modifica nem transmite configurações.
- Não exibir tokens, senhas, headers secretos ou corpos completos.
- Conteúdo acompanha add/edit/disable/delete de uma API após atualização da tela.
- Configurações customizadas listam variáveis reconhecidas e identificam partes não inspecionadas.
- Estados vazios explicam que recursos offline continuam locais.
- Texto evita prometer anonimato e deixa claro quando configuração customizada impede inspeção exata.

## Testes requeridos

Testes unitários de resumo/redação de dados para ações HTTP padrão e customizadas; testes Android para lista vazia, API habilitada/desabilitada e navegação para edição, garantindo que segredos não aparecem.

## Decisões propostas para aprovação

Tela informativa somente local, sem log de tráfego, com redação de segredos; configurações existentes permanecem a autoridade para ativar/desativar serviços.
