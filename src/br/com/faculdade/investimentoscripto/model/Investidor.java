package br.com.faculdade.investimentoscripto.model;

public class Investidor extends Usuario {

    public Investidor() {
    }

    public Investidor(Long id, String nome, String email, String senhaHash) {
        super(id, nome, email, senhaHash);
    }

    /** Construtor completo, incluindo o CPF. Usado pelo InvestidorDAO ao ler o banco. */
    public Investidor(Long id, String nome, String email, String senhaHash, String cpf) {
        super(id, nome, email, senhaHash, cpf);
    }

    @Override
    public void exibirPerfil() {
        System.out.println("Perfil do investidor");
        System.out.println("Id: " + getId());
        System.out.println("Nome: " + getNome());
        System.out.println("Email: " + getEmail());
    }

    /** @return descricao do investidor sem a senha, para nao expor o hash em log */
    @Override
    public String toString() {
        return "Investidor{id=" + getId()
                + ", nome='" + getNome() + '\''
                + ", email='" + getEmail() + '\''
                + ", cpf='" + getCpf() + '\''
                + '}';
    }
}
