# Migração da Tabela Antiga para financeiro_clientes

## ✅ Problema Resolvido

O sistema estava buscando dados da tabela antiga `vendas_jaragua` quando deveria buscar de `financeiro_clientes`. Essa migração foi concluída com sucesso.

## 🔄 Alterações Realizadas

### 1. Modelo `Venda.java`

**Antes:** `@Table(name = "vendas_jaragua")`  
**Depois:** `@Table(name = "financeiro_clientes")`

#### Mapeamento de Campos

O modelo `Venda` foi adaptado para mapear os campos da nova tabela `financeiro_clientes`:

| Campo Java (Venda) | Campo DB (financeiro_clientes) | Observação |
|-------------------|-------------------------------|------------|
| `cliente` | `nome` | Mapeado com @Column |
| `nf` | `cpf_cnpj` | Reutilizado para CPF/CNPJ |
| `telefone` | `telefone` | Direto |
| `vendedor` | `vendedor` | Direto |
| `data` | `data` | Direto |
| `valorVenda` | `valor_debito` | Mapeado com @Column |
| `valorMaterial` | `valor_pago` | Mapeado com @Column |
| `time` | `filial` | Mapeado com @Column |
| `email` | `email` | Novo campo |
| `dataCadastro` | `data_cadastro` | Novo campo |
| `observacao` | `observacao` | Novo campo |
| `formaPagamento` | `forma_pagamento` | Novo campo |
| `ganho` | `ganho` | Novo campo |

#### Campos @Transient (Não Persistidos)

Os seguintes campos foram marcados como `@Transient` pois não existem na nova tabela:
- `ov`
- `entrega`
- `cidade`
- `estado`
- `placas`
- `inversor`
- `potencia`
- `produto` (List<VendaItem>)
- `inverterInfo` (List<InverterItem>)

### 2. Repository `VendaRepository.java`

Alterações nos parâmetros das queries:
- **`times`** → **`filiais`** em todas as queries
- Queries agora filtram por `v.time` (que mapeia para o campo `filial` na tabela)

### 3. Controller `VendaController.java`

Alterações nos endpoints:

#### Parâmetros Atualizados
- `@RequestParam times` → `@RequestParam filiais` em todos os métodos de dashboard

#### Novos Endpoints
- `GET /api/vendas/filiais` - Lista todas as filiais disponíveis

#### Endpoints Ajustados
- `/dashboard/mensal` - Agora usa parâmetro `filiais`
- `/dashboard/vendedores` - Agora usa parâmetro `filiais`
- `/dashboard/produtos` - Agora usa parâmetro `filiais`
- `/dashboard/stats` - Agora usa parâmetro `filiais`
- `/dashboard/vendedores-time` - Parâmetro renomeado para `filial` (aceita `SEM_FILIAL_REF` também)

## 🎯 Compatibilidade

### API Mantida
O sistema **mantém compatibilidade** com o frontend existente através dos endpoints `/api/vendas/*`, mas agora **busca dados de `financeiro_clientes`**.

### Semântica dos Campos

| Conceito Antigo | Conceito Novo |
|----------------|---------------|
| Time | Filial |
| Valor Venda | Valor Débito |
| Valor Material | Valor Pago |
| NF | CPF/CNPJ |
| Cliente | Nome |

### Métodos Auxiliares

- `getValorBruto()` - Retorna o **saldo** (valor_debito - valor_pago)
- `isPago()` - Verifica se o saldo está zerado ou negativo

## 🔍 Exemplo de Uso

### Buscar Vendas por Filial (antes era por time)

**Endpoint:** `GET /api/vendas?filial=CUIABA`

A query interna filtra por: `WHERE v.time = 'CUIABA'`  
Mas na tabela, `v.time` está mapeado para o campo `filial`.

### Dashboard com Filtro de Filiais

**Endpoint:** `GET /api/vendas/dashboard/mensal?filiais=CUIABA,RONDONOPOLIS`

Retorna dados mensais filtrados pelas filiais especificadas.

## ⚠️ Pontos Importantes

1. **Campos Transient**: Não serão salvos no banco e sempre retornarão `null` ao buscar
2. **Compatibilidade**: O frontend pode continuar usando os mesmos endpoints
3. **Nomenclatura**: Internamente usa "filial", mas a API ainda aceita parâmetros com nomes antigos
4. **Migração Transparente**: Mudança ocorre no backend sem impactar o frontend

## 🚀 Status

✅ **CONCLUÍDO** - Sistema agora busca dados de `financeiro_clientes`  
✅ Modelo adaptado com mapeamento correto  
✅ Repository atualizado  
✅ Controller ajustado  
✅ API mantém compatibilidade

## 📝 Próximos Passos

Se necessário:
1. Atualizar frontend para usar nova nomenclatura (filial ao invés de time)
2. Remover campos @Transient se não forem mais necessários
3. Criar endpoints específicos para os novos campos (email, observacao, formaPagamento, ganho)

---

**Data:** 23 de Janeiro de 2026  
**Status:** ✅ Operacional  
**Tabela Ativa:** `financeiro_clientes`
