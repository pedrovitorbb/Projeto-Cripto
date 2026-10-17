package br.com.faculdade.investimentoscripto.dao;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAO {

    private static final String COLUNAS = "id_empresa, nm_razao_social, nr_cnpj, id_usuario";
    private static final String[] COLUNA_ID = {"ID_EMPRESA"};

    public void inserir(Empresa empresa) throws PersistenciaException {
        validar(empresa);
        String sql = "INSERT INTO T_SIP_EMPRESA (nm_razao_social, nr_cnpj, id_usuario) VALUES (?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql, COLUNA_ID)) {
            comando.setString(1, empresa.getRazaoSocial());
            comando.setString(2, empresa.getCnpj());
            comando.setLong(3, empresa.getIdUsuario());
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("O banco nao devolveu o id da empresa.");
                }
                empresa.setId(chaves.getLong(1));
            }
        } catch (SQLException e) {
            throw erro(e, "inserir empresa");
        }
    }

    public Empresa buscarPorId(long id) throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM T_SIP_EMPRESA WHERE id_empresa = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        } catch (SQLException e) {
            throw erro(e, "buscar empresa por id");
        }
    }

    public List<Empresa> listarTodas() throws PersistenciaException {
        String sql = "SELECT " + COLUNAS + " FROM T_SIP_EMPRESA ORDER BY id_empresa";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                empresas.add(mapear(resultado));
            }
            return empresas;
        } catch (SQLException e) {
            throw erro(e, "listar empresas");
        }
    }

    public boolean atualizar(Empresa empresa) throws PersistenciaException {
        validar(empresa);
        if (empresa.getId() == null) {
            throw new IllegalArgumentException("O id da empresa e obrigatorio.");
        }
        String sql = "UPDATE T_SIP_EMPRESA SET nm_razao_social = ?, nr_cnpj = ?, id_usuario = ? "
                + "WHERE id_empresa = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, empresa.getRazaoSocial());
            comando.setString(2, empresa.getCnpj());
            comando.setLong(3, empresa.getIdUsuario());
            comando.setLong(4, empresa.getId());
            return comando.executeUpdate() == 1;
        } catch (SQLException e) {
            throw erro(e, "atualizar empresa");
        }
    }

    public boolean excluir(long id) throws PersistenciaException {
        String sql = "DELETE FROM T_SIP_EMPRESA WHERE id_empresa = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            return comando.executeUpdate() == 1;
        } catch (SQLException e) {
            throw erro(e, "excluir empresa");
        }
    }

    public List<Empresa> buscarPorNome(String nome) throws PersistenciaException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe parte da razao social.");
        }
        String sql = "SELECT " + COLUNAS + " FROM T_SIP_EMPRESA "
                + "WHERE INSTR(UPPER(nm_razao_social), UPPER(?)) > 0 ORDER BY id_empresa";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, nome);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    empresas.add(mapear(resultado));
                }
            }
            return empresas;
        } catch (SQLException e) {
            throw erro(e, "buscar empresa por nome");
        }
    }

    private void validar(Empresa empresa) {
        if (empresa == null || empresa.getRazaoSocial() == null
                || empresa.getRazaoSocial().isBlank() || empresa.getCnpj() == null
                || !empresa.getCnpj().matches("\\d{14}") || empresa.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "Informe razao social, CNPJ com 14 digitos e id do usuario.");
        }
    }

    private Empresa mapear(ResultSet resultado) throws SQLException {
        return new Empresa(resultado.getLong("id_empresa"),
                resultado.getString("nm_razao_social"),
                resultado.getString("nr_cnpj"),
                resultado.getLong("id_usuario"));
    }

    private PersistenciaException erro(SQLException e, String operacao) {
        String mensagem;
        switch (e.getErrorCode()) {
            case 1 -> mensagem = "CNPJ ja cadastrado ou id duplicado";
            case 2291 -> mensagem = "O usuario informado nao existe";
            case 2292 -> mensagem = "A empresa possui carteiras vinculadas";
            default -> mensagem = "Falha ao " + operacao + " (erro Oracle "
                    + e.getErrorCode() + "): " + e.getMessage();
        }
        return new PersistenciaException(mensagem, e);
    }
}
