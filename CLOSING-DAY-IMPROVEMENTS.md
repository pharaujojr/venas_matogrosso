# Melhorias no Dashboard Closing Day

## Resumo das Alterações

Foram implementadas 3 melhorias principais no dashboard Closing Day:

### 1. Valores nas Barras do Gráfico ✅
- Valores monetários aparecem DENTRO da barra quando há espaço suficiente (cor branca)
- Valores aparecem FORA da barra (do lado direito) quando não há espaço (cor clara)
- Formato: "R$ X.XXX" sem casas decimais
- Fonte: Segoe UI, 600 weight, 12px
- Sistema inteligente que mede o texto e compara com a largura da barra

### 2. Auto-Refresh dos Dados ✅
- Atualização automática a cada 30 segundos
- Mantém os filtros selecionados durante o refresh
- Funciona apenas quando a aba Closing Day está ativa
- Para automaticamente ao trocar de aba
- **Não há notificação visual** - atualiza silenciosamente em background

### 3. Migração para Banco de Dados ✅
- Criada tabela `closing_day_metas` no PostgreSQL
- Metas agora são persistidas no banco ao invés de localStorage
- API REST para gerenciar metas:
  - `GET /api/closing-day/metas` - Buscar todas as metas
  - `PUT /api/closing-day/metas` - Salvar/atualizar metas

## Arquivos Criados

### SQL
- `create-metas-table.sql` - Script para criar a tabela de metas

### Backend (Java)
- `ClosingDayMeta.java` - Entidade JPA
- `ClosingDayMetaRepository.java` - Repository JPA
- `ClosingDayMetaController.java` - Controller REST

### Frontend
- Modificações em `index.html`:
  - Plugin `customLabels` no gráfico de vendedores
  - Funções `startClosingDayAutoRefresh()` e `stopClosingDayAutoRefresh()`
  - `loadMetasFromStorage()` e `saveMetasToStorage()` agora usam API REST

## Como Usar

### 1. Criar a Tabela no Banco
Execute o script SQL:
```bash
psql -U seu_usuario -d seu_banco -f create-metas-table.sql
```

### 2. Reiniciar a Aplicação
```bash
./gradlew bootRun
```

### 3. Testar
1. Acesse o dashboard
2. Abra a aba "Closing Day"
3. Clique em "Configurar Metas"
4. Defina valores para as filiais
5. Salvar - agora vai para o banco!
6. O gráfico de top vendedores mostrará valores nas barras
7. Os dados serão atualizados automaticamente a cada 30 segundos

## Estrutura da Tabela

```sql
closing_day_metas (
    id BIGSERIAL PRIMARY KEY,
    filial_nome VARCHAR(255) NOT NULL UNIQUE,
    meta_valor DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

## Comportamento do Auto-Refresh

- ⏱️ Intervalo: 30 segundos
- 🔄 Mantém: Filtros de data, filiais e vendedores selecionados
- ✅ Ativo: Apenas na aba "Closing Day"
- ⏹️ Pausado: Ao trocar para outra aba
- 🔇 Silencioso: Sem notificações visuais

## Notas Técnicas

### Valores nas Barras
O plugin `customLabels` usa o contexto 2D do canvas para:
1. Medir a largura do texto formatado
2. Comparar com a largura da barra
3. Posicionar adequadamente (dentro/fora)
4. Aplicar cor apropriada (branco/claro)

### Auto-Refresh
- Usa `setInterval()` com 30000ms
- Limpa o intervalo ao trocar de aba
- Chama `loadClosingDayDashboard()` que preserva filtros

### Persistência
- Frontend: `async/await` para chamadas à API
- Backend: Spring Boot REST com JPA
- Banco: PostgreSQL com índice em `filial_nome`

## Troubleshooting

### Metas não aparecem
- Verifique se executou o script SQL
- Confirme que a aplicação reiniciou após adicionar os novos arquivos
- Veja o console do navegador para erros de API

### Auto-refresh não funciona
- Confirme que está na aba "Closing Day"
- Verifique o console do navegador (deve logar a cada 30s)
- Tente recarregar a página

### Valores não aparecem nas barras
- Verifique se há vendedores com vendas no período
- Confirme que o filtro closing=true está aplicado
- Veja o console para erros no Chart.js
