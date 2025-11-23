package view;

import java.util.Scanner;
import model.Filme;
import model.Usuario;

public class LocadoraView {
	private Scanner scan;

	public LocadoraView() {
		this.scan = new Scanner(System.in);
	}

	

	public void limparBuffer() {
		
	}

	public String lerTexto(String mensagem) {
		System.out.print(mensagem);
		return scan.nextLine();
	}

	public char lerOpcao(String mensagem) {
		System.out.print(mensagem);
		char opt = scan.next().charAt(0);
		scan.nextLine(); 
		return Character.toUpperCase(opt);
	}

	public int lerInteiro(String mensagem) {
		int num = 0;
		boolean entradaValida = false;

		while (!entradaValida) {
			System.out.print(mensagem);
			String entrada = scan.nextLine(); 

			try {
				// Tenta transformar o texto em número.
				num = Integer.parseInt(entrada);
				entradaValida = true; // Se funcionou, sai do loop.

			} catch (NumberFormatException e) {
				// Se o usuário digitou "abc" ou "", cai aqui.
				System.out.println("ERRO: Entrada inválida. Por favor, digite apenas números.");
				
			}
		}
		return num;
	}

	public void mostrarMensagem(String msg) {
		System.out.println(msg);
	}

	public void mostrarErro(String erro) {
		System.out.println("ERRO: " + erro);
	}

	

	public char menuGeral() {
		System.out.println("\n--- Menu Geral ---");
		System.out.println("1. Filmes");
		System.out.println("2. Usuários");
		System.out.println("3. Sair");
		return lerOpcao("Escolha uma opção: ");
	}

	public char menuFilmes() {
		System.out.println("\n--- Menu Filmes ---");
		System.out.println("1. Alugar Filme");
		System.out.println("2. Cadastrar Filme");
		System.out.println("3. Pesquisar Filme");
		System.out.println("4. Excluir Filme");
		System.out.println("5. Mostrar Filmes (Todos)");
		System.out.println("6. Devolver Filme");
		System.out.println("7. Ver Aluguéis Ativos"); // NOVA OPÇÃO
		System.out.println("8. Voltar"); // MUDOU NUMERAÇÃO
		return lerOpcao("Escolha uma opção: ");
	}

	public char menuUsuarios() {
		System.out.println("\n--- Menu Usuários ---");
		System.out.println("1. Cadastrar Usuário");
		System.out.println("2. Pesquisar Usuário");
		System.out.println("3. Excluir Usuário");
		System.out.println("4. Mostrar Usuários");
		System.out.println("5. Voltar");
		return lerOpcao("Escolha uma opção: ");
	}

	

	public void listarFilme(Filme f) {
		System.out.println(f.toString());
		System.out.println("------------------------");
	}

	public void listarUsuario(Usuario u) {
		System.out.println(u.toString());
		System.out.println("------------------------");
	}
}