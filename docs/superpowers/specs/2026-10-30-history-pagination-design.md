# Paginação do histórico — desenho

## Intenção

Manter o histórico de chamadas e SMS rápido conforme o banco cresce. Hoje `HistoryViewModel.reload()` chama `HistoryTable.listRecords()`, que materializa todas as linhas antes de aplicar filtros em Kotlin.

## Objetivos

- Ler e renderizar um lote limitado de registros por vez, mantendo a ordem mais recente primeiro.
- Preservar estados de carregamento, erro, vazio, pesquisa e filtros permitido/bloqueado.
- Não apagar nem migrar registros existentes.

## Fora de escopo

- Alterar o formato ou a retenção do histórico.
- Mudar a semântica da busca fuzzy ou os resultados das regras de triagem.
- Adicionar uma dependência de banco de dados.

## Abordagens

- **Carregar tudo e paginar na interface:** mantém consultas simples, mas não reduz leitura nem memória; rejeitada.
- **Paginação por offset no SQLite:** simples, mas páginas profundas ficam mais caras e podem deslocar linhas entre leituras novas.
- **Paginação por cursor em SQLite (proposta):** usar a ordenação estável por `(time DESC, id DESC)` e buscar a próxima página antes do último par visto. Evita offsets crescentes e mantém desempate determinístico.

## Proposta

Adicionar paginação na camada `HistoryTable`/`BasicTable` e consumir lotes no `HistoryViewModel`. Começar com 50 registros por lote; permitir carregar o próximo lote ao aproximar-se do fim da lista. A pesquisa existente deve continuar encontrando registros antigos: durante pesquisa, varrer lotes em segundo plano até esgotar resultados ou cancelar a consulta quando o texto/filtros mudarem. Executar consultas e conversão de cursor fora da thread principal.

Não alterar esquema inicialmente. Avaliar o plano de consulta com volume alto; criar índice por tempo apenas se necessário e por migração aditiva.

## Restrições e critérios de aceite

- Chamada e SMS mantêm ordenação correta quando vários registros têm o mesmo timestamp.
- Lista inicial contém no máximo 50 registros; buscar mais não duplica nem perde registros.
- Pesquisa retorna correspondências em páginas antigas e cancelar/trocar a pesquisa descarta resultados obsoletos.
- Estado vazio só aparece após concluir a carga; erro mantém ação de tentar novamente.
- Importar/receber registros novos e recarregar não deixa a paginação presa a um cursor inválido.

## Testes requeridos

Testes JVM de cursor, empate de timestamp e filtros; testes Android de primeira página, carregar mais, busca em página antiga, atualização durante paginação e estados vazio/erro.

## Decisões propostas para aprovação

Lote inicial de 50; cursor composto por timestamp e ID; pesquisa global busca em lotes; nenhuma migração sem evidência de gargalo.
