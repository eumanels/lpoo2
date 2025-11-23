package controller;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; // Coleções.
import java.util.List;	   // Coleções.
import java.util.Scanner; // Para leitura de arquivo.

import model.Aluguel;
import model.Cliente;
import model.Filme;
import model.Usuario;
import view.LocadoraView;

public class LocadoraController {
	// Uso do list para implementar arraylist.
	private List<Filme> listaFilmes;
	private List<Usuario> listaUsuarios;
	private LocadoraView view;

	public LocadoraController() {
		this.listaFilmes = new ArrayList<>();    // inicializa o arraylist.
		this.listaUsuarios = new ArrayList<>(); // inicializa o arraylist.
		this.view = new LocadoraView(); 
	}

	
	public void iniciar() {
		carregarDados(); // Carrega arquivos ao iniciar.

		char opcao;
		do {
			opcao = view.menuGeral();
			switch (opcao) {
			case '1':
				controleFilmes();
				break;
			case '2':
				controleUsuarios();
				break;
			case '3':
				view.mostrarMensagem("\n===== VOLTE SEMPRE :) =====");
				break;
			default:
				view.mostrarErro("Opção inválida.");
			}
		} while (opcao != '3');
	}

	

	private void controleFilmes() {
		char opcao;
		do {
			opcao = view.menuFilmes();
			switch (opcao) {
			case '1':
				alugarFilme();
				salvarFilmes();  //Salva após alterar status.
				break;
			case '2':
				cadastrarFilme();
				salvarFilmes();
				break;
			case '3':
				pesquisarFilme();
				break;
			case '4':
				excluirFilme();
				salvarFilmes();
				break;
			case '5':
				mostrarFilmes();
				break;
			case '6':
				devolverFilme();
				salvarFilmes();
				break;
			case '7':
		        mostrarAlugueisAtivos(); 
		        break;
			case '8':
				break;

			default:
				view.mostrarErro("Opção inválida.");
			}
		} while (opcao != '8');
	}

	private void controleUsuarios() {
		char opcao;
		do {
			opcao = view.menuUsuarios();
			switch (opcao) {
			case '1':
				cadastrarUsuario();
				salvarUsuarios();
				break;
			case '2':
				pesquisarUsuario();
				break;
			case '3':
				excluirUsuario();
				salvarUsuarios();
				break;
			case '4':
				mostrarUsuarios();
				break;
			case '5':
				break; 
			default:
				view.mostrarErro("Opção inválida.");
			}
		} while (opcao != '5');
	}

	
	// Métodos para filmes.
	private void cadastrarFilme() {
		int cod = view.lerInteiro("Digite o código do filme: ");
		String titulo = view.lerTexto("Digite o título do filme: ");
		char genero = view.lerOpcao("Digite o gênero (A – Ação, T – Terror, D – Drama): ");
		int classif = view.lerInteiro("Digite a classificação: ");
		String situacao = "disponível";
		Filme filme = new Filme(cod, titulo, genero, classif, situacao);
		listaFilmes.add(filme);
		view.mostrarMensagem("Filme cadastrado com sucesso :)");
	}

	private void pesquisarFilme() {
		String titulo = view.lerTexto("Digite o título do filme para pesquisa: ");
		boolean achou = false;
		for (Filme f : listaFilmes) {
			if (f.getTitulo().equalsIgnoreCase(titulo)) {
				view.mostrarMensagem("\nFilme encontrado:");
				view.listarFilme(f);
				achou = true;
			}
		}
		if (!achou)
			view.mostrarErro("Filme não encontrado.");
	}

	private void excluirFilme() {
		char escolha = view.lerOpcao("Deseja excluir pelo código (C) ou pelo título (T)? ");
		boolean removido = false;

		if (escolha == 'C') {
			int cod = view.lerInteiro("Digite o código do filme a excluir: ");
			removido = listaFilmes.removeIf(f -> f.getCodFilme() == cod);
		} else if (escolha == 'T') {
			String titulo = view.lerTexto("Digite o título do filme a excluir: ");
			removido = listaFilmes.removeIf(f -> f.getTitulo().equalsIgnoreCase(titulo));
		}

		if (removido)
			view.mostrarMensagem("Filme removido com sucesso.");
		else
			view.mostrarErro("Filme não encontrado.");
	}

	private void devolverFilme() {
		view.mostrarMensagem("\n--- Devolução de Filme ---");

		// Verifica se existe algum filme alugado.
		boolean temAlugado = false;
		for (Filme f : listaFilmes) {
			if (f.getSituacao().equalsIgnoreCase("indisponível")) {
				temAlugado = true;
				
				System.out.println("Cod: " + f.getCodFilme() + " - " + f.getTitulo() + " (Com CPF: "
						+ f.getCpfClienteAlugou() + ")");
			}
		}

		// Se não tiver nenhum filme alugado, encerra aqui.
		if (!temAlugado) {
			view.mostrarErro("Não há nenhum filme alugado no momento.");
			return;
		}

		// Solicita o código.
		System.out.println("------------------------"); 
		int cod = view.lerInteiro("Digite o código do filme a ser devolvido: ");
		Filme filme = buscarFilme(cod);

		// Validações.
		if (filme != null) {
			// Só aceita devolver se a situação for indisponível.
			if (filme.getSituacao().equalsIgnoreCase("indisponível")) {

				filme.setSituacao("disponível");
				filme.setCpfClienteAlugou(null); // Limpa o CPF de quem estava com ele.

				view.mostrarMensagem("Sucesso! O filme '" + filme.getTitulo() + "' foi devolvido.");
			} else {
				// Se o usuário digitar o código de um filme que já está na loja.
				view.mostrarErro("Este filme já está disponível na locadora. Verifique o código.");
			}
		} else {
			// Se o usuário digitar um código que não existe.
			view.mostrarErro("Filme não encontrado.");
		}
	}
	
	private void mostrarAlugueisAtivos() {
        view.mostrarMensagem("\n--- Relatório de Filmes Alugados ---");
        boolean temAluguel = false;

        for (Filme f : listaFilmes) {
            // Se o filme está indisponível, alguém está com ele.
            if (f.getSituacao().equalsIgnoreCase("indisponível")) {
                temAluguel = true;
                
                // Recupera o CPF gravado no filme.
                String cpfCliente = f.getCpfClienteAlugou();
                
                // Busca o objeto Cliente completo para saber o nome dele.
                Usuario u = buscarUsuario(cpfCliente);
                String nomeCliente = (u != null) ? u.getNome() : "Cliente não encontrado (CPF: " + cpfCliente + ")";

                // Mostra formatado.
                System.out.println("Filme: " + f.getTitulo() + " (Cód: " + f.getCodFilme() + ")");
                System.out.println("Alugado por: " + nomeCliente);
                System.out.println("-------------------------------------------------");
            }
        }

        if (!temAluguel) {
            view.mostrarMensagem("Não há nenhum filme alugado no momento.");
        }
    }

	private void mostrarFilmes() {
		view.mostrarMensagem("\n--- Lista de Filmes ---");
		if (listaFilmes.isEmpty()) {
			view.mostrarMensagem("Nenhum filme cadastrado.");
		} else {
			for (Filme f : listaFilmes) {
				view.listarFilme(f);
			}
		}
	}

	
	// Métodos para alugar filme.
	private void alugarFilme() {
		view.mostrarMensagem("\n--- Filmes Disponíveis ---");
		boolean tem = false;
		for (Filme f : listaFilmes) {
			if (f.getSituacao().equalsIgnoreCase("disponível")) {
				view.listarFilme(f);
				tem = true;
			}
		}

		if (!tem) {
			view.mostrarMensagem("Nenhum filme disponível para aluguel no momento.");
			return;
		}

		int cod = view.lerInteiro("Digite o código do filme que deseja alugar: ");
		Filme filme = buscarFilme(cod);

		if (filme != null) {
			if (filme.getSituacao().equalsIgnoreCase("disponível")) {
				String cpf = view.lerTexto("Digite o CPF do cliente que está alugando: ");
				Usuario usuarioEncontrado = buscarUsuario(cpf);

				// Polimorfismo: Checa se é Cliente.
				if (usuarioEncontrado instanceof Cliente) {
					Cliente cliente = (Cliente) usuarioEncontrado;

					filme.setSituacao("indisponível");
					filme.setCpfClienteAlugou(cliente.getCpf());

					view.mostrarMensagem("\n=== Confirmação do Aluguel ===");
					view.mostrarMensagem("Cliente:");
					view.listarUsuario(cliente);
					view.mostrarMensagem("Filme:");
					view.listarFilme(filme);
					view.mostrarMensagem("Aluguel efetuado com sucesso!");

					Aluguel novoAluguel = new Aluguel(cliente, filme);
					salvarAluguelNoArquivo(novoAluguel);
				} else {
					view.mostrarErro("Cliente não encontrado. Por favor, cadastre o cliente antes de alugar.");
				}
			} else {
				view.mostrarErro("Filme já está alugado.");
			}
		} else {
			view.mostrarErro("Filme não encontrado.");
		}
	}

	
	// Métodos para usuários.
	private void cadastrarUsuario() {
		String cpf = view.lerTexto("Digite o CPF: ");
		String nome = view.lerTexto("Digite o nome: ");
		String tel = view.lerTexto("Digite o telefone: ");
		String end = view.lerTexto("Digite o endereço: ");
		char tipo = view.lerOpcao("Digite o tipo de usuário (C para cliente e F para Funcionario): ");

		// Cria Cliente e adiciona na lista de Usuários (Polimorfismo).
		Cliente c = new Cliente(cpf, tel, end, tipo, nome);
		listaUsuarios.add(c);
		view.mostrarMensagem("Usuário cadastrado com sucesso :)");
	}

	private void pesquisarUsuario() {
		String cpf = view.lerTexto("Digite o CPF do usuário para pesquisa: ");
		Usuario u = buscarUsuario(cpf);
		if (u != null) {
			view.mostrarMensagem("Usuário encontrado:");
			view.listarUsuario(u);
		} else {
			view.mostrarErro("Usuário não encontrado.");
		}
	}

	private void excluirUsuario() {
		String cpf = view.lerTexto("Digite o CPF do usuário a excluir: ");
		boolean removido = listaUsuarios.removeIf(u -> u.getCpf().equalsIgnoreCase(cpf));

		if (removido)
			view.mostrarMensagem("Cliente removido com sucesso.");
		else
			view.mostrarErro("Cliente não encontrado.");
	}

	private void mostrarUsuarios() {
		view.mostrarMensagem("\n--- Lista de Usuários ---");
		if (listaUsuarios.isEmpty()) {
			view.mostrarMensagem("Nenhum usuário cadastrado.");
		} else {
			for (Usuario u : listaUsuarios) {
				view.listarUsuario(u);
			}
		}
	}

	
	// Métodos para auxiliar na busca.
	private Filme buscarFilme(int cod) {
		for (Filme f : listaFilmes) {
			if (f.getCodFilme() == cod)
				return f;
		}
		return null;
	}

	private Usuario buscarUsuario(String cpf) {
		for (Usuario u : listaUsuarios) {
			if (u.getCpf().equalsIgnoreCase(cpf))
				return u;
		}
		return null;
	}

	
	// Persistência dos arquivos.
	private void carregarDados() {
		carregarFilmes();
		carregarUsuarios();
	}

	private void salvarFilmes() {
		try (PrintWriter arquivo = new PrintWriter(new FileWriter("filmes.txt"))) {  // Uso do try - catch.
			for (Filme f : listaFilmes) {
				arquivo.println(f.getCodFilme() + ";" + f.getTitulo() + ";" + f.getGenero() + ";" + f.getClassificacao()
						+ ";" + f.getSituacao());
			}
		} catch (Exception e) {
			view.mostrarErro("Erro ao salvar filmes: " + e.getMessage());
		}
	}

	private void carregarFilmes() {
		File arquivo = new File("filmes.txt");
		if (!arquivo.exists())
			return;

		try (Scanner leitor = new Scanner(arquivo)) {
			while (leitor.hasNextLine()) {
				String linha = leitor.nextLine();
				String[] partes = linha.split(";");
				if (partes.length >= 5) {
					Filme f = new Filme(Integer.parseInt(partes[0]), partes[1], partes[2].charAt(0),
							Integer.parseInt(partes[3]), partes[4]);
					listaFilmes.add(f);
				}
			}
		} catch (Exception e) {
			view.mostrarErro("Erro ao carregar filmes: " + e.getMessage());
		}
	}

	private void salvarUsuarios() {
		try (PrintWriter arquivo = new PrintWriter(new FileWriter("usuarios.txt"))) {
			for (Usuario u : listaUsuarios) {
				arquivo.println(u.getCpf() + ";" + u.getNome() + ";" + u.getTelefone() + ";" + u.getEndereco() + ";"
						+ u.getTipoUsuario());
			}
		} catch (Exception e) {
			view.mostrarErro("Erro ao salvar usuários: " + e.getMessage());
		}
	}

	private void carregarUsuarios() {
		File arquivo = new File("usuarios.txt");
		if (!arquivo.exists())
			return;

		try (Scanner leitor = new Scanner(arquivo)) {
			while (leitor.hasNextLine()) {
				String linha = leitor.nextLine();
				String[] partes = linha.split(";");
				if (partes.length >= 5) {
					Cliente c = new Cliente(partes[0], partes[2], partes[3], partes[4].charAt(0), partes[1]);
					listaUsuarios.add(c);
				}
			}
		} catch (Exception e) {
			view.mostrarErro("Erro ao carregar usuários: " + e.getMessage());
		}
	}

	private void salvarAluguelNoArquivo(Aluguel aluguel) {
		try (PrintWriter pw = new PrintWriter(new FileWriter("alugueis.txt", true))) {
			LocalDateTime agora = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			String dataHora = agora.format(formatter);

			pw.println(aluguel.getCliente().getCpf() + ";" + aluguel.getCliente().getNome() + ";"
					+ aluguel.getFilme().getCodFilme() + ";" + aluguel.getFilme().getTitulo() + ";" + dataHora);

		} catch (IOException e) {
			view.mostrarErro("Erro ao salvar aluguel no arquivo: " + e.getMessage());
		}
	}
}