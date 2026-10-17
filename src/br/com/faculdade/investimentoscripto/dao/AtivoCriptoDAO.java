package br.com.faculdade.investimentoscripto.dao;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CRUD e consultas de AtivoCripto (T_SIP_ATIVO_CRIPTO) em JDBC puro.
 *
 * <p>A chave primaria e composta: id da carteira + ticker. O ticker e sempre
 * normalizado (sem espacos e em maiusculas) antes de ir ao banco. Nome,
 * cotacao e transacoes do AtivoCripto sao apenas de dominio e nao sao persistidos.</p>
 *
 * <p>Esta classe nao imprime nada. Quem trata e exibe as mensagens e o teste.</p>
 */
public class AtivoCriptoDAO {

    private static final int TAMANHO_MAXIMO_TICKER = 10;

    private static final String INSERIR =
            "INSERT INTO T_SIP_ATIVO_CRIPTO "
                    + "(id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido) "
                    + "VALUES (?, ?, ?, ?, ?)";

    private static final String ATUALIZAR =
            "UPDATE T_SIP_ATIVO_CRIPTO "
                    + "SET qt_ativo = ?, vl_preco_medio_compra = ?, vl_total_investido = ? "
                    + "WHERE id_carteira = ? AND sg_ticker = ?";

    private static final String DELETAR =
            "DELETE FROM T_SIP_ATIVO_CRIPTO WHERE id_carteira = ? AND sg_ticker = ?";

    private static final String SELECT_BASE =
            "SELECT id_carteira, sg_ticker, qt_ativo, vl_preco_medio_compra, vl_total_investido "
                    + "FROM T_SIP_ATIVO_CRIPTO";

    private static final String BUSCAR_POR_CHAVE = SELECT_BASE + " WHERE id_carteira = ? AND sg_ticker = ?";
    private static final String LISTAR_TODOS = SELECT_BASE + " ORDER BY id_carteira, sg_ticker";
    private static final String LISTAR_POR_CARTEIRA = SELECT_BASE + " WHERE id_carteira = ? ORDER BY sg_ticker";
    private static final String LISTAR_POR_TICKER = SELECT_BASE + " WHERE sg_ticker = ? ORDER BY id_carteira";

    private static final String CONTAR = "SELECT COUNT(*) FROM T_SIP_ATIVO_CRIPTO";

    /**
     * Insere o ativo na carteira.
     *
     * @param ativo ativo com carteira, ticker, quantidade, preco medio e total investido
     * @throws PersistenciaException se a insercao falhar (ex.: ativo ja existe na carteira)
     * @throws IllegalArgumentException se algum dado obrigatorio for invalido
     */
    public void inserir(AtivoCripto ativo) throws PersistenciaException {
        validarAtivo(ativo);

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(INSERIR)) {

            comando.setLong(1, ativo.getIdCarteira());
            comando.setString(2, normalizarTicker(ativo.getTicker()));
            comando.setBigDecimal(3, ativo.getQuantidade());
            comando.setBigDecimal(4, ativo.getPrecoMedioCompra());
            comando.setBigDecimal(5, ativo.getValorTotalInvestido());
            comando.executeUpdate();

        } catch (SQLException e) {
            throw traduzirErro(e, "inserir ativo");
        }
    }

    /**
     * Atualiza quantidade, preco medio e total investido. A chave composta nunca e alterada.
     *
     * @return true se uma linha foi alterada; false se o ativo nao existe na carteira
     * @throws PersistenciaException se a atualizacao falhar
     * @throws IllegalArgumentException se algum dado obrigatorio for invalido
     */
    public boolean atualizar(AtivoCripto ativo) throws PersistenciaException {
        validarAtivo(ativo);

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(ATUALIZAR)) {

            comando.setBigDecimal(1, ativo.getQuantidade());
            comando.setBigDecimal(2, ativo.getPrecoMedioCompra());
            comando.setBigDecimal(3, ativo.getValorTotalInvestido());
            comando.setLong(4, ativo.getIdCarteira());
            comando.setString(5, normalizarTicker(ativo.getTicker()));

            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "atualizar ativo");
        }
    }

    /**
     * Exclui o ativo pela chave composta.
     *
     * @return true se foi excluido; false se nao existia
     * @throws PersistenciaException se a exclusao falhar
     * @throws IllegalArgumentException se a chave for invalida
     */
    public boolean deletar(long idCarteira, String ticker) throws PersistenciaException {
        validarChave(idCarteira, ticker);

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(DELETAR)) {

            comando.setLong(1, idCarteira);
            comando.setString(2, normalizarTicker(ticker));
            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "deletar ativo");
        }
    }

    /**
     * Busca o ativo pela chave composta.
     *
     * @return o ativo ou null se nao existir
     * @throws PersistenciaException se a consulta falhar
     * @throws IllegalArgumentException se a chave for invalida
     */
    public AtivoCripto buscarPorChave(long idCarteira, String ticker) throws PersistenciaException {
        validarChave(idCarteira, ticker);

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_CHAVE)) {

            comando.setLong(1, idCarteira);
            comando.setString(2, normalizarTicker(ticker));

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapearAtivo(resultado) : null;
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "buscar ativo por chave");
        }
    }

    /**
     * Verifica se o ativo existe na carteira.
     *
     * @throws PersistenciaException se a consulta falhar
     * @throws IllegalArgumentException se a chave for invalida
     */
    public boolean existe(long idCarteira, String ticker) throws PersistenciaException {
        return buscarPorChave(idCarteira, ticker) != null;
    }

    /**
     * Lista todos os ativos, ordenados por carteira e ticker.
     *
     * @return ArrayList vazio se nao houver registros, nunca null
     * @throws PersistenciaException se a consulta falhar
     */
    public List<AtivoCripto> listarTodos() throws PersistenciaException {
        return listar(LISTAR_TODOS, null, null, "listar ativos");
    }

    /**
     * Lista os ativos de uma carteira.
     *
     * @throws PersistenciaException se a consulta falhar
     * @throws IllegalArgumentException se o id for invalido
     */
    public List<AtivoCripto> listarPorCarteira(long idCarteira) throws PersistenciaException {
        validarIdCarteira(idCarteira);
        return listar(LISTAR_POR_CARTEIRA, idCarteira, null, "listar ativos por carteira");
    }

    /**
     * Lista as posicoes de um ticker em todas as carteiras.
     *
     * @throws PersistenciaException se a consulta falhar
     * @throws IllegalArgumentException se o ticker for invalido
     */
    public List<AtivoCripto> listarPorTicker(String ticker) throws PersistenciaException {
        validarTicker(ticker);
        return listar(LISTAR_POR_TICKER, null, normalizarTicker(ticker), "listar ativos por ticker");
    }

    /**
     * Conta os ativos cadastrados (linhas da tabela).
     *
     * @throws PersistenciaException se a consulta falhar
     */
    public long contar() throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(CONTAR);
             ResultSet resultado = comando.executeQuery()) {

            resultado.next();
            return resultado.getLong(1);

        } catch (SQLException e) {
            throw traduzirErro(e, "contar ativos");
        }
    }

    /** Executa um SELECT com no maximo um parametro (id da carteira ou ticker). */
    private List<AtivoCripto> listar(String sql, Long idCarteira, String ticker, String operacao)
            throws PersistenciaException {
        List<AtivoCripto> ativos = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            if (idCarteira != null) {
                comando.setLong(1, idCarteira);
            } else if (ticker != null) {
                comando.setString(1, ticker);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ativos.add(mapearAtivo(resultado));
                }
            }
            return ativos;

        } catch (SQLException e) {
            throw traduzirErro(e, operacao);
        }
    }

    private void validarAtivo(AtivoCripto ativo) {
        if (ativo == null) {
            throw new IllegalArgumentException("O ativo nao pode ser null.");
        }
        if (ativo.getIdCarteira() == null) {
            throw new IllegalArgumentException("O id da carteira do ativo nao pode ser null.");
        }
        validarChave(ativo.getIdCarteira(), ativo.getTicker());
        if (ativo.getQuantidade() == null || ativo.getPrecoMedioCompra() == null
                || ativo.getValorTotalInvestido() == null) {
            throw new IllegalArgumentException(
                    "Quantidade, preco medio e total investido sao obrigatorios.");
        }
    }

    private void validarChave(long idCarteira, String ticker) {
        validarIdCarteira(idCarteira);
        validarTicker(ticker);
    }

    private void validarIdCarteira(long idCarteira) {
        if (idCarteira <= 0) {
            throw new IllegalArgumentException("O id da carteira deve ser maior que zero.");
        }
    }

    private void validarTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("O ticker e obrigatorio.");
        }
        if (normalizarTicker(ticker).length() > TAMANHO_MAXIMO_TICKER) {
            throw new IllegalArgumentException(
                    "O ticker deve ter no maximo " + TAMANHO_MAXIMO_TICKER + " caracteres.");
        }
    }

    private String normalizarTicker(String ticker) {
        return ticker.trim().toUpperCase(Locale.ROOT);
    }

    /** Monta o AtivoCripto a partir da linha atual do ResultSet. */
    private AtivoCripto mapearAtivo(ResultSet resultado) throws SQLException {
        return new AtivoCripto(
                resultado.getLong("id_carteira"),
                resultado.getString("sg_ticker"),
                resultado.getBigDecimal("qt_ativo"),
                resultado.getBigDecimal("vl_preco_medio_compra"),
                resultado.getBigDecimal("vl_total_investido"));
    }

    /**
     * Traduz o erro do Oracle em PersistenciaException, mantendo a
     * SQLException original como causa.
     */
    private PersistenciaException traduzirErro(SQLException e, String operacao) {
        int codigo = e.getErrorCode();
        String detalhe = e.getMessage() == null ? "" : e.getMessage().toUpperCase();

        String mensagem = switch (codigo) {
            case 1 -> detalhe.contains("PK_SIP_ATIVO_CRIPTO")
                    ? "A carteira já possui esse ativo"
                    : "Registro duplicado";
            case 2291 -> detalhe.contains("FK_SIP_ATIVO_CARTEIRA")
                    ? "A carteira informada não existe"
                    : "Registro referenciado não existe";
            case 1400 -> "Campo obrigatório não informado";
            case 1438 -> "Valor numérico maior que o permitido pela coluna";
            case 12899 -> "Valor maior que o tamanho permitido (o ticker aceita até 10 caracteres)";
            case 1017 -> "Usuário ou senha do banco inválidos";
            case 17002 -> "Não foi possível conectar ao servidor Oracle";
            default -> "Falha ao " + operacao + " (erro Oracle " + codigo + "): " + e.getMessage();
        };

        return new PersistenciaException(mensagem, e);
    }
}
