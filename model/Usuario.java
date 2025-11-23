package model;

public abstract class Usuario { // Classe abstrata.
	private long id;
        private String cpf;
        private String telefone;
        private String endereco;

        public Usuario(long id, String cpf, String telefone, String endereco) {
                this.id = id;
                this.cpf = cpf;
                this.telefone = telefone;
                this.endereco = endereco;
        }

        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	// Método abstrato.
	// Toda classe que herdar de Usuario tem que implementar este método.
	public abstract String descricaoTipo();

	public abstract String getNome();

        @Override
        public String toString() {
                return "CPF: " + cpf + "\nTelefone: " + telefone + "\nEndereço: " + endereco;
        }
}