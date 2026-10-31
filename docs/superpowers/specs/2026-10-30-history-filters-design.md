# Filtros avançados do histórico — desenho

## Intenção

Permitir localizar chamadas ou SMS por período e motivo da decisão sem percorrer toda a lista. O histórico já oferece busca fuzzy e alternância entre permitidos e bloqueados.

## Objetivos

- Acrescentar filtros de período e categoria do resultado sem remover os controles existentes.
- Mostrar claramente quais filtros estão ativos e permitir limpá-los.
- Combinar os filtros com a paginação do histórico.

## Fora de escopo

- Alterar resultados de triagem ou classificação.
- Criar relatórios agregados, exportação ou retenção automática.
- Manter filtros sincronizados entre dispositivos.

## Abordagens

- **Mais botões permanentes na tela:** rápido, mas disputa espaço com lista e controles atuais; rejeitada.
- **Painel/modal de filtros (proposta):** filtros agrupados em um lugar, com resumo compacto dos ativos.
- **Tela de análise separada:** amplia navegação para uma tarefa que pertence à lista; rejeitada.

## Proposta

Adicionar seletor de período com `24 horas`, `7 dias`, `30 dias` e `Tudo`, além de filtro por tipo de resultado (bloqueio por regra, base de spam, IA local, exceção/permitido e falha de consulta, conforme os resultados persistidos existentes). Preservar os toggles permitido/bloqueado e busca textual. Aplicar filtros na consulta paginada sempre que representáveis em SQLite; fazer busca fuzzy sobre lotes retornados sem quebrar a busca atual por nome de contato.

## Restrições e critérios de aceite

- Filtros compõem-se entre si e atualizam a lista e o estado vazio.
- `Tudo` reproduz a abrangência atual; período respeita horário local e limites inclusivos documentados.
- Categorias são derivadas dos códigos de resultado existentes, sem renumerar enums/códigos.
- Limpar filtros restaura a lista; mudanças reiniciam o cursor e cancelam cargas antigas.
- Chamadas e SMS mostram apenas filtros aplicáveis a cada tipo.

## Testes requeridos

Testes JVM para mapeamento de resultado e limites de período; testes Android para combinações, limpeza, estado vazio e interação com paginação.

## Decisões propostas para aprovação

Usar exatamente os quatro períodos listados e um modal/painel compacto; manter a busca fuzzy e os toggles atuais.
