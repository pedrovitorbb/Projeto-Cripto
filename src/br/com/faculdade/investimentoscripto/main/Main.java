package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.dao.InvestidorDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Investidor;

import java.sql.SQLException;
import java.util.List;

public class Main {

    private static final InvestidorDAO investidorDAO = new InvestidorDAO();
    private static Investidor investidorTeste;

    public static void main(String[] args) {
        if (!testarConexao()) {
            System.out.println("\nOs testes do CRUD nao foram executados porque nao houve conexao com o banco.");
            return;
        }

        try {
            testarInsercao();
            testarConsulta();
            testarAtualizacao();
            confirmarAtualizacao();
            testarExclusao();
            confirmarExclusao();

            System.out.println("\nTodos os testes do CRUD foram concluidos com sucesso.");
        } catch (Exception e) {
            System.out.println("\nOs testes foram interrompidos.");
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static boolean testarConexao() {
        mostrarTitulo("TESTE DE CONEXAO");

        try {
            if (ConnectionFactory.testarConexao()) {
                System.out.println("Conexao realizada com sucesso.");
                return true;
            }

            System.out.println("O banco nao respondeu ao teste de conexao.");
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Nao foi possivel conectar ao Oracle.");
            System.out.println("Erro: " + e.getMessage());
        }

        return false;
    }

    private static void testarInsercao() throws PersistenciaException {
        mostrarTitulo("TESTE DE INSERT");

        String numeroTeste = String.valueOf(System.currentTimeMillis());
        String cpfTeste = numeroTeste.substring(numeroTeste.length() - 11);

        investidorTeste = new Investidor(
                null,
                "Investidor Teste",
                "investidor." + numeroTeste + "@teste.com",
                "hash_teste_123",
                cpfTeste);

        investidorDAO.inserir(investidorTeste);

        if (investidorTeste.getId() == null) {
            throw new IllegalStateException("O banco nao devolveu o ID do investidor.");
        }

        System.out.println("Investidor inserido com sucesso.");
        System.out.println("ID gerado: " + investidorTeste.getId());
    }

    private static void testarConsulta() throws PersistenciaException {
        mostrarTitulo("TESTE DE SELECT");

        List<Investidor> investidores = investidorDAO.listar();
        System.out.println("Quantidade de investidores cadastrados: " + investidores.size());

        Investidor investidorEncontrado = investidorDAO.buscarPorId(investidorTeste.getId());
        if (investidorEncontrado == null) {
            throw new IllegalStateException("O investidor inserido nao foi encontrado.");
        }

        mostrarInvestidor(investidorEncontrado);
    }

    private static void testarAtualizacao() throws PersistenciaException {
        mostrarTitulo("TESTE DE UPDATE");

        investidorTeste.setNome("Investidor Teste Alterado");
        investidorTeste.setEmail("alterado." + System.currentTimeMillis() + "@teste.com");

        boolean alterou = investidorDAO.alterar(investidorTeste);
        if (!alterou) {
            throw new IllegalStateException("Nenhum investidor foi atualizado.");
        }

        System.out.println("Investidor atualizado com sucesso.");
    }

    private static void confirmarAtualizacao() throws PersistenciaException {
        mostrarTitulo("CONFIRMACAO DO UPDATE");

        Investidor investidorAtualizado = investidorDAO.buscarPorId(investidorTeste.getId());
        if (investidorAtualizado == null
                || !investidorTeste.getNome().equals(investidorAtualizado.getNome())
                || !investidorTeste.getEmail().equals(investidorAtualizado.getEmail())) {
            throw new IllegalStateException("A alteracao nao foi confirmada pela consulta.");
        }

        mostrarInvestidor(investidorAtualizado);
        System.out.println("Alteracao confirmada.");
    }

    private static void testarExclusao() throws PersistenciaException {
        mostrarTitulo("TESTE DE DELETE");

        boolean excluiu = investidorDAO.excluir(investidorTeste.getId());
        if (!excluiu) {
            throw new IllegalStateException("Nenhum investidor foi excluido.");
        }

        System.out.println("Investidor excluido com sucesso.");
    }

    private static void confirmarExclusao() throws PersistenciaException {
        mostrarTitulo("CONFIRMACAO DO DELETE");

        Investidor investidorEncontrado = investidorDAO.buscarPorId(investidorTeste.getId());
        if (investidorEncontrado != null) {
            throw new IllegalStateException("O investidor ainda existe no banco.");
        }

        System.out.println("Registro nao encontrado. Exclusao confirmada.");
    }

    private static void mostrarInvestidor(Investidor investidor) {
        System.out.println("ID: " + investidor.getId());
        System.out.println("Nome: " + investidor.getNome());
        System.out.println("Email: " + investidor.getEmail());
        System.out.println("CPF: " + investidor.getCpf());
    }

    private static void mostrarTitulo(String titulo) {
        System.out.println("\n===============================");
        System.out.println(titulo);
        System.out.println("===============================");
    }
}
