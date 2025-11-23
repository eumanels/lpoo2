package model;

public class Aluguel {
	private Cliente cliente;
	private Filme filme;

	public Aluguel(Cliente cliente, Filme filme) {
		this.cliente = cliente;
		this.filme = filme;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public Filme getFilme() {
		return filme;
	}

        // Formato para salvar no arquivo alugueis.txt (IdUsuario;CodFilme;TituloFilme)
        public String toArquivo() {
                return cliente.getId() + ";" + filme.getCodFilme() + ";" + filme.getTitulo();
        }
	
	@Override
	public String toString() {
		return "Cliente: " + cliente.getNome() + " (CPF: " + cliente.getCpf() + ") - Filme: " + filme.getTitulo()
				+ " (Código: " + filme.getCodFilme() + ")";
	}


}