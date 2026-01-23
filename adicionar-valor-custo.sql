-- ============================================
-- Script para Adicionar Coluna valor_custo na Tabela financeiro_clientes
-- ============================================

-- Adicionar coluna valor_custo
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS valor_custo NUMERIC(15, 2);

-- Criar índice para melhor performance
CREATE INDEX IF NOT EXISTS idx_financeiro_valor_custo ON financeiro_clientes(valor_custo);

-- Comentário na coluna para documentação
COMMENT ON COLUMN financeiro_clientes.valor_custo IS 'Valor de custo/material da venda';

-- Verificar se a coluna foi adicionada
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'financeiro_clientes'
AND column_name = 'valor_custo';

-- ============================================
-- FIM DO SCRIPT
-- ============================================
