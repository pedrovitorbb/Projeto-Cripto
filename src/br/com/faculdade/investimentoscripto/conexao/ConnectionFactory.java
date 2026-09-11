package br.com.faculdade.investimentoscripto.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Fabrica de conexoes JDBC com o banco Oracle da FIAP.
 *
 * <p><b>Driver:</b> o projeto nao usa ferramenta de build, entao o arquivo
 * {@code ojdbc11.jar} precisa ser adicionado manualmente como biblioteca do
 * modulo no IntelliJ (File &gt; Project Structure &gt; Modules &gt; Dependencies).
 * O {@code .gitignore} ignora {@code *.jar}, portanto o driver nao vai para o
 * repositorio e cada integrante precisa adiciona-lo na sua maquina.</p>
 *
 * <p><b>Credenciais:</b> usuario e senha nunca ficam no codigo. Sao lidos das
 * variaveis de ambiente {@code FIAP_DB_USER} e {@code FIAP_DB_PASSWORD}, que
 * podem ser configuradas na Run Configuration do IntelliJ
 * (Run &gt; Edit Configurations &gt; Environment variables). A variavel opcional
 * {@code FIAP_DB_URL} substitui a URL padrao, util para apontar para outro
 * servidor.</p>
 *
 * <p><b>Fechamento:</b> cada chamada a {@link #getConnection()} abre uma conexao
 * nova. Quem chama e responsavel por fecha-la, de preferencia usando
 * try-with-resources.</p>
 */
public final class ConnectionFactory {

    /** URL padrao do Oracle da FIAP, usada quando FIAP_DB_URL nao esta definida. */
    private static final String URL_PADRAO = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL";

    private static final String VAR_URL = "FIAP_DB_URL";
    private static final String VAR_USUARIO = "FIAP_DB_USER";
    private static final String VAR_SENHA = "FIAP_DB_PASSWORD";

    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    private static final String SQL_TESTE = "SELECT 1 FROM DUAL";

    /** Classe utilitaria: nao deve ser instanciada. */
    private ConnectionFactory() {
    }

    /**
     * Abre uma conexao nova com o banco Oracle.
     *
     * @return conexao aberta, que deve ser fechada por quem chamou
     * @throws SQLException se o driver nao estiver no classpath ou a conexao falhar
     * @throws IllegalStateException se FIAP_DB_USER ou FIAP_DB_PASSWORD nao estiverem definidas
     */
    public static Connection getConnection() throws SQLException {
        String usuario = obterVariavelObrigatoria(VAR_USUARIO);
        String senha = obterVariavelObrigatoria(VAR_SENHA);

        String url = System.getenv(VAR_URL);
        if (url == null || url.isBlank()) {
            url = URL_PADRAO;
        }

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Driver Oracle nao encontrado. Adicione o ojdbc11.jar ao classpath "
                            + "(como biblioteca do modulo no IntelliJ).", e);
        }

        return DriverManager.getConnection(url, usuario, senha);
    }

    /**
     * Testa a conexao executando SELECT 1 FROM DUAL.
     *
     * @return true se o banco respondeu 1
     * @throws SQLException se a conexao ou a consulta falharem
     */
    public static boolean testarConexao() throws SQLException {
        try (Connection conexao = getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_TESTE);
             ResultSet resultado = comando.executeQuery()) {

            return resultado.next() && resultado.getInt(1) == 1;
        }
    }

    /**
     * Le uma variavel de ambiente obrigatoria.
     *
     * @param nome nome da variavel
     * @return o valor da variavel
     * @throws IllegalStateException se a variavel nao estiver definida ou estiver vazia
     */
    private static String obterVariavelObrigatoria(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            // A mensagem cita apenas o NOME da variavel, nunca o valor.
            throw new IllegalStateException(
                    "Variavel de ambiente nao definida: " + nome
                            + ". Configure-a na Run Configuration do IntelliJ.");
        }
        return valor;
    }
}
