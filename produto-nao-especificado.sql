-- Script para criar produto "Não Especificado" e atualizar vendas existentes
-- Execute este script no banco de dados dbsolturi

-- 1. Inserir produto "Não Especificado" no grupo "Não Especificado"
INSERT INTO matogrosso_produtos (descricao, grupo, unidade, created_at)
VALUES ('Não Especificado', 'Não Especificado', 'UN', NOW())
ON CONFLICT DO NOTHING;

-- 2. Verificar o ID do produto criado (anotar este ID)
SELECT id, descricao, grupo FROM matogrosso_produtos WHERE descricao = 'Não Especificado';

-- 3. Atualizar vendas existentes que têm produto JSON para incluir o produto "Não Especificado"
-- IMPORTANTE: Substitua {PRODUTO_ID} pelo ID retornado no passo 2
-- 
-- Exemplo de estrutura do JSON produto esperada:
-- [{"produtoId": {PRODUTO_ID}, "quantidade": 1, "nomeProduto": "Não Especificado", "valorVenda": VALOR_TOTAL}]
--
-- Como o produto JSON em financeiro_clientes é complexo e varia,
-- este script apenas mostra o conceito. Você precisará ajustar baseado nos dados reais.
--
-- Opção 1: Atualizar vendas sem produto especificado (produto = null ou [])
UPDATE financeiro_clientes
SET produto = jsonb_build_array(
    jsonb_build_object(
        'produtoId', {PRODUTO_ID},  -- SUBSTITUA pelo ID real
        'quantidade', 1,
        'nomeProduto', 'Não Especificado',
        'valorVenda', COALESCE(valor_debito, 0)
    )
)
WHERE produto IS NULL OR produto = '[]'::jsonb OR jsonb_array_length(produto) = 0;

-- Opção 2: Se todas as vendas devem ter o produto "Não Especificado"
-- (descomente se necessário)
-- UPDATE financeiro_clientes
-- SET produto = jsonb_build_array(
--     jsonb_build_object(
--         'produtoId', {PRODUTO_ID},  -- SUBSTITUA pelo ID real
--         'quantidade', 1,
--         'nomeProduto', 'Não Especificado',
--         'valorVenda', COALESCE(valor_debito, 0)
--     )
-- );

-- 4. Verificar as atualizações
SELECT id, nome, valor_debito, produto 
FROM financeiro_clientes 
WHERE produto::text LIKE '%Não Especificado%'
LIMIT 10;
