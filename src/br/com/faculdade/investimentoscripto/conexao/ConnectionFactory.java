package br.com.faculdade.investimentoscripto.conexao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexoes JDBC com o banco Oracle da FIAP.
 *
 * <p><b>Driver:</b> o projeto nao usa ferramenta de build, entao o arquivo
 * {@code ojdbc11.jar} precisa ser adicionado manualmente como biblioteca do
 * modulo no IntelliJ (File &gt; Project Structure &gt; Modules &gt; Dependencies).
 * O {@code .gitignore} ignora {@code *.jar}, portanto o driver nao vai para o
 * repositorio e cada integrante precisa adiciona-lo na sua maquina.</p>
 *
 * <p><b>Credenciais:</b> nada fica no codigo. Cada integrante usa o proprio
 * schema, configurado no arquivo {@code db.properties} (chaves {@code db.url},
 * {@code db.user} e {@code db.password}), procurado na pasta de execucao e, se
 * nao achar, no classpath. As variaveis de ambiente {@code FIAP_DB_URL},
 * {@code FIAP_DB_USER} e {@code FIAP_DB_PASSWORD}, quando definidas, tem
 * prioridade sobre o arquivo. O caminho do arquivo pode ser trocado pela
 * variavel {@code FIAP_DB_CONFIG}.</p>
 *
 * <p><b>Fechamento:</b> cada chamada a {@link #getConnection()} abre uma conexao
 * nova. Quem chama e responsavel por fecha-la, de preferencia usando
 * try-with-resources.</p>
 */
public final class ConnectionFactory {

    private static final String ARQUIVO_PADRAO = "db.properties";

    private static final String VAR_URL = "FIAP_DB_URL";
    private static final String VAR_USUARIO = "FIAP_DB_USER";
    private static final String VAR_SENHA = "FIAP_DB_PASSWORD";
    private static final String VAR_ARQUIVO = "FIAP_DB_CONFIG";

    private static final String CHAVE_URL = "db.url";
    private static final String CHAVE_USUARIO = "db.user";
    private static final String CHAVE_SENHA = "db.password";

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
     * @throws IllegalStateException se url, usuario ou senha nao estiverem configurados
     */
    public static Connection getConnection() throws SQLException {
        Properties config = carregarArquivo();

        String url = obterObrigatorio(VAR_URL, CHAVE_URL, config);
        String usuario = obterObrigatorio(VAR_USUARIO, CHAVE_USUARIO, config);
        String senha = obterObrigatorio(VAR_SENHA, CHAVE_SENHA, config);

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
     * Carrega o db.properties. Se o arquivo nao existir, devolve propriedades
     * vazias e a configuracao precisa vir das variaveis de ambiente.
     */
    private static Properties carregarArquivo() {
        Properties config = new Properties();
        String caminho = System.getenv(VAR_ARQUIVO);
        Path arquivo = Path.of(caminho == null || caminho.isBlank() ? ARQUIVO_PADRAO : caminho);

        try {
            if (Files.isRegularFile(arquivo)) {
                try (InputStream in = Files.newInputStream(arquivo)) {
                    config.load(in);
                }
            } else {
                try (InputStream in = ConnectionFactory.class.getClassLoader()
                        .getResourceAsStream(ARQUIVO_PADRAO)) {
                    if (in != null) {
                        config.load(in);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler o arquivo " + arquivo, e);
        }
        return config;
    }

    /**
     * Le um valor: variavel de ambiente primeiro, depois o db.properties.
     *
     * @throws IllegalStateException se nao houver valor ou se ele ainda for o placeholder
     */
    private static String obterObrigatorio(String variavel, String chave, Properties config) {
        String valor = System.getenv(variavel);
        if (valor == null || valor.isBlank()) {
            valor = config.getProperty(chave);
        }
        if (valor == null || valor.isBlank()
                || valor.equals("rmXXXXXX") || valor.equals("SUA_SENHA_AQUI")) {
            // A mensagem cita apenas o NOME da configuracao, nunca o valor.
            throw new IllegalStateException(
                    "Configuracao ausente: preencha '" + chave + "' no db.properties "
                            + "ou defina a variavel de ambiente " + variavel + ".");
        }
        return valor.trim();
    }
}
