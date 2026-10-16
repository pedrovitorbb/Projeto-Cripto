package br.com.faculdade.investimentoscripto.dao;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Transacao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD e consultas de Transacao (T_SIP_TRANSACAO) em JDBC puro.
 *
 * <p>A transacao tem uma unica chave estrangeira, a carteira (T_SIP_CARTEIRA),
 * gravada e lida pelo campo {@code idCarteira}. Ticker, preco medio de compra e
 * ativo nao tem coluna na tabela e nao sao persistidos.</p>
 *
 * <p>Cada metodo executa um unico comando em uma unica tabela, por isso nao ha
 * controle manual de transacao (o auto-commit do JDBC basta).</p>
 *
 * <p>Esta classe nao imprime nada. Quem trata e exibe as mensagens e o teste.</p>
 */
public class TransacaoDAO {

    private static final String INSERIR =
            "INSERT INTO T_SIP_TRANSACAO (dt_data_hora, tp_transacao, qt_transacao, vl_unitario, "
                    + "ds_hash_blockchain, id_carteira) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String ATUALIZAR =
            "UPDATE T_SIP_TRANSACAO SET dt_data_hora = ?, tp_transacao = ?, qt_transacao = ?, "
                    + "vl_unitario = ?, ds_hash_blockchain = ?, id_carteira = ? "
                    + "WHERE id_transacao = ?";

    private static final String DELETAR =
            "DELETE FROM T_SIP_TRANSACAO WHERE id_transacao = ?";

    private static final String SELECT_BASE =
            "SELECT id_transacao, dt_data_hora, tp_transacao, qt_transacao, vl_unitario, "
                    + "ds_hash_blockchain, id_carteira "
                    + "FROM T_SIP_TRANSACAO";

    private static final String BUSCAR_POR_ID = SELECT_BASE + " WHERE id_transacao = ?";
    private static final String LISTAR_TODOS = SELECT_BASE + " ORDER BY id_transacao";
    private static final String LISTAR_POR_CARTEIRA =
            SELECT_BASE + " WHERE id_carteira = ? ORDER BY id_transacao";
    private static final String LISTAR_POR_TIPO =
            SELECT_BASE + " WHERE tp_transacao = ? ORDER BY id_transacao";

    private static final String SOMAR_VALOR_POR_CARTEIRA =
            "SELECT NVL(SUM(qt_transacao * vl_unitario), 0) FROM T_SIP_TRANSACAO WHERE id_carteira = ?";

    /** Nome da coluna de chave gerada, necessario para o getGeneratedKeys do Oracle. */
    private static final String[] COLUNA_ID_GERADO = {"ID_TRANSACAO"};

    /**
     * Insere a transacao. Em caso de sucesso, o id gerado e atribuido ao objeto.
     * Se a data e hora nao forem informadas, usa o momento atual (sem fracao de
     * segundo, que a coluna DATE nao guarda) e atribui esse valor ao objeto.
     *
     * @param transacao transacao com tipo, quantidade, valor unitario e id da carteira preenchidos
     * @throws PersistenciaException se a insercao falhar
     * @throws IllegalArgumentException se algum dado obrigatorio for null
     */
    public void inserir(Transacao transacao) throws PersistenciaException {
        validar(transacao);
        if (transacao.getDataHora() == null) {
            transacao.setDataHora(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        }

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(INSERIR, COLUNA_ID_GERADO)) {

            preencherCampos(comando, transacao);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco nao devolveu o id gerado para T_SIP_TRANSACAO.");
                }
                transacao.setId(chaves.getLong(1));
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "inserir transação");
        }
    }

    /**
     * Atualiza data e hora, tipo, quantidade, valor unitario, hash e carteira da
     * transacao. O id nunca e alterado.
     *
     * @param transacao transacao com o id e a data e hora preenchidos
     * @return true se uma linha foi alterada; false se o id nao existe
     * @throws PersistenciaException se a atualizacao falhar
     * @throws IllegalArgumentException se algum dado obrigatorio for null
     */
    public boolean atualizar(Transacao transacao) throws PersistenciaException {
        validar(transacao);
        if (transacao.getId() == null) {
            throw new IllegalArgumentException("O id da transacao nao pode ser null.");
        }
        if (transacao.getDataHora() == null) {
            throw new IllegalArgumentException("A data e hora da transacao nao podem ser null.");
        }

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(ATUALIZAR)) {

            preencherCampos(comando, transacao);
            comando.setLong(7, transacao.getId());

            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "atualizar transação");
        }
    }

    /**
     * Exclui a transacao pelo id.
     *
     * @return true se foi excluida; false se o id nao existia
     * @throws PersistenciaException se a exclusao falhar
     */
    public boolean deletar(long id) throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(DELETAR)) {

            comando.setLong(1, id);
            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "deletar transação");
        }
    }

    /**
     * Busca a transacao pelo id.
     *
     * @return a transacao ou null se nao existir
     * @throws PersistenciaException se a consulta falhar
     */
    public Transacao buscarPorId(long id) throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_ID)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapearTransacao(resultado) : null;
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "buscar transação por id");
        }
    }

    /**
     * Lista todas as transacoes, em ordem de id.
     *
     * @return ArrayList vazio se nao houver registros, nunca null
     * @throws PersistenciaException se a consulta falhar
     */
    public List<Transacao> listarTodos() throws PersistenciaException {
        return listar(LISTAR_TODOS, null, "listar transações");
    }

    /**
     * Lista as transacoes de uma carteira, em ordem de id.
     *
     * @param idCarteira id da carteira (T_SIP_CARTEIRA.id_carteira)
     * @return ArrayList vazio se nao houver registros, nunca null
     * @throws PersistenciaException se a consulta falhar
     */
    public List<Transacao> listarPorCarteira(long idCarteira) throws PersistenciaException {
        return listar(LISTAR_POR_CARTEIRA, idCarteira, "listar transações por carteira");
    }

    /**
     * Lista as transacoes de um tipo (COMPRA ou VENDA), em ordem de id.
     *
     * @return ArrayList vazio se nao houver registros, nunca null
     * @throws PersistenciaException se a consulta falhar
     * @throws IllegalArgumentException se o tipo for null
     */
    public List<Transacao> listarPorTipo(Transacao.Tipo tipo) throws PersistenciaException {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da transacao nao pode ser null.");
        }
        return listar(LISTAR_POR_TIPO, tipo.name(), "listar transações por tipo");
    }

    /**
     * Soma o valor (quantidade x valor unitario) das transacoes de uma carteira,
     * sem distinguir compra de venda.
     *
     * @param idCarteira id da carteira (T_SIP_CARTEIRA.id_carteira)
     * @return a soma; zero se a carteira nao tiver transacoes
     * @throws PersistenciaException se a consulta falhar
     */
    public BigDecimal somarValorPorCarteira(long idCarteira) throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SOMAR_VALOR_POR_CARTEIRA)) {

            comando.setLong(1, idCarteira);

            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBigDecimal(1);
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "somar valor das transações da carteira");
        }
    }

    /**
     * Executa um SELECT com no maximo um parametro e monta a lista de transacoes.
     * O parametro e sempre ligado ao PreparedStatement, nunca concatenado.
     */
    private List<Transacao> listar(String sql, Object parametro, String operacao)
            throws PersistenciaException {
        List<Transacao> transacoes = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            if (parametro != null) {
                comando.setObject(1, parametro);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    transacoes.add(mapearTransacao(resultado));
                }
            }
            return transacoes;

        } catch (SQLException e) {
            throw traduzirErro(e, operacao);
        }
    }

    /** Preenche os parametros 1 a 6, comuns ao INSERT e ao UPDATE. */
    private void preencherCampos(PreparedStatement comando, Transacao transacao) throws SQLException {
        comando.setTimestamp(1, Timestamp.valueOf(transacao.getDataHora()));
        comando.setString(2, transacao.getTipo().name());
        comando.setBigDecimal(3, transacao.getQuantidade());
        comando.setBigDecimal(4, transacao.getValorUnitario());

        String hash = transacao.getHashBlockchain();
        if (hash == null || hash.isBlank()) {
            comando.setNull(5, Types.VARCHAR);
        } else {
            comando.setString(5, hash);
        }

        comando.setLong(6, transacao.getIdCarteira());
    }

    private void validar(Transacao transacao) {
        if (transacao == null) {
            throw new IllegalArgumentException("A transacao nao pode ser null.");
        }
        if (transacao.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da transacao nao pode ser null.");
        }
        if (transacao.getQuantidade() == null) {
            throw new IllegalArgumentException("A quantidade da transacao nao pode ser null.");
        }
        if (transacao.getValorUnitario() == null) {
            throw new IllegalArgumentException("O valor unitario da transacao nao pode ser null.");
        }
        if (transacao.getIdCarteira() == null) {
            throw new IllegalArgumentException("A transacao precisa do id da carteira.");
        }
    }

    /** Monta a Transacao a partir da linha atual do ResultSet, usando o construtor de persistencia. */
    private Transacao mapearTransacao(ResultSet resultado) throws SQLException {
        Timestamp dataHora = resultado.getTimestamp("dt_data_hora");

        return new Transacao(
                resultado.getLong("id_transacao"),
                resultado.getLong("id_carteira"),
                Transacao.Tipo.valueOf(resultado.getString("tp_transacao")),
                resultado.getBigDecimal("qt_transacao"),
                resultado.getBigDecimal("vl_unitario"),
                resultado.getString("ds_hash_blockchain"),
                dataHora == null ? null : dataHora.toLocalDateTime());
    }

    /**
     * Traduz o erro do Oracle em PersistenciaException, mantendo a
     * SQLException original como causa.
     */
    private PersistenciaException traduzirErro(SQLException e, String operacao) {
        int codigo = e.getErrorCode();
        String detalhe = e.getMessage() == null ? "" : e.getMessage().toUpperCase();

        String mensagem = switch (codigo) {
            case 2290 -> detalhe.contains("CH_SIP_TRANSACAO")
                    ? "Tipo de transação inválido: use COMPRA ou VENDA"
                    : detalhe.contains("CH_SIP_VL_UNITARIO")
                    ? "O valor unitário deve ser maior que zero"
                    : "Dado fora das regras da tabela";
            case 2291 -> "A carteira informada não existe";
            case 1 -> detalhe.contains("UN_SIP_TRANSACAO_HASH")
                    ? "Hash de blockchain já registrado"
                    : "Registro duplicado";
            case 1400 -> "Campo obrigatório não informado";
            case 1438, 12899 -> "Valor maior que o tamanho permitido pela coluna";
            case 1017 -> "Usuário ou senha do banco inválidos";
            case 17002 -> "Não foi possível conectar ao servidor Oracle";
            default -> "Falha ao " + operacao + " (erro Oracle " + codigo + "): " + e.getMessage();
        };

        return new PersistenciaException(mensagem, e);
    }
}
