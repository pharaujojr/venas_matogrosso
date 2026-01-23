-- Script para criar produto "Não Especificado" e atualizar vendas existentes
-- Execute este script no banco de dados dbsolturi

DO $$
DECLARE
    produto_id INTEGER;
BEGIN
    -- 1. Inserir produto "Não Especificado" no grupo "Não Especificado" e capturar o ID
    INSERT INTO matogrosso_produtos (descricao, grupo, unidade, created_at)
    VALUES ('Não Especificado', 'Não Especificado', 'UN', NOW())
    ON CONFLICT (descricao) DO UPDATE SET descricao = EXCLUDED.descricao
    RETURNING id INTO produto_id;
    
    -- Se o produto já existia, buscar o ID
    IF produto_id IS NULL THEN
        SELECT id INTO produto_id FROM matogrosso_produtos WHERE descricao = 'Não Especificado';
    END IF;

    RAISE NOTICE 'Produto Não Especificado criado/encontrado com ID: %', produto_id;

    -- 2. Atualizar TODAS as vendas para terem o produto "Não Especificado"
    UPDATE financeiro_clientes
    SET produto = jsonb_build_array(
        jsonb_build_object(
            'produtoId', produto_id,
            'quantidade', 1,
            'nomeProduto', 'Não Especificado',
            'valorVenda', COALESCE(valor_debito, 0)
        )
    );

    RAISE NOTICE 'Vendas atualizadas com sucesso!';
END $$;

-- 3. Verificar as atualizações
SELECT id, nome, valor_debito, produto 
FROM financeiro_clientes 
WHERE produto::text LIKE '%Não Especificado%'
LIMIT 10;
