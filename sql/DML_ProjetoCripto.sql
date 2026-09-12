-- =====================================================================
-- PROJETO CRIPTO - FASE 5
-- Script DML (INSERT, UPDATE, DELETE e SELECT)
-- Oracle FIAP
-- Equipe: Fabio de Souza Junior, Gianluca Rodrigues Cordeiro,
--         Guilherme Moura Badia, Michael Molinari, Pedro Vitor Boeloni Baier
-- =====================================================================

-- =====================================================================
-- 1. INSERT - USUARIOS
-- T_SIP_USUARIO possui IDENTITY, mas neste script os IDs sao informados
-- explicitamente para facilitar a referencia pelas chaves estrangeiras.
-- =====================================================================

INSERT INTO T_SIP_USUARIO (id, nm_usuario, ds_email, ds_senha_hash, cpf_usuario)
VALUES (1, 'Michael Molinari', 'michael@projetocripto.com',
        'hash_michael_123456789', '11111111101');

INSERT INTO T_SIP_USUARIO (id, nm_usuario, ds_email, ds_senha_hash, cpf_usuario)
VALUES (2, 'Ana Beatriz Souza', 'ana@projetocripto.com',
        'hash_ana_123456789', '22222222202');

INSERT INTO T_SIP_USUARIO (id, nm_usuario, ds_email, ds_senha_hash, cpf_usuario)
VALUES (3, 'Carlos Eduardo Lima', 'carlos@projetocripto.com',
        'hash_carlos_123456789', '33333333303');

INSERT INTO T_SIP_USUARIO (id, nm_usuario, ds_email, ds_senha_hash, cpf_usuario)
VALUES (4, 'Julia Martins', 'julia@projetocripto.com',
        'hash_julia_123456789', '44444444404');

INSERT INTO T_SIP_USUARIO (id, nm_usuario, ds_email, ds_senha_hash, cpf_usuario)
VALUES (5, 'Rafael Oliveira', 'rafael@projetocripto.com',
        'hash_rafael_123456789', '55555555505');


-- =====================================================================
-- 2. INSERT - INVESTIDORES
-- T_SIP_INVESTIDOR depende de T_SIP_USUARIO.
-- =====================================================================

INSERT INTO T_SIP_INVESTIDOR (id_usuario)
VALUES (1);

INSERT INTO T_SIP_INVESTIDOR (id_usuario)
VALUES (2);

INSERT INTO T_SIP_INVESTIDOR (id_usuario)
VALUES (3);

INSERT INTO T_SIP_INVESTIDOR (id_usuario)
VALUES (4);


-- =====================================================================
-- 3. INSERT - EMPRESAS
-- T_SIP_EMPRESA depende de T_SIP_USUARIO.
-- CNPJ armazenado somente com os 14 digitos, conforme o DDL.
-- =====================================================================

INSERT INTO T_SIP_EMPRESA (id_empresa, nm_razao_social, nr_cnpj, id_usuario)
VALUES (1, 'CryptoTech Investimentos Ltda', '12345678000190', 1);

INSERT INTO T_SIP_EMPRESA (id_empresa, nm_razao_social, nr_cnpj, id_usuario)
VALUES (2, 'BlockData Solucoes Financeiras Ltda', '23456789000181', 2);

INSERT INTO T_SIP_EMPRESA (id_empresa, nm_razao_social, nr_cnpj, id_usuario)
VALUES (3, 'Digital Assets Brasil S.A.', '34567890000172', 3);


-- =====================================================================
-- 4. INSERT - CARTEIRAS
-- T_SIP_CARTEIRA depende de INVESTIDOR e EMPRESA.
-- =====================================================================

INSERT INTO T_SIP_CARTEIRA
    (id_carteira, vl_saldo_total_geral, id_investidor, id_empresa)
VALUES
    (1, 125000.00, 1, 1);

INSERT INTO T_SIP_CARTEIRA
    (id_carteira, vl_saldo_total_geral, id_investidor, id_empresa)
VALUES
    (2, 84500.00, 2, 2);

INSERT INTO T_SIP_CARTEIRA
    (id_carteira, vl_saldo_total_geral, id_investidor, id_empresa)
VALUES
    (3, 212750.00, 3, 1);

INSERT INTO T_SIP_CARTEIRA
    (id_carteira, vl_saldo_total_geral, id_investidor, id_empresa)
VALUES
    (4, 65300.00, 4, 3);


-- =====================================================================
-- 5. INSERT - ATIVOS DE CRIPTO
-- Chave primaria: (id_carteira, sg_ticker)
-- =====================================================================

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (1, 'BTC', 1.25000000, 52000.00, 65000.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (1, 'ETH', 12.50000000, 3200.00, 40000.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo,  vl_preco_medio_compra, vl_total_investido)
VALUES
    (1, 'SOL', 100.00000000, 200.00, 20000.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (2, 'BTC', 0.85000000, 50000.00, 42500.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (2, 'ETH', 10.00000000, 3200.00, 32000.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (3, 'BTC', 2.10000000, 60000.00, 126000.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (3, 'SOL', 250.00000000, 190.00, 47500.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (4, 'ETH', 15.00000000, 3000.00, 45000.00);

INSERT INTO T_SIP_ATIVO_CRIPTO
    (id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido)
VALUES
    (4, 'ADA', 8000.00000000, 2.00, 16000.00);


-- =====================================================================
-- 6. INSERT - TRANSACOES
-- T_SIP_TRANSACAO depende de T_SIP_CARTEIRA.
-- tp_transacao aceita somente COMPRA ou VENDA.
-- =====================================================================

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (1, TO_DATE('10/01/2026 10:15:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 0.50000000, 50000.00,
     'a1111111111111111111111111111111111111111111111111111111111111', 1);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (2, TO_DATE('15/01/2026 14:30:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 10.00000000, 3100.00,
     'b2222222222222222222222222222222222222222222222222222222222222', 1);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (3, TO_DATE('03/02/2026 09:20:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 100.00000000, 200.00,
     'c3333333333333333333333333333333333333333333333333333333333333', 1);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (4, TO_DATE('11/02/2026 11:10:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 0.85000000, 50000.00,
     'd4444444444444444444444444444444444444444444444444444444444444', 2);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (5, TO_DATE('19/02/2026 16:45:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 10.00000000, 3200.00,
     'e5555555555555555555555555555555555555555555555555555555555555', 2);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (6, TO_DATE('05/03/2026 13:05:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 2.10000000, 60000.00,
     'f6666666666666666666666666666666666666666666666666666666666666', 3);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (7, TO_DATE('12/03/2026 15:40:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 250.00000000, 190.00,
     '0711111111111111111111111111111111111111111111111111111111111111', 3);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (8, TO_DATE('25/03/2026 10:25:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 15.00000000, 3000.00,
     '0822222222222222222222222222222222222222222222222222222222222222', 4);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (9, TO_DATE('02/04/2026 12:50:00', 'DD/MM/YYYY HH24:MI:SS'),
     'COMPRA', 8000.00000000, 2.00,
     '0933333333333333333333333333333333333333333333333333333333333333', 4);

INSERT INTO T_SIP_TRANSACAO
    (id_transacao, dt_data_hora, tp_transacao, qt_transacao,
     vl_unitario, ds_hash_blockchain, id_carteira)
VALUES
    (10, TO_DATE('15/04/2026 17:10:00', 'DD/MM/YYYY HH24:MI:SS'),
     'VENDA', 0.10000000, 65000.00,
     '1044444444444444444444444444444444444444444444444444444444444444', 1);


-- =====================================================================
-- 7. UPDATE - exemplos de alteracao de dados
-- =====================================================================

-- Atualiza o nome do usuario 5.
UPDATE T_SIP_USUARIO
SET nm_usuario = 'Rafael Henrique Oliveira'
WHERE id = 5;

-- Ajusta o saldo geral da carteira 1 apos uma movimentacao.
UPDATE T_SIP_CARTEIRA
SET vl_saldo_total_geral = 118500.00
WHERE id_carteira = 1;


-- =====================================================================
-- 8. DELETE - exemplo seguro para demonstrar a operacao
-- O usuario 6 e temporario e nao possui registros filhos.
-- =====================================================================

INSERT INTO T_SIP_USUARIO (id, nm_usuario, ds_email, ds_senha_hash, cpf_usuario)
VALUES (6, 'Usuario Temporario', 'temporario@projetocripto.com',
        'hash_temporario', '66666666606');

DELETE FROM T_SIP_USUARIO
WHERE id = 6;


-- =====================================================================
-- 9. SELECT - consultas basicas
-- =====================================================================

-- 9.1 Listar todos os usuarios
SELECT id, nm_usuario, ds_email, cpf_usuario
FROM T_SIP_USUARIO
ORDER BY id;


-- 9.2 Listar investidores
SELECT i.id_usuario,
       u.nm_usuario,
       u.ds_email
FROM T_SIP_INVESTIDOR i
INNER JOIN T_SIP_USUARIO u
        ON u.id = i.id_usuario
ORDER BY u.nm_usuario;


-- 9.3 Listar empresas e respectivos usuarios
SELECT e.id_empresa,
       e.nm_razao_social,
       e.nr_cnpj,
       u.nm_usuario AS responsavel
FROM T_SIP_EMPRESA e
INNER JOIN T_SIP_USUARIO u
        ON u.id = e.id_usuario
ORDER BY e.id_empresa;


-- 9.4 Listar carteiras com investidor e empresa
SELECT c.id_carteira,
       c.vl_saldo_total_geral,
       u.nm_usuario AS investidor,
       e.nm_razao_social AS empresa
FROM T_SIP_CARTEIRA c
INNER JOIN T_SIP_INVESTIDOR i
        ON i.id_usuario = c.id_investidor
INNER JOIN T_SIP_USUARIO u
        ON u.id = i.id_usuario
INNER JOIN T_SIP_EMPRESA e
        ON e.id_empresa = c.id_empresa
ORDER BY c.id_carteira;


-- =====================================================================
-- 10. SELECT - ativos por carteira
-- =====================================================================

SELECT ac.id_carteira,
       ac.sg_ticker,
       ac.qt_ativo,
       ac.vl_preco_medio_compra,
       ac.vl_total_investido
FROM T_SIP_ATIVO_CRIPTO ac
ORDER BY ac.id_carteira, ac.sg_ticker;


-- =====================================================================
-- 11. SELECT - historico de transacoes
-- =====================================================================

SELECT t.id_transacao,
       t.dt_data_hora,
       t.tp_transacao,
       t.qt_transacao,
       t.vl_unitario,
       (t.qt_transacao * t.vl_unitario) AS vl_total,
       t.id_carteira
FROM T_SIP_TRANSACAO t
ORDER BY t.dt_data_hora;


-- =====================================================================
-- 12. SELECT - transacoes com dados do investidor
-- =====================================================================

SELECT t.id_transacao,
       t.dt_data_hora,
       t.tp_transacao,
       t.qt_transacao,
       t.vl_unitario,
       u.nm_usuario AS investidor,
       e.nm_razao_social AS empresa
FROM T_SIP_TRANSACAO t
INNER JOIN T_SIP_CARTEIRA c
        ON c.id_carteira = t.id_carteira
INNER JOIN T_SIP_INVESTIDOR i
        ON i.id_usuario = c.id_investidor
INNER JOIN T_SIP_USUARIO u
        ON u.id = i.id_usuario
INNER JOIN T_SIP_EMPRESA e
        ON e.id_empresa = c.id_empresa
ORDER BY t.dt_data_hora;


-- =====================================================================
-- 13. SELECT - total investido por carteira
-- =====================================================================

SELECT c.id_carteira,
       u.nm_usuario AS investidor,
       SUM(ac.vl_total_investido) AS total_investido
FROM T_SIP_CARTEIRA c
INNER JOIN T_SIP_INVESTIDOR i
        ON i.id_usuario = c.id_investidor
INNER JOIN T_SIP_USUARIO u
        ON u.id = i.id_usuario
INNER JOIN T_SIP_ATIVO_CRIPTO ac
        ON ac.id_carteira = c.id_carteira
GROUP BY c.id_carteira, u.nm_usuario
ORDER BY total_investido DESC;


-- =====================================================================
-- 14. SELECT - quantidade total de cada criptomoeda
-- =====================================================================

SELECT sg_ticker,
       SUM(qt_ativo) AS quantidade_total,
       SUM(vl_total_investido) AS valor_total_investido
FROM T_SIP_ATIVO_CRIPTO
GROUP BY sg_ticker
ORDER BY valor_total_investido DESC;


-- =====================================================================
-- 15. SELECT - maior transacao por carteira
-- =====================================================================

SELECT id_carteira,
       MAX(qt_transacao * vl_unitario) AS maior_valor_transacao
FROM T_SIP_TRANSACAO
GROUP BY id_carteira
ORDER BY id_carteira;


-- =====================================================================
-- 16. SELECT - validacao da massa de dados
-- =====================================================================

SELECT 'T_SIP_USUARIO' AS tabela, COUNT(*) AS quantidade
FROM T_SIP_USUARIO

UNION ALL

SELECT 'T_SIP_INVESTIDOR', COUNT(*)
FROM T_SIP_INVESTIDOR

UNION ALL

SELECT 'T_SIP_EMPRESA', COUNT(*)
FROM T_SIP_EMPRESA

UNION ALL

SELECT 'T_SIP_CARTEIRA', COUNT(*)
FROM T_SIP_CARTEIRA

UNION ALL

SELECT 'T_SIP_ATIVO_CRIPTO', COUNT(*)
FROM T_SIP_ATIVO_CRIPTO

UNION ALL

SELECT 'T_SIP_TRANSACAO', COUNT(*)
FROM T_SIP_TRANSACAO;


-- =====================================================================
-- 17. COMMIT
-- =====================================================================

COMMIT;

-- FIM DO SCRIPT DML
