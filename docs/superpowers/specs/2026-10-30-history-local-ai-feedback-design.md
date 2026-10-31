# Treinamento local a partir do histórico — desenho

## Intenção

Deixar o usuário corrigir uma classificação de SMS sem sair do histórico e reaproveitar o treinamento bayesiano que já existe no dispositivo.

## Objetivos

- Oferecer ações explícitas `Marcar como spam` e `Marcar como legítima` para SMS com conteúdo disponível.
- Persistir a amostra no `BayesTable`, reconstruir o modelo local e atualizar a classificação futura.
- Evitar amostras duplicadas e permitir desfazer uma correção recente.

## Fora de escopo

- Treinar com chamadas ou texto enviado a servidor.
- Alterar mensagens no app SMS ou apagá-las.
- Retreinar automaticamente usando mensagens sem confirmação do usuário.

## Abordagens

- **Só mudar o resultado no histórico:** não melhora triagem futura; rejeitada.
- **Adicionar amostra Bayes e reconstruir modelo (proposta):** reutiliza `BayesTable`, hash de amostra e `ComplementNaiveBayes` já existentes.
- **Criar uma fila de revisão separada:** aumenta telas e estado sem necessidade para uma correção pontual; rejeitada.

## Proposta

Adicionar ações ao menu contextual do registro SMS. Exibir as ações somente quando `extraInfo` contém o texto da mensagem; quando conteúdo não foi armazenado, explicar que a configuração de registro de conteúdo precisa estar ativa para correções futuras. Usar `record.time` e o conteúdo para calcular a identidade da amostra. Inserir/atualizar a classe confirmada, reconstruir o modelo com os exemplos salvos e gravá-lo localmente. Oferecer desfazer via snackbar enquanto a ação puder ser revertida com segurança.

## Restrições e critérios de aceite

- Nenhum conteúdo sai do aparelho.
- Repetir a mesma correção não duplica amostras; corrigir a classe atualiza a amostra correspondente.
- Exibir as ações somente com Local AI ativo; não começar a guardar exemplos de treino sem essa escolha.
- Desfazer restaura o rótulo anterior quando a amostra já existia e remove a amostra se a correção a criou.
- Falha ao gravar ou reconstruir o modelo não deve deixar DB e arquivo do modelo divergentes silenciosamente.
- Ocultar/desabilitar ação quando não há conteúdo da mensagem; não inferir o texto por acesso extra a SMS.
- Atualizar contagens e probabilidade exibidas sem reiniciar o app.

## Testes requeridos

Testes instrumentados para inserção/duplicação/mudança de rótulo, persistência do modelo e desfazer; testes Android de correção, estado sem conteúdo, Local AI desligado e privacidade.

## Decisões propostas para aprovação

Usar apenas texto já presente em `HistoryRecord.extraInfo`; sem nova permissão; confirmação do usuário obrigatória; nenhuma chamada de rede.
