# Vendas Mato Grosso - Sistema Multi-Filial

Sistema de gerenciamento de vendas e clientes para múltiplas filiais no Mato Grosso.

## 🚀 Tecnologias

- **Java 17+**
- **Spring Boot 3.x**
- **PostgreSQL**
- **Gradle**
- **Docker**

## 📋 Pré-requisitos

- Java 17 ou superior
- PostgreSQL 12+
- Gradle 7+ (ou use o wrapper incluído)
- Docker (opcional)

## ⚙️ Configuração

### 1. Banco de Dados

Execute o script de migração para criar as tabelas necessárias:

```bash
psql -h SEU_HOST -p SUA_PORTA -U SEU_USUARIO -d SEU_BANCO -f migration-script.sql
```

### 2. Configuração da Aplicação

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://SEU_HOST:PORTA/SEU_BANCO
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
server.port=8787
```

### 3. Executar a Aplicação

#### Usando Gradle Wrapper:
```bash
./gradlew bootRun
```

#### Usando JAR:
```bash
./gradlew build
java -jar build/libs/vendas-jaragua-0.0.1-SNAPSHOT.jar
```

#### Usando Docker:
```bash
docker-compose up -d
```

## 📚 Documentação

Consulte o [MIGRATION-GUIDE.md](MIGRATION-GUIDE.md) para detalhes completos sobre:
- Estrutura das novas tabelas
- Endpoints da API
- Exemplos de uso
- Diferenças entre sistema antigo e novo

## 🔗 API Endpoints

### Base URL
```
http://localhost:8787/api
```

### Principais Endpoints

#### Clientes
- `GET /api/clientes?filial={filial}` - Listar clientes (filial obrigatória)
- `POST /api/clientes` - Criar cliente
- `PUT /api/clientes/{id}` - Atualizar cliente
- `DELETE /api/clientes/{id}` - Deletar cliente

#### Vendedores
- `GET /api/clientes/vendedores?filial={filial}` - Listar vendedores
- `POST /api/clientes/vendedores` - Criar vendedor
- `PUT /api/clientes/vendedores/{id}` - Atualizar vendedor

#### Produtos
- `GET /api/clientes/produtos` - Listar produtos
- `POST /api/clientes/produtos` - Criar produto

#### Dashboard
- `GET /api/clientes/dashboard/stats?filial={filial}` - Estatísticas
- `GET /api/clientes/dashboard/mensal` - Dados mensais
- `GET /api/clientes/filiais` - Listar filiais

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/example/vendasjaragua/
│   │   ├── controller/
│   │   │   ├── ClienteController.java (NOVO - Use este)
│   │   │   └── VendaController.java (Antigo - Mantido)
│   │   ├── model/
│   │   │   ├── Cliente.java (NOVO)
│   │   │   ├── VendedorMatoGrosso.java (NOVO)
│   │   │   ├── ProdutoMatoGrosso.java (NOVO)
│   │   │   ├── Venda.java (Antigo)
│   │   │   ├── Vendedor.java (Antigo)
│   │   │   └── Produto.java (Antigo)
│   │   ├── repository/
│   │   │   ├── ClienteRepository.java (NOVO)
│   │   │   ├── VendedorMatoGrossoRepository.java (NOVO)
│   │   │   └── ProdutoMatoGrossoRepository.java (NOVO)
│   │   └── service/
│   └── resources/
│       ├── application.properties
│       └── static/
migration-script.sql (Execute manualmente no banco)
```

## 🔑 Principais Mudanças

### Sistema Multi-Filial
- Filtro por filial obrigatório em todas as listagens
- Cada vendedor pertence a uma filial específica
- Dashboard com estatísticas por filial

### Vendedores
- ❌ Não usa mais "Times"
- ✅ Usa "Filial"
- ✅ Campo "ativo" para controle

### Estrutura de Dados
- Tabela principal: `financeiro_clientes`
- Controle financeiro: `valor_debito`, `valor_pago`, `saldo`
- Suporte a múltiplas formas de pagamento

## 📖 Exemplo de Uso

### Criar Cliente
```bash
curl -X POST http://localhost:8787/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Empresa ABC Ltda",
    "cpfCnpj": "12.345.678/0001-90",
    "filial": "CUIABA",
    "telefone": "(65) 3333-4444",
    "vendedor": "João Silva",
    "valorDebito": 50000.00,
    "valorPago": 0,
    "ganho": true
  }'
```

### Listar Clientes de uma Filial
```bash
curl "http://localhost:8787/api/clientes?filial=CUIABA&page=0&size=50"
```

### Buscar Estatísticas
```bash
curl "http://localhost:8787/api/clientes/dashboard/stats?filial=CUIABA"
```

## 🔧 Desenvolvimento

### Build
```bash
./gradlew build
```

### Testes
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean build
```

## 📦 Deploy

### Docker
```bash
docker build -t vendas-matogrosso .
docker run -p 8787:8787 vendas-matogrosso
```

## 🤝 Contribuindo

1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📝 Licença

Propriedade de [Sua Empresa]

## 📞 Contato

Para dúvidas ou suporte, entre em contato.

---

**Última Atualização:** 23 de Janeiro de 2026  
**Versão:** 2.0 (Sistema Multi-Filial)  
**Repositório:** https://github.com/pharaujojr/venas_matogrosso.git
