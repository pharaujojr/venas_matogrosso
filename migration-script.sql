-- ============================================
-- Script de Migração - Vendas Mato Grosso
-- ============================================
-- Este script cria as novas tabelas para o sistema multi-filial
-- EXECUTE MANUALMENTE NO BANCO DE DADOS

-- ============================================
-- 1. CRIAR TABELA DE PRODUTOS (Mato Grosso)
-- ============================================
CREATE TABLE IF NOT EXISTS matogrosso_produtos (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    grupo VARCHAR(100),
    unidade VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_produtos_descricao ON matogrosso_produtos(descricao);
CREATE INDEX idx_produtos_grupo ON matogrosso_produtos(grupo);

COMMENT ON TABLE matogrosso_produtos IS 'Tabela de produtos para o sistema Mato Grosso multi-filial';
COMMENT ON COLUMN matogrosso_produtos.descricao IS 'Descrição do produto';
COMMENT ON COLUMN matogrosso_produtos.grupo IS 'Grupo/categoria do produto';
COMMENT ON COLUMN matogrosso_produtos.unidade IS 'Unidade de medida (UN, KG, M, etc)';

-- ============================================
-- 2. CRIAR TABELA DE VENDEDORES (Mato Grosso)
-- ============================================
CREATE TABLE IF NOT EXISTS matogrosso_vendedores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    filial VARCHAR(50) NOT NULL,
    ativo BOOLEAN DEFAULT true,
    email VARCHAR(255),
    telefone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vendedores_nome ON matogrosso_vendedores(nome);
CREATE INDEX idx_vendedores_filial ON matogrosso_vendedores(filial);
CREATE INDEX idx_vendedores_ativo ON matogrosso_vendedores(ativo);

COMMENT ON TABLE matogrosso_vendedores IS 'Tabela de vendedores com controle por filial';
COMMENT ON COLUMN matogrosso_vendedores.nome IS 'Nome do vendedor';
COMMENT ON COLUMN matogrosso_vendedores.filial IS 'Filial à qual o vendedor pertence';
COMMENT ON COLUMN matogrosso_vendedores.ativo IS 'Se o vendedor está ativo no sistema';

-- ============================================
-- 3. CRIAR FUNÇÃO PARA ATUALIZAR updated_at
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger na tabela de vendedores
DROP TRIGGER IF EXISTS update_vendedores_updated_at ON matogrosso_vendedores;
CREATE TRIGGER update_vendedores_updated_at
    BEFORE UPDATE ON matogrosso_vendedores
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 4. VERIFICAR TABELA FINANCEIRO_CLIENTES
-- ============================================
-- A tabela financeiro_clientes já existe no banco
-- Validando sua estrutura (comentário informativo)

-- Estrutura esperada:
-- - id (BIGSERIAL PRIMARY KEY)
-- - nome (VARCHAR(255) NOT NULL)
-- - data (DATE)
-- - email (VARCHAR(255))
-- - telefone (VARCHAR(50))
-- - cpf_cnpj (VARCHAR(20))
-- - filial (VARCHAR(50) NOT NULL)
-- - valor_debito (NUMERIC(19,2) NOT NULL DEFAULT 0)
-- - valor_pago (NUMERIC(19,2) NOT NULL DEFAULT 0)
-- - data_cadastro (TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)
-- - vendedor (VARCHAR(25))
-- - observacao (TEXT)
-- - forma_pagamento (VARCHAR(50))
-- - ganho (BOOLEAN DEFAULT false)

-- ============================================
-- 5. INSERIR DADOS EXEMPLO (OPCIONAL)
-- ============================================
-- Exemplo de filiais típicas (descomente se necessário)

-- INSERT INTO matogrosso_vendedores (nome, filial, ativo) VALUES
-- ('Vendedor Cuiabá 1', 'CUIABA', true),
-- ('Vendedor Várzea Grande 1', 'VARZEA_GRANDE', true),
-- ('Vendedor Rondonópolis 1', 'RONDONOPOLIS', true);

-- INSERT INTO matogrosso_produtos (descricao, grupo, unidade) VALUES
-- ('Painel Solar 550W', 'PAINEIS', 'UN'),
-- ('Inversor 5kW', 'INVERSORES', 'UN'),
-- ('String Box', 'PROTECAO', 'UN'),
-- ('Cabo Solar 6mm', 'CABEAMENTO', 'M');

-- ============================================
-- 6. QUERIES DE VERIFICAÇÃO
-- ============================================
-- Execute estas queries para validar as tabelas criadas:

-- SELECT COUNT(*) as total_produtos FROM matogrosso_produtos;
-- SELECT COUNT(*) as total_vendedores FROM matogrosso_vendedores;
-- SELECT DISTINCT filial FROM financeiro_clientes ORDER BY filial;
-- SELECT DISTINCT filial FROM matogrosso_vendedores ORDER BY filial;

-- ============================================
-- 7. MANTER TABELAS ANTIGAS (NÃO DELETAR)
-- ============================================
-- As tabelas abaixo são mantidas para referência histórica:
-- - vendas_jaragua (tabela antiga de vendas)
-- - jaragua_vendedor (tabela antiga de vendedores)
-- - jaragua_time (tabela antiga de times - não será mais utilizada)
-- - jaragua_produtos (tabela antiga de produtos)
-- - financeiro_pagamentos (será deprecated em breve)

-- NÃO EXECUTE os comandos abaixo:
-- DROP TABLE vendas_jaragua;
-- DROP TABLE jaragua_vendedor;
-- DROP TABLE jaragua_time;
-- DROP TABLE jaragua_produtos;

-- ============================================
-- FIM DO SCRIPT
-- ============================================
