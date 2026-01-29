-- Criar tabela para armazenar metas do Closing Day
CREATE TABLE IF NOT EXISTS closing_day_metas (
    id BIGSERIAL PRIMARY KEY,
    filial_nome VARCHAR(255) NOT NULL UNIQUE,
    meta_valor DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar índice para melhorar performance de buscas
CREATE INDEX idx_closing_metas_filial ON closing_day_metas(filial_nome);

-- Comentários
COMMENT ON TABLE closing_day_metas IS 'Armazena metas de vendas por filial para o dashboard Closing Day';
COMMENT ON COLUMN closing_day_metas.filial_nome IS 'Nome da filial';
COMMENT ON COLUMN closing_day_metas.meta_valor IS 'Valor da meta em reais';
COMMENT ON COLUMN closing_day_metas.data_criacao IS 'Data de criação do registro';
COMMENT ON COLUMN closing_day_metas.data_atualizacao IS 'Data da última atualização';
