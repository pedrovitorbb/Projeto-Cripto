package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.InvestidorDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
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
 * Teste executavel do CarteiraDAO, rodando no schema configurado no db.properties.
 *
 * <p>Nao depende do DML: cria um investidor e uma empresa temporarios para
 * satisfazer as FKs, exercita todos os metodos do DAO e remove tudo no final,
 * mesmo se algum passo falhar.</p>
 */
public class TesteCarteiraDAO {

    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final InvestidorDAO investidorDAO = new InvestidorDAO();

    public static void main(String[] args) {
        try {
            if (!ConnectionFactory.testarConexao()) {
                System.out.println("O banco nao respondeu ao teste de conexao.");
                return;
            }
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Nao foi possivel conectar ao Oracle.");
            System.out.println("Erro: " + e.getMessage());
            return;
        }

        Investidor investidor = null;
        Empresa empresa = null;
        Carteira carteira = null;

        try {
            titulo("PREPARACAO (investidor e empresa temporarios)");
            String sufixo = String.valueOf(System.currentTimeMillis());
            investidor = new Investidor(null, "Investidor Carteira Teste",
                    "carteira." + sufixo + "@teste.com", "hash_teste",
                    sufixo.substring(sufixo.length() - 11));
            investidorDAO.inserir(investidor);
            empresa = inserirEmpresaTemporaria(investidor.getId(), sufixo);
            System.out.println("Investidor id=" + investidor.getId() + ", empresa id=" + empresa.getId());

            titulo("INSERT");
            carteira = new Carteira(null, new BigDecimal("1500.50"), investidor, empresa);
            carteiraDAO.inserir(carteira);
            exigir(carteira.getId() != null, "O banco nao devolveu o id da carteira.");
            System.out.println("Inserida: " + carteira);

            titulo("SELECT por id");
            Carteira lida = carteiraDAO.buscarPorId(carteira.getId());
            exigir(lida != null, "Carteira inserida nao foi encontrada.");
            exigir(lida.getSaldoTotalGeral().compareTo(new BigDecimal("1500.50")) == 0,
                    "Saldo lido difere do gravado.");
            exigir(lida.getEmpresa().getCnpj() != null, "Empresa nao foi carregada.");
            System.out.println(lida + " | empresa=" + lida.getEmpresa().getRazaoSocial()
                    + " | investidor=" + lida.getInvestidor().getNome());

            titulo("UPDATE");
            carteira.setSaldoTotalGeral(new BigDecimal("2750.75"));
            exigir(carteiraDAO.atualizar(carteira), "Nenhuma carteira foi atualizada.");
            Carteira atualizada = carteiraDAO.buscarPorId(carteira.getId());
            exigir(atualizada.getSaldoTotalGeral().compareTo(new BigDecimal("2750.75")) == 0,
                    "Atualizacao nao confirmada.");
            System.out.println("Atualizada: " + atualizada);

            titulo("SELECT listas e analiticos");
            List<Carteira> todas = carteiraDAO.listarTodos();
            List<Carteira> doInvestidor = carteiraDAO.listarPorInvestidor(investidor.getId());
            List<Carteira> daEmpresa = carteiraDAO.listarPorEmpresa(empresa.getId());
            System.out.println("listarTodos: " + todas.size() + " carteira(s)");
            System.out.println("listarPorInvestidor: " + doInvestidor);
            System.out.println("listarPorEmpresa: " + daEmpresa);
            System.out.println("contarTotalCarteiras: " + carteiraDAO.contarTotalCarteiras());
            System.out.println("somarSaldoTotal: " + carteiraDAO.somarSaldoTotal());
            exigir(doInvestidor.size() == 1 && daEmpresa.size() == 1,
                    "Filtros por FK retornaram quantidade inesperada.");

            titulo("DELETE");
            exigir(carteiraDAO.deletar(carteira.getId()), "Nenhuma carteira foi excluida.");
            exigir(carteiraDAO.buscarPorId(carteira.getId()) == null, "A carteira ainda existe.");
            System.out.println("Exclusao confirmada.");
            carteira = null;

            System.out.println("\nTodos os testes do CarteiraDAO foram concluidos com sucesso.");
        } catch (Exception e) {
            System.out.println("\nOs testes foram interrompidos.");
            System.out.println("Erro: " + e.getMessage());
        } finally {
            limpar(carteira, empresa, investidor);
        }
    }

    /** Ainda nao ha EmpresaDAO: insercao direta, so para satisfazer a FK da carteira. */
    private static Empresa inserirEmpresaTemporaria(long idUsuario, String sufixo) throws SQLException {
        String razaoSocial = "Empresa Teste Carteira";
        String cnpj = sufixo.substring(sufixo.length() - 13) + "0";
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
    private static void limpar(Carteira carteira, Empresa empresa, Investidor investidor) {
        try {
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
