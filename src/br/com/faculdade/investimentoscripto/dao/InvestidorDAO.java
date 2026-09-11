package br.com.faculdade.investimentoscripto.dao;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Investidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de Investidor em JDBC puro.
 *
 * <p>O investidor ocupa duas tabelas: os dados cadastrais ficam em
 * T_SIP_USUARIO e T_SIP_INVESTIDOR guarda apenas o id que marca o usuario
 * como investidor. Por isso inserir e excluir sao transacoes: as duas
 * tabelas mudam juntas ou nenhuma muda.</p>
 *
 * <p>Esta classe nao imprime nada. Quem trata e exibe as mensagens e a Main.</p>
 */
public class InvestidorDAO {

    private static final String INSERIR_USUARIO =
            "INSERT INTO T_SIP_USUARIO (nm_usuario, ds_email, ds_senha_hash, cpf_usuario) VALUES (?, ?, ?, ?)";

    private static final String INSERIR_INVESTIDOR =
            "INSERT INTO T_SIP_INVESTIDOR (id_usuario) VALUES (?)";

    private static final String SELECT_BASE =
            "SELECT u.id, u.nm_usuario, u.ds_email, u.ds_senha_hash, u.cpf_usuario "
                    + "FROM T_SIP_USUARIO u "
                    + "INNER JOIN T_SIP_INVESTIDOR i ON i.id_usuario = u.id";

    private static final String LISTAR = SELECT_BASE + " ORDER BY u.id";

    private static final String BUSCAR_POR_ID = SELECT_BASE + " WHERE u.id = ?";

    private static final String ALTERAR =
            "UPDATE T_SIP_USUARIO SET nm_usuario = ?, ds_email = ?, ds_senha_hash = ?, cpf_usuario = ? "
                    + "WHERE id = ? AND id IN (SELECT id_usuario FROM T_SIP_INVESTIDOR)";

    private static final String EXCLUIR_INVESTIDOR =
            "DELETE FROM T_SIP_INVESTIDOR WHERE id_usuario = ?";

    private static final String EXCLUIR_USUARIO =
            "DELETE FROM T_SIP_USUARIO WHERE id = ?";

    /** Nome da coluna de chave gerada, necessario para o getGeneratedKeys do Oracle. */
    private static final String[] COLUNA_ID_GERADO = {"ID"};

    /**
     * Insere o investidor em T_SIP_USUARIO e T_SIP_INVESTIDOR dentro de uma
     * transacao. Em caso de sucesso, o id gerado e atribuido ao objeto recebido.
     *
     * @param investidor investidor a inserir; nao pode ser null
     * @throws PersistenciaException se a insercao falhar
     * @throws IllegalArgumentException se o investidor for null
     */
    public void inserir(Investidor investidor) throws PersistenciaException {
        if (investidor == null) {
            throw new IllegalArgumentException("O investidor nao pode ser null.");
        }

        final String operacao = "inserir investidor";

        try (Connection conexao = ConnectionFactory.getConnection()) {
            conexao.setAutoCommit(false);

            // O rollback fica neste try interno: o catch de um try-with-resources
            // so roda depois que a conexao ja foi fechada, e o Oracle faz commit
            // implicito ao fechar uma conexao com autocommit desligado.
            try {
                long idGerado = inserirUsuario(conexao, investidor);
                inserirVinculoInvestidor(conexao, idGerado);

                conexao.commit();
                investidor.setId(idGerado);
            } catch (SQLException | RuntimeException e) {
                desfazer(conexao, e);
                if (e instanceof SQLException erroSql) {
                    throw traduzirErro(erroSql, operacao);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw traduzirErro(e, operacao);
        }
    }

    /**
     * Lista todos os investidores cadastrados, em ordem de id.
     *
     * @return lista de investidores; vazia se nao houver registros, nunca null
     * @throws PersistenciaException se a consulta falhar
     */
    public List<Investidor> listar() throws PersistenciaException {
        List<Investidor> investidores = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(LISTAR);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                investidores.add(mapearInvestidor(resultado));
            }
            return investidores;

        } catch (SQLException e) {
            throw traduzirErro(e, "listar investidores");
        }
    }

    /**
     * Busca um investidor pelo id.
     *
     * @param id id do usuario/investidor; nao pode ser null
     * @return o investidor encontrado ou null se nao existir
     * @throws PersistenciaException se a consulta falhar
     * @throws IllegalArgumentException se o id for null
     */
    public Investidor buscarPorId(Long id) throws PersistenciaException {
        if (id == null) {
            throw new IllegalArgumentException("O id nao pode ser null.");
        }

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_ID)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapearInvestidor(resultado) : null;
            }

        } catch (SQLException e) {
            throw traduzirErro(e, "buscar investidor por id");
        }
    }

    /**
     * Altera os dados cadastrais do investidor. O id nunca e alterado.
     *
     * @param investidor investidor com o id preenchido; nao pode ser null
     * @return true se uma linha foi alterada; false se o id nao existir ou nao for investidor
     * @throws PersistenciaException se a alteracao falhar
     * @throws IllegalArgumentException se o investidor ou o id dele for null
     */
    public boolean alterar(Investidor investidor) throws PersistenciaException {
        if (investidor == null) {
            throw new IllegalArgumentException("O investidor nao pode ser null.");
        }
        if (investidor.getId() == null) {
            throw new IllegalArgumentException("O id do investidor nao pode ser null.");
        }

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(ALTERAR)) {

            comando.setString(1, investidor.getNome());
            comando.setString(2, investidor.getEmail());
            comando.setString(3, investidor.getSenhaHash());
            comando.setString(4, investidor.getCpf());
            comando.setLong(5, investidor.getId());

            return comando.executeUpdate() == 1;

        } catch (SQLException e) {
            throw traduzirErro(e, "alterar investidor");
        }
    }

    /**
     * Exclui o investidor das duas tabelas dentro de uma transacao.
     *
     * @param id id do investidor; nao pode ser null
     * @return true se o investidor foi excluido; false se ele nao existia
     * @throws PersistenciaException se a exclusao falhar
     * @throws IllegalArgumentException se o id for null
     */
    public boolean excluir(Long id) throws PersistenciaException {
        if (id == null) {
            throw new IllegalArgumentException("O id nao pode ser null.");
        }

        final String operacao = "excluir investidor";

        try (Connection conexao = ConnectionFactory.getConnection()) {
            conexao.setAutoCommit(false);

            // Mesmo motivo do inserir: rollback dentro do try interno.
            try {
                int linhasInvestidor;
                try (PreparedStatement comando = conexao.prepareStatement(EXCLUIR_INVESTIDOR)) {
                    comando.setLong(1, id);
                    linhasInvestidor = comando.executeUpdate();
                }

                if (linhasInvestidor == 0) {
                    conexao.rollback();
                    return false;
                }

                try (PreparedStatement comando = conexao.prepareStatement(EXCLUIR_USUARIO)) {
                    comando.setLong(1, id);
                    comando.executeUpdate();
                }

                conexao.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                desfazer(conexao, e);
                if (e instanceof SQLException erroSql) {
                    throw traduzirErro(erroSql, operacao);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw traduzirErro(e, operacao);
        }
    }

    /**
     * Insere a linha de T_SIP_USUARIO e devolve o id gerado pela coluna IDENTITY.
     */
    private long inserirUsuario(Connection conexao, Investidor investidor) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(INSERIR_USUARIO, COLUNA_ID_GERADO)) {
            comando.setString(1, investidor.getNome());
            comando.setString(2, investidor.getEmail());
            comando.setString(3, investidor.getSenhaHash());
            comando.setString(4, investidor.getCpf());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco nao devolveu o id gerado para T_SIP_USUARIO.");
                }
                return chaves.getLong(1);
            }
        }
    }

    /** Insere em T_SIP_INVESTIDOR o id do usuario recem-criado. */
    private void inserirVinculoInvestidor(Connection conexao, long idUsuario) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(INSERIR_INVESTIDOR)) {
            comando.setLong(1, idUsuario);
            comando.executeUpdate();
        }
    }

    /**
     * Desfaz a transacao. Se o proprio rollback falhar, a falha e anexada
     * como suppressed no erro original, para nao esconder a causa real.
     * Aceita Exception porque o rollback tambem precisa acontecer quando o
     * erro no meio da transacao e uma RuntimeException.
     */
    private void desfazer(Connection conexao, Exception erroOriginal) {
        try {
            conexao.rollback();
        } catch (SQLException falhaNoRollback) {
            erroOriginal.addSuppressed(falhaNoRollback);
        }
    }

    /** Monta o Investidor a partir da linha atual do ResultSet. */
    private Investidor mapearInvestidor(ResultSet resultado) throws SQLException {
        return new Investidor(
                resultado.getLong("id"),
                resultado.getString("nm_usuario"),
                resultado.getString("ds_email"),
                resultado.getString("ds_senha_hash"),
                resultado.getString("cpf_usuario"));
    }

    /**
     * Traduz o erro do Oracle em PersistenciaException, mantendo sempre a
     * SQLException original como causa.
     *
     * @param e erro devolvido pelo driver
     * @param operacao operacao em andamento, usada na mensagem generica
     * @return a excecao pronta para ser lancada
     */
    private PersistenciaException traduzirErro(SQLException e, String operacao) {
        int codigo = e.getErrorCode();

        String mensagem = switch (codigo) {
            case 1 -> traduzirViolacaoDeUnicidade(e);
            case 2292 -> traduzirViolacaoDeChaveEstrangeira(e);
            case 1400 -> "Campo obrigatório não informado";
            case 12899 -> "Valor maior que o tamanho permitido (o CPF deve ter 11 dígitos, sem pontuação)";
            case 1017 -> "Usuário ou senha do banco inválidos";
            case 17002 -> "Não foi possível conectar ao servidor Oracle";
            // Inclui a mensagem do driver: erros de conexao (driver ausente, SID ou
            // URL invalidos) vem com codigo fora do switch, as vezes 0.
            default -> "Falha ao " + operacao + " (erro Oracle " + codigo + "): " + e.getMessage();
        };

        return new PersistenciaException(mensagem, e);
    }

    /**
     * Descobre qual constraint unica foi violada (erro ORA-00001) olhando o
     * nome da constraint dentro da mensagem do driver.
     */
    private String traduzirViolacaoDeUnicidade(SQLException e) {
        String detalhe = detalharErro(e);

        if (detalhe.contains("UN_SIP_USUARIO_EMAIL")) {
            return "E-mail já cadastrado";
        }
        if (detalhe.contains("UN_SIP_USUARIO_CPF")) {
            return "CPF já cadastrado";
        }
        if (detalhe.contains("PK_SIP_USUARIO")) {
            return "Conflito de id: o contador IDENTITY de T_SIP_USUARIO está dessincronizado "
                    + "com os dados inseridos pelo script DML";
        }
        return "Registro duplicado";
    }

    /**
     * Descobre qual chave estrangeira impediu a operacao (erro ORA-02292).
     * A exclusao do investidor esbarra na carteira; a exclusao do usuario
     * ainda pode esbarrar na empresa vinculada a ele.
     */
    private String traduzirViolacaoDeChaveEstrangeira(SQLException e) {
        String detalhe = detalharErro(e);

        if (detalhe.contains("FK_SIP_CARTEIRA_INVEST")) {
            return "Investidor possui carteira vinculada e não pode ser excluído";
        }
        if (detalhe.contains("FK_SIP_EMPRESA_USER")) {
            return "Usuário vinculado a uma empresa e não pode ser excluído";
        }
        return "Registro possui dependências e não pode ser excluído";
    }

    /** Mensagem do driver em maiusculas, pronta para procurar nome de constraint. */
    private String detalharErro(SQLException e) {
        return e.getMessage() == null ? "" : e.getMessage().toUpperCase();
    }
}
