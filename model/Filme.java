package model;

public class Filme {
	private int codFilme;
	private String titulo;
	private char genero;
	private int classificacao;
	private String situacao;
	private String cpfClienteAlugou;

	public Filme(int cod, String titulo, char genero, int classificacao, String situacao) {
		this.codFilme = cod;
		this.titulo = titulo;
		this.genero = genero;
		this.classificacao = classificacao;
		this.situacao = situacao;
		this.cpfClienteAlugou = null;
	}

	public int getClassificacao() {
		return classificacao;

	}

	public void setClassificacao(int classificacao) {
		this.classificacao = classificacao;

	}

	public String getSituacao() {
		return situacao;

	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
		if ("disponível".equalsIgnoreCase(situacao)) {
			this.cpfClienteAlugou = null;
		}
	}

	public int getCodFilme() {
		return codFilme;

	}

	public void setCodFilme(int codFilme) {
		this.codFilme = codFilme;

	}

	public String getTitulo() {
		return titulo;

	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;

	}

	public char getGenero() {
		return genero;

	}

	public void setGenero(char genero) {
		this.genero = genero;

	}

	public String getCpfClienteAlugou() {
		return cpfClienteAlugou;

	}

	public void setCpfClienteAlugou(String cpfClienteAlugou) {
		this.cpfClienteAlugou = cpfClienteAlugou;

	}

	public String toString() {
		String info = "Código do filme: " + codFilme + "\n" + "Título: " + titulo + "\n" + "Gênero: " + genero + "\n"
				+ "Classificação: " + classificacao + "\n" + "Situação: " + situacao;
		if ("indisponível".equalsIgnoreCase(situacao) && cpfClienteAlugou != null) {
			info += "\nAlugado por CPF: " + cpfClienteAlugou;
		}
		return info;
	}
}
