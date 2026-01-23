-- ============================================
-- Script para Criar Tabela de Filiais e Cadastrar as 4 Filiais
-- ============================================
-- Execute este script ANTES de importar vendedores e produtos

-- ============================================
-- 1. CRIAR TABELA DE FILIAIS
-- ============================================

CREATE TABLE IF NOT EXISTS matogrosso_filiais (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    codigo VARCHAR(50),
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar índice no nome para melhor performance nas buscas
CREATE INDEX IF NOT EXISTS idx_filiais_nome ON matogrosso_filiais(nome);
CREATE INDEX IF NOT EXISTS idx_filiais_ativo ON matogrosso_filiais(ativo);

-- ============================================
-- 2. CADASTRAR AS 4 FILIAIS
-- ============================================

INSERT INTO matogrosso_filiais (nome, codigo, ativo) 
VALUES
    ('LUCAS_DO_RIO_VERDE', 'LRV', true),
    ('MATUPA', 'MTP', true),
    ('SINOP', 'SNP', true),
    ('SORRISO', 'SRR', true)
ON CONFLICT (nome) DO NOTHING;

-- ============================================
-- 3. ATUALIZAR TABELA DE VENDEDORES
-- ============================================
-- Adicionar coluna filial_id (relacionamento com filiais)
-- e remover coluna filial (string)

-- Se a tabela já existe e tem coluna "filial" como string, vamos migrar os dados
DO $$
BEGIN
    -- Adicionar coluna filial_id se não existir
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='matogrosso_vendedores' AND column_name='filial_id') THEN
        ALTER TABLE matogrosso_vendedores 
        ADD COLUMN filial_id BIGINT;
        
        -- Criar foreign key
        ALTER TABLE matogrosso_vendedores 
        ADD CONSTRAINT fk_vendedor_filial 
        FOREIGN KEY (filial_id) REFERENCES matogrosso_filiais(id);
        
        -- Criar índice
        CREATE INDEX idx_vendedor_filial ON matogrosso_vendedores(filial_id);
    END IF;
    
    -- Se ainda existe coluna "filial" como string, migrar dados e remover
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='matogrosso_vendedores' AND column_name='filial' AND data_type='character varying') THEN
        
        -- Migrar dados existentes
        UPDATE matogrosso_vendedores v
        SET filial_id = f.id
        FROM matogrosso_filiais f
        WHERE v.filial = f.nome
        AND v.filial_id IS NULL;
        
        -- Remover coluna antiga
        ALTER TABLE matogrosso_vendedores DROP COLUMN filial;
    END IF;
END $$;

-- ============================================
-- 4. VERIFICAÇÃO
-- ============================================

-- Ver as filiais cadastradas
SELECT * FROM matogrosso_filiais ORDER BY nome;

-- Ver estrutura da tabela de vendedores
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'matogrosso_vendedores'
ORDER BY ordinal_position;

-- ============================================
-- 5. EXEMPLOS DE INSERÇÃO MANUAL
-- ============================================

-- Exemplo para inserir vendedor manualmente (após ter as filiais):
-- INSERT INTO matogrosso_vendedores (nome, filial_id, email, telefone, ativo)
-- VALUES ('João Silva', 
--         (SELECT id FROM matogrosso_filiais WHERE nome = 'SINOP'),
--         'joao@email.com',
--         '(66) 99999-9999',
--         true);

-- ============================================
-- FIM DO SCRIPT
-- ============================================
