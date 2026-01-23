-- ============================================
-- Script para Adicionar Campos na Tabela financeiro_clientes
-- ============================================
-- Este script adiciona os campos que estavam @Transient
-- EXECUTE MANUALMENTE NO BANCO DE DADOS

-- ============================================
-- 1. ADICIONAR NOVOS CAMPOS
-- ============================================

-- Campo OV (Ordem de Venda)
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS ov VARCHAR(255);

-- Campo Entrega
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS entrega VARCHAR(255);

-- Campo Cidade
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS cidade VARCHAR(255);

-- Campo Estado
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS estado VARCHAR(50);

-- Campo Placas (painéis solares)
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS placas VARCHAR(255);

-- Campo Inversor
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS inversor VARCHAR(255);

-- Campo Potência
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS potencia VARCHAR(255);

-- Campo Produto (JSONB para lista de produtos)
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS produto JSONB;

-- Campo Inverter Info (JSONB para lista de inversores)
ALTER TABLE financeiro_clientes 
ADD COLUMN IF NOT EXISTS inverter_info JSONB;

-- ============================================
-- 2. CRIAR ÍNDICES PARA MELHOR PERFORMANCE
-- ============================================

CREATE INDEX IF NOT EXISTS idx_clientes_cidade ON financeiro_clientes(cidade);
CREATE INDEX IF NOT EXISTS idx_clientes_estado ON financeiro_clientes(estado);
CREATE INDEX IF NOT EXISTS idx_clientes_ov ON financeiro_clientes(ov);

-- Índices GIN para campos JSONB (melhor performance em buscas)
CREATE INDEX IF NOT EXISTS idx_clientes_produto_gin ON financeiro_clientes USING GIN(produto);
CREATE INDEX IF NOT EXISTS idx_clientes_inverter_info_gin ON financeiro_clientes USING GIN(inverter_info);

-- ============================================
-- 3. COMENTÁRIOS NOS CAMPOS
-- ============================================

COMMENT ON COLUMN financeiro_clientes.ov IS 'Ordem de Venda';
COMMENT ON COLUMN financeiro_clientes.entrega IS 'Informação de entrega (data ou texto)';
COMMENT ON COLUMN financeiro_clientes.cidade IS 'Cidade do cliente';
COMMENT ON COLUMN financeiro_clientes.estado IS 'Estado do cliente (UF)';
COMMENT ON COLUMN financeiro_clientes.placas IS 'Informação sobre placas solares';
COMMENT ON COLUMN financeiro_clientes.inversor IS 'Tipo/modelo do inversor';
COMMENT ON COLUMN financeiro_clientes.potencia IS 'Potência do sistema';
COMMENT ON COLUMN financeiro_clientes.produto IS 'Lista de produtos em formato JSON';
COMMENT ON COLUMN financeiro_clientes.inverter_info IS 'Informações detalhadas dos inversores em formato JSON';

-- ============================================
-- 4. QUERY DE VERIFICAÇÃO
-- ============================================

-- Verificar estrutura da tabela
-- SELECT column_name, data_type, character_maximum_length 
-- FROM information_schema.columns 
-- WHERE table_name = 'financeiro_clientes' 
-- ORDER BY ordinal_position;

-- ============================================
-- FIM DO SCRIPT
-- ============================================
