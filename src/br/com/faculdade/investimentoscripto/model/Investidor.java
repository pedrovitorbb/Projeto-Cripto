package br.com.faculdade.investimentoscripto.model;

public class Investidor extends Usuario {

    public Investidor() {
    }

    public Investidor(Long id, String nome, String email, String senhaHash) {
        super(id, nome, email, senhaHash);
    }

    @Override
    public void exibirPerfil() {
        System.out.println("Perfil do investidor");
        System.out.println("Id: " + getId());
        System.out.println("Nome: " + getNome());
        System.out.println("Email: " + getEmail());
    }
}
