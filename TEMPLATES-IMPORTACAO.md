# Templates de Importação - Sistema Mato Grosso

## 📋 Ordem de Importação

Execute os passos na seguinte ordem:

1. **Cadastrar Filiais no Banco** (via SQL)
2. Importar Vendedores (via Excel)
3. Importar Produtos (via Excel)

---

## 1️⃣ Cadastro de Filiais (SQL)

Execute o script `setup-filiais.sql` no banco de dados PostgreSQL:

```bash
psql -h 192.168.0.162 -p 8449 -U solturi -d dbsolturi -f setup-filiais.sql
```

**Filiais Cadastradas:**
- LUCAS_DO_RIO_VERDE (Código: LRV)
- MATUPA (Código: MTP)
- SINOP (Código: SNP)
- SORRISO (Código: SRR)

---

## 2️⃣ Template de Importação - Vendedores

**Endpoint:** `/api/vendas/vendedores-mg/upload`

**Formato do Arquivo Excel (.xlsx):**

| NOME | FILIAL | EMAIL | TELEFONE | ATIVO |
|------|--------|-------|----------|-------|
| João Silva | SINOP | joao@email.com | (66) 99999-9999 | sim |
| Maria Santos | LUCAS_DO_RIO_VERDE | maria@email.com | (66) 98888-8888 | sim |
| Carlos Souza | MATUPA | carlos@email.com | (66) 97777-7777 | não |
| Ana Costa | SORRISO | ana@email.com | (66) 96666-6666 | sim |

**Regras:**
- **NOME:** Obrigatório
- **FILIAL:** Obrigatório - Deve ser exatamente um dos seguintes valores:
  - LUCAS_DO_RIO_VERDE
  - MATUPA
  - SINOP
  - SORRISO
- **EMAIL:** Opcional
- **TELEFONE:** Opcional
- **ATIVO:** Opcional (valores aceitos: "sim", "true", "ativo" ou vazio = ativo; qualquer outro = inativo)
- **Primeira linha:** Cabeçalho (será ignorada)
- **Se a filial não existir no banco, a importação falhará com erro**

---

## 3️⃣ Template de Importação - Produtos

**Endpoint:** `/api/vendas/produtos-mg/upload`

**Formato do Arquivo Excel (.xlsx):**

| DESCRIÇÃO | GRUPO | UNIDADE |
|-----------|-------|---------|
| Painel Solar 550W | PAINÉIS | UN |
| Inversor Fronius 15kW | INVERSORES | UN |
| Cabo Solar 6mm | CABOS | M |
| Estrutura Metálica | ESTRUTURAS | KG |

**Regras:**
- **DESCRIÇÃO:** Obrigatório
- **GRUPO:** Obrigatório
- **UNIDADE:** Obrigatório (ex: UN, M, KG, CX, etc.)
- **Primeira linha:** Cabeçalho (será ignorada)

---

## 4️⃣ Template de Importação - Vendas (Financeiro Clientes)

**Endpoint:** `/api/vendas/upload`

**Formato do Arquivo Excel (.xlsx):**

As vendas continuam sendo importadas da mesma forma, usando a tabela `financeiro_clientes`.

---

## 🔄 Mudanças Importantes

### Antes (Sistema Jaraguá):
- Usava conceito de **Times** (liderados por uma pessoa)
- Vendedores pertenciam a Times

### Agora (Sistema Mato Grosso):
- Usa conceito de **Filiais** (unidades da empresa)
- Vendedores pertencem a Filiais
- 4 Filiais: Lucas do Rio Verde, Matupá, Sinop, Sorriso
- Produtos e Vendedores são separados por filial

---

## 📊 Endpoints Disponíveis

### Filiais
- `GET /api/vendas/filiais` - Listar todas as filiais
- `GET /api/vendas/filiais?apenasAtivas=true` - Apenas ativas
- `POST /api/vendas/filiais` - Criar filial
- `PUT /api/vendas/filiais/{id}` - Atualizar filial
- `DELETE /api/vendas/filiais/{id}` - Deletar filial

### Vendedores (Mato Grosso)
- `GET /api/vendas/vendedores-mg` - Listar todos
- `GET /api/vendas/vendedores-mg?filial=SINOP` - Filtrar por filial
- `GET /api/vendas/vendedores-mg?apenasAtivos=true` - Apenas ativos
- `POST /api/vendas/vendedores-mg` - Criar vendedor
- `PUT /api/vendas/vendedores-mg/{id}` - Atualizar vendedor
- `DELETE /api/vendas/vendedores-mg/{id}` - Deletar vendedor
- `POST /api/vendas/vendedores-mg/upload` - Importar Excel

### Produtos (Mato Grosso)
- `GET /api/vendas/produtos-mg` - Listar todos
- `POST /api/vendas/produtos-mg` - Criar produto
- `PUT /api/vendas/produtos-mg/{id}` - Atualizar produto
- `DELETE /api/vendas/produtos-mg/{id}` - Deletar produto
- `POST /api/vendas/produtos-mg/upload` - Importar Excel

### Grupos (Mato Grosso)
- `GET /api/vendas/grupos-mg` - Listar grupos distintos

---

## ⚠️ Observações Importantes

1. **Cadastre as filiais PRIMEIRO** executando o script SQL
2. A coluna FILIAL no Excel deve ter exatamente o mesmo nome cadastrado no banco
3. Nomes de filiais são case-sensitive (maiúsculas/minúsculas importam)
4. Não adicione espaços extras nos nomes das filiais

---

## 🐛 Resolução de Problemas

### Erro: "Filial 'XXX' não encontrada"
- **Causa:** A filial não existe no banco de dados
- **Solução:** Execute o script `setup-filiais.sql` primeiro

### Erro: "Vendedor não possui filial informada"
- **Causa:** Coluna FILIAL vazia no Excel
- **Solução:** Preencha a coluna FILIAL com uma das 4 filiais válidas

### Erro ao importar Excel
- **Causa:** Formato incorreto ou campos obrigatórios vazios
- **Solução:** Verifique se o Excel segue exatamente o template acima

---

**Data:** 23 de janeiro de 2025  
**Versão:** 2.0 - Sistema Mato Grosso
