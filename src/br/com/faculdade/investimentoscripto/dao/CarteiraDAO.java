package br.com.faculdade.investimentoscripto.dao;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Empresa;
import br.com.faculdade.investimentoscripto.model.Investidor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD e consultas de Carteira (T_SIP_CARTEIRA) em JDBC puro.
 *
 * <p>A carteira tem duas chaves estrangeiras: o investidor (T_SIP_INVESTIDOR,
 * cujos dados cadastrais ficam em T_SIP_USUARIO) e a empresa (T_SIP_EMPRESA).
 * As consultas fazem JOIN para devolver a Carteira ja com o Investidor e a
 * Empresa preenchidos. Nao carregam as listas de ativos nem de transacoes.</p>
 *
 * <p>Esta classe nao imprime nada. Quem trata e exibe as mensagens e o teste.</p>
 */
public class CarteiraDAO {

    private static final String INSERIR =
            "INSERT INTO T_SIP_CARTEIRA (vl_saldo_total_geral, id_investidor, id_empresa) VALUES (?, ?, ?)";

    private static final String ATUALIZAR =
            "UPDATE T_SIP_CARTEIRA SET vl_saldo_total_geral = ?, id_investidor = ?, id_empresa = ? "
                    + "WHERE id_carteira = ?";

    private static final String DELETAR =
            "DELETE FROM T_SIP_CARTEIRA WHERE id_carteira = ?";

    private static final String SELECT_BASE =
            "SELECT c.id_carteira, c.vl_saldo_total_geral, "
                    + "u.id AS id_usuario, u.nm_usuario, u.ds_email, u.ds_senha_hash, u.cpf_usuario, "
                    + "e.id_empresa, e.nm_razao_social, e.nr_cnpj "
                    + "FROM T_SIP_CARTEIRA c "
                    + "INNER JOIN T_SIP_INVESTIDOR i ON i.id_usuario = c.id_investidor "
                    + "INNER JOIN T_SIP_USUARIO u ON u.id = i.id_usuario "
                    + "INNER JOIN T_SIP_EMPRESA e ON e.id_empresa = c.id_empresa";

    private static final String BUSCAR_POR_ID = SELECT_BASE + " WHERE c.id_carteira = ?";
    private static final String LISTAR_TODOS = SELECT_BASE + " ORDER BY c.id_carteira";
    private static final String LISTAR_POR_INVESTIDOR =
            SELECT_BASE + " WHERE c.id_investidor = ? ORDER BY c.id_carteira";
    private static final String LISTAR_POR_EMPRESA =
            SELECT_BASE + " WHERE c.id_empresa = ? ORDER BY c.id_carteira";

    private static final String CONTAR = "SELECT COUNT(*) FROM T_SIP_CARTEIRA";
    private static final String SOMAR_SALDO =
            "SELECT NVL(SUM(vl_saldo_total_geral), 0) FROM T_SIP_CARTEIRA";

    /** Nome da coluna de chave gerada, necessario para o getGeneratedKeys do Oracle. */
    private static final String[] COLUNA_ID_GERADO = {"ID_CARTEIRA"};

    /**
     * Insere a carteira. Em caso de sucesso, o id gerado e atribuido ao objeto.
     *
     * @param carteira carteira com saldo, investidor e empresa (com ids) preenchidos
     * @throws PersistenciaException se a insercao falhar
     * @throws IllegalArgumentException se algum dado obrigatorio for null
     */
    public void inserir(Carteira carteira) throws PersistenciaException {
        validar(carteira);

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(INSERIR, COLUNA_ID_GERADO)) {

            comando.setBigDecimal(1, carteira.getSaldoTotalGeral());
            comando.setLong(2, carteira.getInvestidor().getId());
            comando.setLong(3, carteira.getEmpresa().getId());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco nao devolveu o id gerado para T_SIP_CARTEIRA.");
                }
                carteira.setId(chaves.getLong(1));
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "inserir carteira");
        }
    }

    /**
     * Atualiza saldo, investidor e empresa da carteira. O id nunca e alterado.
     *
     * @param carteira carteira com o id preenchido
     * @return true se uma linha foi alterada; false se o id nao existe
     * @throws PersistenciaException se a atualizacao falhar
     * @throws IllegalArgumentException se algum dado obrigatorio for null
     */
    public boolean atualizar(Carteira carteira) throws PersistenciaException {
        validar(carteira);
        if (carteira.getId() == null) {
            throw new IllegalArgumentException("O id da carteira nao pode ser null.");
        }

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(ATUALIZAR)) {

            comando.setBigDecimal(1, carteira.getSaldoTotalGeral());
            comando.setLong(2, carteira.getInvestidor().getId());
            comando.setLong(3, carteira.getEmpresa().getId());
            comando.setLong(4, carteira.getId());

            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "atualizar carteira");
        }
    }

    /**
     * Exclui a carteira pelo id.
     *
     * @return true se foi excluida; false se o id nao existia
     * @throws PersistenciaException se a exclusao falhar (ex.: ha ativos ou transacoes vinculados)
     */
    public boolean deletar(long id) throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(DELETAR)) {

            comando.setLong(1, id);
            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "deletar carteira");
        }
    }

    /**
     * Busca a carteira pelo id.
     *
     * @return a carteira ou null se nao existir
     * @throws PersistenciaException se a consulta falhar
     */
    public Carteira buscarPorId(long id) throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_ID)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapearCarteira(resultado) : null;
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "buscar carteira por id");
        }
    }

    /**
     * Lista todas as carteiras, em ordem de id.
     *
     * @return ArrayList vazio se nao houver registros, nunca null
     * @throws PersistenciaException se a consulta falhar
     */
    public List<Carteira> listarTodos() throws PersistenciaException {
        return listar(LISTAR_TODOS, null, "listar carteiras");
    }

    /**
     * Lista as carteiras de um investidor.
     *
     * @param idInvestidor id do investidor (T_SIP_INVESTIDOR.id_usuario)
     * @throws PersistenciaException se a consulta falhar
     */
    public List<Carteira> listarPorInvestidor(long idInvestidor) throws PersistenciaException {
        return listar(LISTAR_POR_INVESTIDOR, idInvestidor, "listar carteiras por investidor");
    }

    /**
     * Lista as carteiras vinculadas a uma empresa.
     *
     * @param idEmpresa id da empresa (T_SIP_EMPRESA.id_empresa)
     * @throws PersistenciaException se a consulta falhar
     */
    public List<Carteira> listarPorEmpresa(long idEmpresa) throws PersistenciaException {
        return listar(LISTAR_POR_EMPRESA, idEmpresa, "listar carteiras por empresa");
    }

    /**
     * Conta as carteiras cadastradas.
     *
     * @throws PersistenciaException se a consulta falhar
     */
    public long contarTotalCarteiras() throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(CONTAR);
             ResultSet resultado = comando.executeQuery()) {

            resultado.next();
            return resultado.getLong(1);

        } catch (SQLException e) {
            throw traduzirErro(e, "contar carteiras");
        }
    }

    /**
     * Soma o saldo de todas as carteiras (consulta analitica complementar).
     *
     * @return a soma; zero se nao houver carteiras
     * @throws PersistenciaException se a consulta falhar
     */
    public BigDecimal somarSaldoTotal() throws PersistenciaException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SOMAR_SALDO);
             ResultSet resultado = comando.executeQuery()) {

            resultado.next();
            return resultado.getBigDecimal(1);

        } catch (SQLException e) {
            throw traduzirErro(e, "somar saldo das carteiras");
        }
    }

    /** Executa um SELECT com no maximo um parametro long e monta a lista de carteiras. */
    private List<Carteira> listar(String sql, Long parametro, String operacao)
            throws PersistenciaException {
        List<Carteira> carteiras = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            if (parametro != null) {
                comando.setLong(1, parametro);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    carteiras.add(mapearCarteira(resultado));
                }
            }
            return carteiras;

        } catch (SQLException e) {
            throw traduzirErro(e, operacao);
        }
    }

    private void validar(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException("A carteira nao pode ser null.");
        }
        if (carteira.getSaldoTotalGeral() == null) {
            throw new IllegalArgumentException("O saldo da carteira nao pode ser null.");
        }
        if (carteira.getInvestidor() == null || carteira.getInvestidor().getId() == null) {
            throw new IllegalArgumentException("A carteira precisa de um investidor com id.");
        }
        if (carteira.getEmpresa() == null || carteira.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("A carteira precisa de uma empresa com id.");
        }
    }

    /** Monta a Carteira (com Investidor e Empresa) a partir da linha atual do ResultSet. */
    private Carteira mapearCarteira(ResultSet resultado) throws SQLException {
        Investidor investidor = new Investidor(
                resultado.getLong("id_usuario"),
                resultado.getString("nm_usuario"),
                resultado.getString("ds_email"),
                resultado.getString("ds_senha_hash"),
                resultado.getString("cpf_usuario"));

        Empresa empresa = new Empresa(
                resultado.getLong("id_empresa"),
                resultado.getString("nm_razao_social"),
                resultado.getString("nr_cnpj"));

        return new Carteira(
                resultado.getLong("id_carteira"),
                resultado.getBigDecimal("vl_saldo_total_geral"),
                investidor,
                empresa);
    }

    /**
     * Traduz o erro do Oracle em PersistenciaException, mantendo a
     * SQLException original como causa.
     */
    private PersistenciaException traduzirErro(SQLException e, String operacao) {
        int codigo = e.getErrorCode();
        String detalhe = e.getMessage() == null ? "" : e.getMessage().toUpperCase();

        String mensagem = switch (codigo) {
            case 2291 -> detalhe.contains("FK_SIP_CARTEIRA_INVEST")
                    ? "Investidor informado não existe"
                    : detalhe.contains("FK_SIP_CARTEIRA_EMP")
                    ? "Empresa informada não existe"
                    : "Registro referenciado não existe";
            case 2292 -> "Carteira possui ativos ou transações vinculados e não pode ser excluída";
            case 1400 -> "Campo obrigatório não informado";
            case 1438 -> "Saldo maior que o permitido (até 13 dígitos inteiros e 2 decimais)";
            case 1017 -> "Usuário ou senha do banco inválidos";
            case 17002 -> "Não foi possível conectar ao servidor Oracle";
            default -> "Falha ao " + operacao + " (erro Oracle " + codigo + "): " + e.getMessage();
        };

        return new PersistenciaException(mensagem, e);
    }
}
