-- Script para atualizar vendas existentes com produto "Não Especificado"
-- Execute este script no banco de dados dbsolturi
-- Assume que o produto "Não Especificado" já foi criado

DO $$
DECLARE
    produto_id INTEGER;
    vendas_atualizadas INTEGER;
BEGIN
    -- 1. Buscar o ID do produto "Não Especificado"
    SELECT id INTO produto_id 
    FROM matogrosso_produtos 
    WHERE descricao = 'Não Especificado' 
    LIMIT 1;
    
    IF produto_id IS NULL THEN
        RAISE EXCEPTION 'Produto "Não Especificado" não encontrado! Execute primeiro: INSERT INTO matogrosso_produtos (descricao, grupo, unidade, created_at) VALUES (''Não Especificado'', ''Não Especificado'', ''UN'', NOW());';
    END IF;

    RAISE NOTICE 'Produto Não Especificado encontrado com ID: %', produto_id;

    -- 2. Atualizar TODAS as vendas para terem o produto "Não Especificado"
    -- O valor de venda do produto será o valor_debito da venda
    -- O valor de custo será 0
    UPDATE financeiro_clientes
    SET produto = jsonb_build_array(
        jsonb_build_object(
            'produtoId', produto_id,
            'quantidade', 1,
            'nomeProduto', 'Não Especificado',
            'valorUnitarioVenda', COALESCE(valor_debito, 0),
            'valorUnitarioCusto', 0
        )
    );

    GET DIAGNOSTICS vendas_atualizadas = ROW_COUNT;
    
    RAISE NOTICE 'Total de % vendas atualizadas com sucesso!', vendas_atualizadas;
END $$;

-- 3. Verificar as atualizações
SELECT id, nome, valor_debito, valor_custo, produto 
FROM financeiro_clientes 
WHERE produto::text LIKE '%Não Especificado%'
ORDER BY id DESC
LIMIT 10;
