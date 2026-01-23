# Migração para Sistema Multi-Filial - Mato Grosso

## Resumo das Alterações

Este documento descreve as mudanças realizadas para adaptar o sistema de vendas Jaraguá para um sistema multi-filial Mato Grosso.

## 🏗️ Estrutura do Banco de Dados

### Novas Tabelas Criadas

#### 1. `financeiro_clientes` (Principal)
Substitui a antiga tabela `vendas_jaragua`. Armazena informações de clientes com controle financeiro.

**Campos principais:**
- `filial` (obrigatório) - Identifica a filial do cliente
- `valor_debito` - Valor total devido
- `valor_pago` - Valor já pago
- `ganho` - Indica se é uma venda ganha/realizada
- `vendedor` - Nome do vendedor responsável

#### 2. `matogrosso_vendedores`
Nova tabela de vendedores com controle por filial (substitui `jaragua_vendedor`).

**Diferenças da tabela antiga:**
- ❌ Não usa mais o campo `time` 
- ✅ Usa o campo `filial`
- ✅ Campo `ativo` para controlar vendedores ativos/inativos

#### 3. `matogrosso_produtos`
Nova tabela de produtos (substitui `jaragua_produtos`).

**Estrutura similar, mas independente da tabela antiga.**

### Tabelas Mantidas (Não Utilizar)

As seguintes tabelas foram mantidas para referência histórica, mas **NÃO devem ser usadas**:
- `vendas_jaragua` - Tabela antiga de vendas
- `jaragua_vendedor` - Vendedores antigos
- `jaragua_time` - Times (não será mais utilizado)
- `jaragua_produtos` - Produtos antigos
- `financeiro_pagamentos` - Será deprecated em breve

## 📁 Estrutura do Código Java

### Novos Models

1. **`Cliente.java`** - Representa `financeiro_clientes`
2. **`VendedorMatoGrosso.java`** - Representa `matogrosso_vendedores`
3. **`ProdutoMatoGrosso.java`** - Representa `matogrosso_produtos`

### Novos Repositories

1. **`ClienteRepository.java`** - CRUD e queries de clientes
2. **`VendedorMatoGrossoRepository.java`** - CRUD de vendedores
3. **`ProdutoMatoGrossoRepository.java`** - CRUD de produtos

### Novo Controller

**`ClienteController.java`** - Gerencia toda a API REST para:
- Clientes (CRUD completo)
- Vendedores (CRUD completo)
- Produtos (CRUD completo)
- Dashboard com estatísticas
- Filiais

## 🚀 Endpoints da API

### Base URL: `/api/clientes`

#### Clientes

```
GET    /api/clientes                    - Listar clientes (obrigatório filtro por filial)
GET    /api/clientes/{id}               - Buscar cliente por ID
POST   /api/clientes                    - Criar novo cliente
PUT    /api/clientes/{id}               - Atualizar cliente
DELETE /api/clientes/{id}               - Deletar cliente
```

**Parâmetros de Filtro (GET /api/clientes):**
- `filial` (obrigatório) - Filial a filtrar
- `page` (opcional, default: 0) - Página
- `size` (opcional, default: 50) - Tamanho da página
- `startDate` (opcional) - Data inicial
- `endDate` (opcional) - Data final
- `search` (opcional) - Busca por nome, vendedor, CPF/CNPJ, telefone

#### Vendedores

```
GET    /api/clientes/vendedores         - Listar vendedores
POST   /api/clientes/vendedores         - Criar vendedor
PUT    /api/clientes/vendedores/{id}    - Atualizar vendedor
DELETE /api/clientes/vendedores/{id}    - Deletar vendedor
```

**Parâmetros (GET):**
- `filial` (opcional) - Filtrar por filial
- `apenasAtivos` (opcional, default: true) - Apenas vendedores ativos

#### Produtos

```
GET    /api/clientes/produtos           - Listar produtos
POST   /api/clientes/produtos           - Criar produto
PUT    /api/clientes/produtos/{id}      - Atualizar produto
DELETE /api/clientes/produtos/{id}      - Deletar produto
GET    /api/clientes/grupos             - Listar grupos de produtos
```

#### Filiais

```
GET    /api/clientes/filiais            - Listar todas as filiais distintas
```

#### Dashboard

```
GET    /api/clientes/dashboard/stats         - Estatísticas gerais por filial
GET    /api/clientes/dashboard/mensal        - Dados mensais
GET    /api/clientes/dashboard/vendedores    - Dados por vendedor
```

## 🔑 Mudanças Importantes

### 1. Filtro por Filial Obrigatório

Na listagem principal (`GET /api/clientes`), o parâmetro `filial` é **obrigatório**. Se não for informado, retorna vazio.

**Exemplo:**
```
GET /api/clientes?filial=CUIABA&page=0&size=50
```

### 2. Vendedores Agora Têm Filial

Diferente do sistema antigo que usava "Time", agora cada vendedor pertence a uma **filial**.

**Exemplo de vendedor:**
```json
{
  "nome": "João Silva",
  "filial": "CUIABA",
  "ativo": true,
  "email": "joao@email.com",
  "telefone": "(65) 99999-9999"
}
```

### 3. Estrutura de Cliente

**Exemplo de cliente:**
```json
{
  "nome": "Empresa XYZ Ltda",
  "cpfCnpj": "12.345.678/0001-90",
  "filial": "CUIABA",
  "telefone": "(65) 3333-4444",
  "email": "contato@empresa.com",
  "vendedor": "João Silva",
  "valorDebito": 50000.00,
  "valorPago": 25000.00,
  "data": "2026-01-23",
  "formaPagamento": "Boleto",
  "ganho": true,
  "observacao": "Cliente preferencial"
}
```

## 📋 Script de Migração

Execute o arquivo `migration-script.sql` **manualmente** no banco de dados PostgreSQL para criar as novas tabelas.

```bash
psql -h 192.168.0.162 -p 8449 -U solturi -d dbsolturi -f migration-script.sql
```

## ⚙️ Configuração

A porta da aplicação foi alterada para **8787** em `application.properties`:

```properties
server.port=8787
```

## 🔄 Próximas Etapas

Conforme mencionado, haverão mais etapas posteriormente. O sistema está preparado para:

1. ✅ Trabalhar com múltiplas filiais
2. ✅ Gerenciar vendedores por filial (sem times)
3. ✅ Controlar produtos independentes
4. ✅ Dashboard com filtros por filial
5. ⏳ Integração com sistema de pagamentos (futuro)

## 🎯 Sistema Antigo vs Novo

| Aspecto | Sistema Antigo | Sistema Novo |
|---------|---------------|--------------|
| Tabela Principal | `vendas_jaragua` | `financeiro_clientes` |
| Vendedores | `jaragua_vendedor` com `time` | `matogrosso_vendedores` com `filial` |
| Produtos | `jaragua_produtos` | `matogrosso_produtos` |
| Unidades | Apenas Jaraguá | Multi-filial |
| Times | Sim (campo obrigatório) | Não (removido) |
| Filtro Principal | Por time/vendedor | Por filial (obrigatório) |

## 📝 Observações Finais

- **Não deletar** as tabelas antigas, apenas não utilizá-las
- O controller antigo (`VendaController`) foi mantido, mas deve-se usar o novo (`ClienteController`)
- A tabela `financeiro_pagamentos` não está sendo utilizada nesta etapa
- Todos os endpoints têm validação de filial para garantir isolamento de dados

---

**Data da Migração:** 23 de Janeiro de 2026  
**Versão:** 1.0  
**Sistema:** Vendas Mato Grosso Multi-Filial
