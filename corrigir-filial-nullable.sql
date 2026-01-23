-- ============================================
-- Script para Tornar a Coluna filial NULLABLE
-- ============================================
-- Permite que vendas sejam criadas sem filial definida

-- Remover a constraint NOT NULL da coluna filial
ALTER TABLE financeiro_clientes 
ALTER COLUMN filial DROP NOT NULL;

-- Verificar a mudança
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'financeiro_clientes'
AND column_name = 'filial';

-- ============================================
-- FIM DO SCRIPT
-- ============================================
