package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.dao.AtivoCriptoDAO;
import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.InvestidorDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Empresa;
import br.com.faculdade.investimentoscripto.model.Investidor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Teste executavel do AtivoCriptoDAO, rodando no schema configurado no db.properties.
 *
 * <p>Nao depende do DML: cria investidor, empresa e carteira temporarios para
 * satisfazer as FKs, exercita todos os metodos do DAO, testa dois erros do
 * banco e remove tudo no final, mesmo se algum passo falhar.</p>
 */
public class TesteAtivoCriptoDAO {

    private static final AtivoCriptoDAO ativoDAO = new AtivoCriptoDAO();
    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final InvestidorDAO investidorDAO = new InvestidorDAO();

    private static final String TICKER = "DAOTEST";
    private static final long ID_CARTEIRA_INEXISTENTE = 999999L;

    private static final String ERRO_ATIVO_DUPLICADO = "A carteira já possui esse ativo";
    private static final String ERRO_CARTEIRA_INEXISTENTE = "A carteira informada não existe";

    public static void main(String[] args) {
        titulo("TESTE DE CONEXAO");
        try {
            if (!ConnectionFactory.testarConexao()) {
                System.out.println("O banco nao respondeu ao teste de conexao.");
                return;
            }
            System.out.println("Conexao realizada com sucesso.");
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Nao foi possivel conectar ao Oracle.");
            System.out.println("Erro: " + e.getMessage());
            return;
        }

        Investidor investidor = null;
        Empresa empresa = null;
        Carteira carteira = null;
        boolean ativoCriado = false;

        try {
            titulo("PREPARACAO (investidor, empresa e carteira temporarios)");
            String sufixo = String.valueOf(System.currentTimeMillis());
            investidor = new Investidor(null, "Investidor Ativo Teste",
                    "ativo." + sufixo + "@teste.com", "hash_teste",
                    sufixo.substring(sufixo.length() - 11));
            investidorDAO.inserir(investidor);
            empresa = inserirEmpresaTemporaria(investidor.getId(), sufixo);
            carteira = new Carteira(null, new BigDecimal("1000.00"), investidor, empresa);
            carteiraDAO.inserir(carteira);
            long idCarteira = carteira.getId();
            System.out.println("Carteira temporaria id=" + idCarteira);

            long totalAntes = ativoDAO.contar();

            titulo("INSERT");
            AtivoCripto ativo = new AtivoCripto(idCarteira, " " + TICKER.toLowerCase() + " ",
                    new BigDecimal("2.50000000"), new BigDecimal("125.50"), new BigDecimal("313.75"));
            ativoDAO.inserir(ativo);
            ativoCriado = true;
            System.out.println("Ativo inserido (ticker informado com espacos e minusculas).");

            titulo("SELECT por chave composta");
            AtivoCripto lido = ativoDAO.buscarPorChave(idCarteira, TICKER);
            exigir(lido != null, "O ativo inserido nao foi encontrado.");
            exigir(TICKER.equals(lido.getTicker()), "O ticker nao foi normalizado para maiusculas.");
            exigir(lido.getQuantidade().compareTo(new BigDecimal("2.5")) == 0, "Quantidade lida difere da gravada.");
            exigir(ativoDAO.existe(idCarteira, TICKER), "existe() deveria retornar true.");
            exigir(!ativoDAO.existe(idCarteira, "NAOEXISTE"), "existe() deveria retornar false.");
            mostrar(lido);

            titulo("UPDATE");
            lido.setQuantidade(new BigDecimal("3.00000000"));
            lido.setPrecoMedioCompra(new BigDecimal("130.00"));
            lido.setValorTotalInvestido(new BigDecimal("390.00"));
            exigir(ativoDAO.atualizar(lido), "Nenhum ativo foi atualizado.");
            AtivoCripto atualizado = ativoDAO.buscarPorChave(idCarteira, TICKER);
            exigir(atualizado.getQuantidade().compareTo(new BigDecimal("3")) == 0
                            && atualizado.getValorTotalInvestido().compareTo(new BigDecimal("390.00")) == 0,
                    "Atualizacao nao confirmada.");
            mostrar(atualizado);

            titulo("SELECT listas");
            List<AtivoCripto> daCarteira = ativoDAO.listarPorCarteira(idCarteira);
            List<AtivoCripto> doTicker = ativoDAO.listarPorTicker(TICKER);
            System.out.println("listarTodos: " + ativoDAO.listarTodos().size() + " ativo(s)");
            System.out.println("listarPorCarteira: " + daCarteira.size());
            System.out.println("listarPorTicker: " + doTicker.size());
            System.out.println("contar: " + ativoDAO.contar());
            exigir(daCarteira.size() == 1 && doTicker.size() == 1, "Filtros retornaram quantidade inesperada.");
            exigir(ativoDAO.contar() == totalAntes + 1, "contar() nao refletiu a insercao.");

            titulo("ERROS DO BANCO");
            esperarErro(() -> ativoDAO.inserir(new AtivoCripto(idCarteira, TICKER,
                            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)),
                    ERRO_ATIVO_DUPLICADO);
            esperarErro(() -> ativoDAO.inserir(new AtivoCripto(ID_CARTEIRA_INEXISTENTE, "X" + TICKER,
                            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)),
                    ERRO_CARTEIRA_INEXISTENTE);

            titulo("DELETE");
            exigir(ativoDAO.deletar(idCarteira, TICKER), "Nenhum ativo foi excluido.");
            ativoCriado = false;
            exigir(!ativoDAO.existe(idCarteira, TICKER), "O ativo ainda existe.");
            exigir(!ativoDAO.deletar(idCarteira, TICKER), "Segundo deletar() deveria retornar false.");
            System.out.println("Exclusao confirmada.");

            System.out.println("\nTodos os testes do AtivoCriptoDAO foram concluidos com sucesso.");
        } catch (Exception e) {
            System.out.println("\nOs testes foram interrompidos.");
            System.out.println("Erro: " + e.getMessage());
        } finally {
            limpar(ativoCriado, carteira, empresa, investidor);
        }
    }

    /** Ainda nao ha EmpresaDAO: insercao direta, so para satisfazer a FK da carteira. */
    private static Empresa inserirEmpresaTemporaria(long idUsuario, String sufixo) throws SQLException {
        String razaoSocial = "Empresa Teste Ativo";
        String cnpj = sufixo.substring(sufixo.length() - 13) + "1";
        String sql = "INSERT INTO T_SIP_EMPRESA (nm_razao_social, nr_cnpj, id_usuario) VALUES (?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql, new String[]{"ID_EMPRESA"})) {
            comando.setString(1, razaoSocial);
            comando.setString(2, cnpj);
            comando.setLong(3, idUsuario);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                chaves.next();
                return new Empresa(chaves.getLong(1), razaoSocial, cnpj);
            }
        }
    }

    /** Remove os dados temporarios na ordem inversa das dependencias. */
    private static void limpar(boolean ativoCriado, Carteira carteira, Empresa empresa, Investidor investidor) {
        try {
            if (ativoCriado && carteira != null && carteira.getId() != null) {
                ativoDAO.deletar(carteira.getId(), TICKER);
            }
            if (carteira != null && carteira.getId() != null) {
                carteiraDAO.deletar(carteira.getId());
            }
            if (empresa != null) {
                try (Connection conexao = ConnectionFactory.getConnection();
                     PreparedStatement comando = conexao.prepareStatement(
                             "DELETE FROM T_SIP_EMPRESA WHERE id_empresa = ?")) {
                    comando.setLong(1, empresa.getId());
                    comando.executeUpdate();
                }
            }
            if (investidor != null && investidor.getId() != null) {
                investidorDAO.excluir(investidor.getId());
            }
        } catch (SQLException | PersistenciaException e) {
            System.out.println("Aviso: nao foi possivel remover os dados temporarios: " + e.getMessage());
        }
    }

    /** O toString do AtivoCripto e de dominio (usa nome e cotacao); aqui mostramos os campos da tabela. */
    private static void mostrar(AtivoCripto ativo) {
        System.out.println("carteira=" + ativo.getIdCarteira()
                + ", ticker=" + ativo.getTicker()
                + ", quantidade=" + ativo.getQuantidade()
                + ", precoMedio=" + ativo.getPrecoMedioCompra()
                + ", totalInvestido=" + ativo.getValorTotalInvestido());
    }

    private interface Operacao {
        void executar() throws PersistenciaException;
    }

    /** Exige que a operacao falhe com a mensagem de erro traduzida esperada. */
    private static void esperarErro(Operacao operacao, String mensagemEsperada) {
        try {
            operacao.executar();
        } catch (PersistenciaException e) {
            exigir(mensagemEsperada.equals(e.getMessage()),
                    "Mensagem inesperada: " + e.getMessage());
            System.out.println("Erro esperado recebido: " + e.getMessage());
            return;
        }
        throw new IllegalStateException("Era esperado o erro: " + mensagemEsperada);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException(mensagem);
        }
    }

    private static void titulo(String titulo) {
        System.out.println("\n===============================");
        System.out.println(titulo);
        System.out.println("===============================");
    }
}
