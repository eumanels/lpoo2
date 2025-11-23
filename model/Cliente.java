package model;

public class Cliente extends Usuario {  // Herança.
	private String nome;

	public Cliente(String cpf, String telefone, String endereco, char tipoUsuario, String nome) {
		super(cpf, telefone, endereco, tipoUsuario);
		this.nome = nome;
	}

	@Override
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	// implementação do método abstrato.
	@Override
	public String descricaoTipo() {
		return "Cliente da Locadora";
	}

	@Override
	public String toString() {
		// Aproveita o toString do pai, que agora chama descricaoTipo()
		return super.toString() + "\nNome: " + nome;
	}
}