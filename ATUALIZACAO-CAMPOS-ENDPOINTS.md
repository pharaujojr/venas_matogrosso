# Atualização dos Campos e Novos Endpoints

## 📋 Resumo das Alterações

### 1. Campos Adicionados à Tabela `financeiro_clientes`

Script SQL criado: [migration-add-fields.sql](migration-add-fields.sql)

**Novos campos adicionados:**
- `ov` (VARCHAR) - Ordem de Venda
- `entrega` (VARCHAR) - Informação de entrega
- `cidade` (VARCHAR) - Cidade do cliente
- `estado` (VARCHAR) - Estado (UF)
- `placas` (VARCHAR) - Informações sobre placas solares
- `inversor` (VARCHAR) - Tipo/modelo do inversor
- `potencia` (VARCHAR) - Potência do sistema
- `produto` (JSONB) - Lista de produtos
- `inverter_info` (JSONB) - Informações dos inversores

**Índices criados:**
- `idx_clientes_cidade` - Para buscas por cidade
- `idx_clientes_estado` - Para buscas por estado
- `idx_clientes_ov` - Para buscas por OV
- `idx_clientes_produto_gin` - Índice GIN para campo JSONB produto
- `idx_clientes_inverter_info_gin` - Índice GIN para campo JSONB inverter_info

### 2. Modelo Java Atualizado

**[Venda.java](src/main/java/com/example/vendasjaragua/model/Venda.java)**

Removidos todos os `@Transient`, campos agora persistem no banco:
- ✅ `ov` - Agora persiste
- ✅ `entrega` - Agora persiste
- ✅ `cidade` - Agora persiste
- ✅ `estado` - Agora persiste
- ✅ `placas` - Agora persiste
- ✅ `inversor` - Agora persiste
- ✅ `potencia` - Agora persiste
- ✅ `produto` - Agora persiste (JSONB)
- ✅ `inverterInfo` - Agora persiste (JSONB)

### 3. Novos Endpoints - API REST

#### **Vendedores Mato Grosso** (`/api/vendas/vendedores-mg`)

| Método | Endpoint | Descrição | Parâmetros |
|--------|----------|-----------|------------|
| GET | `/api/vendas/vendedores-mg` | Listar vendedores | `?filial={filial}&apenasAtivos={true/false}` |
| POST | `/api/vendas/vendedores-mg` | Criar vendedor | Body: VendedorMatoGrosso JSON |
| PUT | `/api/vendas/vendedores-mg/{id}` | Atualizar vendedor | {id} + Body: VendedorMatoGrosso JSON |
| DELETE | `/api/vendas/vendedores-mg/{id}` | Deletar vendedor | {id} |

**Exemplo de corpo JSON para criar vendedor:**
```json
{
  "nome": "João Silva",
  "filial": "CUIABA",
  "ativo": true,
  "email": "joao@email.com",
  "telefone": "(65) 99999-9999"
}
```

#### **Produtos Mato Grosso** (`/api/vendas/produtos-mg`)

| Método | Endpoint | Descrição | Parâmetros |
|--------|----------|-----------|------------|
| GET | `/api/vendas/produtos-mg` | Listar produtos | `?grupo={grupo}` |
| POST | `/api/vendas/produtos-mg` | Criar produto | Body: ProdutoMatoGrosso JSON |
| PUT | `/api/vendas/produtos-mg/{id}` | Atualizar produto | {id} + Body: ProdutoMatoGrosso JSON |
| DELETE | `/api/vendas/produtos-mg/{id}` | Deletar produto | {id} |
| GET | `/api/vendas/grupos-mg` | Listar grupos de produtos | - |

**Exemplo de corpo JSON para criar produto:**
```json
{
  "descricao": "Painel Solar 550W",
  "grupo": "PAINEIS",
  "unidade": "UN"
}
```

### 4. Endpoints Antigos Mantidos

Os endpoints antigos foram mantidos para compatibilidade:
- `/api/vendas/vendedores` - Tabela antiga `jaragua_vendedor`
- `/api/vendas/produtos` - Tabela antiga `jaragua_produtos`
- `/api/vendas/times` - Tabela antiga `jaragua_time`

### 5. Query de Busca Aprimorada

A query `searchVendas` agora busca também por:
- `ov` - Ordem de Venda
- `cidade` - Cidade do cliente

Campos de busca disponíveis:
- Cliente (nome)
- Vendedor
- NF/CPF/CNPJ
- OV (Ordem de Venda)
- Telefone
- Cidade
- Email

## 🚀 Como Usar

### 1. Executar Script SQL

```bash
psql -h 192.168.0.162 -p 8449 -U solturi -d dbsolturi -f migration-add-fields.sql
```

### 2. Reiniciar a Aplicação

```bash
./gradlew bootRun
```

### 3. Testar Novos Endpoints

**Listar vendedores de uma filial:**
```bash
curl "http://localhost:8787/api/vendas/vendedores-mg?filial=CUIABA"
```

**Listar produtos de um grupo:**
```bash
curl "http://localhost:8787/api/vendas/produtos-mg?grupo=PAINEIS"
```

**Criar novo vendedor:**
```bash
curl -X POST http://localhost:8787/api/vendas/vendedores-mg \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos",
    "filial": "RONDONOPOLIS",
    "ativo": true,
    "email": "maria@email.com"
  }'
```

**Criar novo produto:**
```bash
curl -X POST http://localhost:8787/api/vendas/produtos-mg \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Inversor 10kW",
    "grupo": "INVERSORES",
    "unidade": "UN"
  }'
```

**Buscar vendas/clientes:**
```bash
# Buscar por cidade
curl "http://localhost:8787/api/vendas?search=CUIABA"

# Buscar por OV
curl "http://localhost:8787/api/vendas?search=OV-12345"
```

## 📊 Estrutura de Tabelas

### Tabela `matogrosso_vendedores`
```
id              BIGSERIAL
nome            VARCHAR(255) NOT NULL
filial          VARCHAR(50) NOT NULL
ativo           BOOLEAN DEFAULT true
email           VARCHAR(255)
telefone        VARCHAR(50)
created_at      TIMESTAMP
updated_at      TIMESTAMP
```

### Tabela `matogrosso_produtos`
```
id              BIGSERIAL
descricao       VARCHAR(255) NOT NULL
grupo           VARCHAR(100)
unidade         VARCHAR(50)
created_at      TIMESTAMP
```

### Tabela `financeiro_clientes` (atualizada)
```
[Campos anteriores...]
+ ov            VARCHAR(255)
+ entrega       VARCHAR(255)
+ cidade        VARCHAR(255)
+ estado        VARCHAR(50)
+ placas        VARCHAR(255)
+ inversor      VARCHAR(255)
+ potencia      VARCHAR(255)
+ produto       JSONB
+ inverter_info JSONB
```

## ✅ Status da Migração

- ✅ Script SQL criado
- ✅ Modelo Java atualizado (campos não são mais @Transient)
- ✅ Endpoints para vendedores Mato Grosso criados
- ✅ Endpoints para produtos Mato Grosso criados
- ✅ Query de busca aprimorada
- ✅ Compilação bem-sucedida
- ✅ Índices de performance criados

## 🔄 Próximos Passos

1. **Executar** o script `migration-add-fields.sql` no banco de dados
2. **Reiniciar** a aplicação
3. **Testar** os novos endpoints
4. **Popular** as tabelas matogrosso_vendedores e matogrosso_produtos
5. **Atualizar** o frontend para usar os novos endpoints `-mg`

## 📝 Notas Importantes

- Os endpoints antigos (`/vendedores`, `/produtos`) continuam funcionando
- Use os endpoints `-mg` para as novas tabelas multi-filial
- Os campos JSONB (`produto`, `inverter_info`) permitem estruturas complexas
- Índices GIN nos campos JSONB melhoram performance de buscas
- Filtros por filial estão disponíveis nos novos endpoints

---

**Data:** 23 de Janeiro de 2026  
**Status:** ✅ Pronto para uso  
**Versão:** 2.1 - Multi-filial com novos campos
