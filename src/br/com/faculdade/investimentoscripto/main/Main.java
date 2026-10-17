package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.dao.AtivoCriptoDAO;
import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.EmpresaDAO;
import br.com.faculdade.investimentoscripto.dao.InvestidorDAO;
import br.com.faculdade.investimentoscripto.dao.TransacaoDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Empresa;
import br.com.faculdade.investimentoscripto.model.Investidor;
import br.com.faculdade.investimentoscripto.model.Transacao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Main {

    private static final InvestidorDAO investidorDAO = new InvestidorDAO();
    private static final EmpresaDAO empresaDAO = new EmpresaDAO();
    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final AtivoCriptoDAO ativoDAO = new AtivoCriptoDAO();
    private static final TransacaoDAO transacaoDAO = new TransacaoDAO();

    private static final String TICKER_TESTE = "FASE6";

    private static Investidor investidor;
    private static Empresa empresa1;
    private static Empresa empresa2;
    private static Carteira carteira;
    private static AtivoCripto ativo;
    private static Transacao transacao;

    public static void main(String[] args) {
        boolean sucesso = false;
        try {
            preparar();
            inserir();
            consultar();
            atualizarETestarRestricao();
            excluir();
            System.out.println("\nTodos os blocos de teste passaram (6 tabelas: usuario, investidor, empresa, carteira, ativo e transacao).");
            sucesso = true;
        } catch (Exception e) {
            System.out.println("\nTeste interrompido: " + e.getMessage());
        } finally {
            limparDadosTemporarios();
        }
        if (!sucesso) {
            System.exit(1);
        }
    }

    private static void preparar() throws SQLException, PersistenciaException {
        titulo("1. CONEXAO E USUARIO DE TESTE");
        exigir(ConnectionFactory.testarConexao(), "O Oracle nao respondeu ao teste de conexao.");

        String sufixo = String.valueOf(System.currentTimeMillis());
        investidor = new Investidor(null, "Investidor Fase6", "fase6." + sufixo + "@teste.com",
                "hash_teste", sufixo.substring(sufixo.length() - 11));
        investidorDAO.inserir(investidor);
        exigir(investidor.getId() != null, "O usuario nao recebeu um id.");
        System.out.println("Investidor criado: " + investidor.getId());
    }

    private static void inserir() throws PersistenciaException {
        titulo("2. INSERT DE EMPRESAS, CARTEIRA, ATIVO E TRANSACAO");
        String sufixo = String.valueOf(System.currentTimeMillis());
        empresa1 = new Empresa(null, "Empresa Fase6 Alfa", sufixo + "1", investidor.getId());
        empresa2 = new Empresa(null, "Empresa Fase6 Beta", sufixo + "2", investidor.getId());
        empresaDAO.inserir(empresa1);
        empresaDAO.inserir(empresa2);
        exigir(empresa1.getId() != null && empresa2.getId() != null,
                "O Oracle nao devolveu os ids das empresas.");

        carteira = new Carteira(null, new BigDecimal("100.00"), investidor, empresa1);
        carteiraDAO.inserir(carteira);
        exigir(carteira.getId() != null, "A carteira nao recebeu um id.");

        ativo = new AtivoCripto(carteira.getId(), TICKER_TESTE, new BigDecimal("2.50000000"),
                new BigDecimal("125.50"), new BigDecimal("313.75"));
        ativoDAO.inserir(ativo);

        transacao = new Transacao(null, carteira.getId(), Transacao.Tipo.COMPRA,
                new BigDecimal("2.50000000"), new BigDecimal("125.50"),
                "hash_fase6_" + sufixo, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        transacaoDAO.inserir(transacao);
        exigir(transacao.getId() != null, "A transacao nao recebeu um id.");

        System.out.println("Empresas: " + empresa1.getId() + ", " + empresa2.getId()
                + "; carteira: " + carteira.getId() + "; ativo: " + TICKER_TESTE
                + "; transacao: " + transacao.getId());
    }

    private static void consultar() throws PersistenciaException {
        titulo("3. SELECT POR ID, NOME E LISTAS");
        Empresa lida = empresaDAO.buscarPorId(empresa1.getId());
        exigir(lida != null && lida.getCnpj().equals(empresa1.getCnpj())
                        && lida.getIdUsuario().equals(investidor.getId()),
                "Os dados da empresa lidos do Oracle estao incorretos.");
        exigir(empresaDAO.buscarPorId(Long.MAX_VALUE) == null,
                "A busca por id inexistente deveria retornar null.");

        List<Empresa> encontradas = empresaDAO.buscarPorNome("Empresa Fase6");
        exigir(contem(encontradas, empresa1.getId()) && contem(encontradas, empresa2.getId()),
                "A busca por nome nao retornou as duas empresas.");
        List<Empresa> todas = empresaDAO.listarTodas();
        exigir(contem(todas, empresa1.getId()) && contem(todas, empresa2.getId()),
                "A listagem nao retornou as empresas inseridas.");
        exigir(carteiraDAO.buscarPorId(carteira.getId()) != null,
                "A carteira vinculada nao foi encontrada.");
        System.out.println("Empresas localizadas por nome: " + encontradas.size());

        AtivoCripto ativoLido = ativoDAO.buscarPorChave(carteira.getId(), TICKER_TESTE);
        exigir(ativoLido != null && ativoLido.getQuantidade().compareTo(new BigDecimal("2.5")) == 0,
                "O ativo lido do Oracle esta incorreto.");
        exigir(ativoDAO.listarPorCarteira(carteira.getId()).size() == 1,
                "A carteira deveria ter exatamente um ativo.");

        Transacao transacaoLida = transacaoDAO.buscarPorId(transacao.getId());
        exigir(transacaoLida != null && transacaoLida.getIdCarteira().equals(carteira.getId())
                        && transacaoLida.getTipo() == Transacao.Tipo.COMPRA
                        && transacaoLida.getValorUnitario().compareTo(new BigDecimal("125.50")) == 0,
                "A transacao lida do Oracle esta incorreta.");
        exigir(transacaoDAO.listarPorCarteira(carteira.getId()).size() == 1,
                "A carteira deveria ter exatamente uma transacao.");
        System.out.println("Ativo e transacao confirmados por SELECT.");
    }

    private static void atualizarETestarRestricao() throws PersistenciaException {
        titulo("4. UPDATE E CHAVE ESTRANGEIRA");
        empresa1.setRazaoSocial("Empresa Fase6 Alfa Atualizada");
        exigir(empresaDAO.atualizar(empresa1), "Nenhuma empresa foi atualizada.");
        exigir(empresa1.getRazaoSocial().equals(
                empresaDAO.buscarPorId(empresa1.getId()).getRazaoSocial()),
                "O UPDATE da empresa nao foi confirmado por SELECT.");

        ativo.setQuantidade(new BigDecimal("3.00000000"));
        ativo.setValorTotalInvestido(new BigDecimal("376.50"));
        exigir(ativoDAO.atualizar(ativo), "Nenhum ativo foi atualizado.");
        exigir(ativoDAO.buscarPorChave(carteira.getId(), TICKER_TESTE).getQuantidade()
                        .compareTo(new BigDecimal("3")) == 0,
                "O UPDATE do ativo nao foi confirmado por SELECT.");

        transacao.setTipo(Transacao.Tipo.VENDA);
        exigir(transacaoDAO.atualizar(transacao), "Nenhuma transacao foi atualizada.");
        exigir(transacaoDAO.buscarPorId(transacao.getId()).getTipo() == Transacao.Tipo.VENDA,
                "O UPDATE da transacao nao foi confirmado por SELECT.");

        investidor.setNome("Investidor Fase6 Atualizado");
        exigir(investidorDAO.alterar(investidor), "Nenhum investidor foi atualizado.");
        exigir(investidor.getNome().equals(investidorDAO.buscarPorId(investidor.getId()).getNome()),
                "O UPDATE do investidor nao foi confirmado.");

        try {
            carteiraDAO.deletar(carteira.getId());
            throw new IllegalStateException("O Oracle permitiu excluir carteira com ativo e transacao.");
        } catch (PersistenciaException e) {
            if (!(e.getCause() instanceof SQLException sql) || sql.getErrorCode() != 2292) {
                throw e;
            }
            System.out.println("Restricao de ativo/transacao na carteira confirmada (ORA-02292).");
        }

        try {
            empresaDAO.excluir(empresa1.getId());
            throw new IllegalStateException("O Oracle permitiu excluir empresa com carteira.");
        } catch (PersistenciaException e) {
            if (!(e.getCause() instanceof SQLException sql) || sql.getErrorCode() != 2292) {
                throw e;
            }
            System.out.println("Restricao de carteira confirmada (ORA-02292).");
        }
    }

    private static void excluir() throws PersistenciaException {
        titulo("5. DELETE E CONFIRMACAO");
        exigir(transacaoDAO.deletar(transacao.getId()), "A transacao nao foi excluida.");
        exigir(transacaoDAO.buscarPorId(transacao.getId()) == null,
                "A transacao ainda existe no Oracle.");
        transacao = null;
        exigir(ativoDAO.deletar(carteira.getId(), TICKER_TESTE), "O ativo nao foi excluido.");
        exigir(!ativoDAO.existe(carteira.getId(), TICKER_TESTE), "O ativo ainda existe no Oracle.");
        ativo = null;

        exigir(carteiraDAO.deletar(carteira.getId()), "A carteira nao foi excluida.");
        exigir(carteiraDAO.buscarPorId(carteira.getId()) == null,
                "A carteira ainda existe no Oracle.");
        carteira = null;

        exigir(empresaDAO.excluir(empresa1.getId()), "A primeira empresa nao foi excluida.");
        exigir(empresaDAO.buscarPorId(empresa1.getId()) == null,
                "A primeira empresa ainda existe no Oracle.");
        empresa1 = null;
        exigir(empresaDAO.excluir(empresa2.getId()), "A segunda empresa nao foi excluida.");
        exigir(empresaDAO.buscarPorId(empresa2.getId()) == null,
                "A segunda empresa ainda existe no Oracle.");
        empresa2 = null;

        exigir(investidorDAO.excluir(investidor.getId()), "O investidor nao foi excluido.");
        exigir(investidorDAO.buscarPorId(investidor.getId()) == null,
                "O investidor ainda existe no Oracle.");
        investidor = null;
        System.out.println("Exclusoes confirmadas por SELECT.");
    }

    private static boolean contem(List<Empresa> empresas, Long id) {
        for (Empresa empresa : empresas) {
            if (empresa.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static void limparDadosTemporarios() {
        try {
            if (transacao != null && transacao.getId() != null) {
                transacaoDAO.deletar(transacao.getId());
            }
        } catch (PersistenciaException e) {
            System.out.println("Falha ao limpar transacao: " + e.getMessage());
        }
        try {
            if (ativo != null && carteira != null && carteira.getId() != null) {
                ativoDAO.deletar(carteira.getId(), TICKER_TESTE);
            }
        } catch (PersistenciaException e) {
            System.out.println("Falha ao limpar ativo: " + e.getMessage());
        }
        try {
            if (carteira != null && carteira.getId() != null) {
                carteiraDAO.deletar(carteira.getId());
            }
        } catch (PersistenciaException e) {
            System.out.println("Falha ao limpar carteira: " + e.getMessage());
        }
        try {
            if (empresa1 != null && empresa1.getId() != null) {
                empresaDAO.excluir(empresa1.getId());
            }
        } catch (PersistenciaException e) {
            System.out.println("Falha ao limpar primeira empresa: " + e.getMessage());
        }
        try {
            if (empresa2 != null && empresa2.getId() != null) {
                empresaDAO.excluir(empresa2.getId());
            }
        } catch (PersistenciaException e) {
            System.out.println("Falha ao limpar segunda empresa: " + e.getMessage());
        }
        try {
            if (investidor != null && investidor.getId() != null) {
                investidorDAO.excluir(investidor.getId());
            }
        } catch (PersistenciaException e) {
            System.out.println("Falha ao limpar investidor: " + e.getMessage());
        }
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException(mensagem);
        }
    }

    private static void titulo(String nome) {
        System.out.println("\n=== " + nome + " ===");
    }
}
