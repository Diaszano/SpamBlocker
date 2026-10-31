# Prévia de importação CSV — desenho

## Intenção

Permitir revisar um arquivo antes de criar uma ou várias regras. Hoje a importação exige a coluna `pattern`, converte as linhas e grava de imediato como regra única ou múltiplas regras.

## Objetivos

- Mostrar resumo do arquivo e do modo escolhido antes de gravar.
- Identificar arquivo sem cabeçalho `pattern`, CSV vazio e linhas sem padrão.
- Excluir padrões regex inválidos segundo a mesma validação do editor de regras e informar a contagem ignorada.
- Manter a escolha atual entre consolidar regras e importar regras individuais.
- Aplicar todas as inserções em uma transação para evitar importação parcial.

## Fora de escopo

- Criar novo formato de backup ou alterar o formato CSV existente.
- Baixar arquivos ou validar conteúdo remoto.
- Reescrever todas as regras existentes para um esquema diferente.

## Abordagens

- **Confirmar apenas contagem total:** rápido, mas não deixa revisar conteúdo.
- **Prévia resumida com amostras e contagens (proposta):** explica o resultado sem renderizar arquivos inteiros.
- **Editor completo de cada regra antes da importação:** caro e redundante com os editores existentes.

## Proposta

Após escolher arquivo, parsear para um resumo e mostrar nome, total de linhas, amostra das primeiras cinco regras, contagens de allow/block e quantidade de linhas ignoradas. Manter apenas a URI e esse resumo na tela; ao confirmar, reler e validar o arquivo. Se a contagem mudou, mostrar a nova prévia e pedir confirmação outra vez. Na importação como regra única, mostrar que padrões serão consolidados; no modo multi, informar quantas regras serão criadas. Manter o comportamento de CSV já aceito; abortar sem gravação se o parser falhar, faltar `pattern` ou não houver nenhuma regra importável. Inserir múltiplas regras em transação e mostrar sucesso/erro com contagem.

## Restrições e critérios de aceite

- Cancelar prévia não grava nada.
- CSV grande não é copiado inteiro para a UI; a prévia mantém apenas contagens e amostras limitadas.
- Conteúdo com aspas, delimitadores, BOM e CRLF preserva o parser atual.
- Cada modo produz exatamente o número anunciado; falha não deixa subset persistido.
- Mensagem diferencia linhas vazias ignoradas de arquivo inválido.
- Se o conteúdo do URI mudar entre prévia e confirmação, exigir nova confirmação antes de importar.

## Testes requeridos

Testes JVM para resumo e validação (vazio, cabeçalho ausente, linhas vazias, modos allow/block); teste de transação/rollback; testes Android para prévia, cancelar, confirmar e erro de leitura.

## Decisões propostas para aprovação

Resumo com até cinco amostras; preservar os dois modos existentes; não inserir regra parcial; sem mudança do formato CSV.
