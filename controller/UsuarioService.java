package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.Cliente;
import model.Usuario;

/**
 * Serviço simples para centralizar o carregamento, cadastro e persistência de usuários
 * em "usuarios.txt". A instância é compartilhada pelas telas para manter o mesmo
 * estado em memória e evitar divergência de dados.
 */
public class UsuarioService {
    private static final UsuarioService INSTANCE = new UsuarioService();

    private final List<Usuario> usuarios;
    private final File arquivoUsuarios;
    private final Map<String, String> tipoTextualPorCpf;

    private UsuarioService() {
        this.usuarios = new ArrayList<>();
        this.arquivoUsuarios = new File("usuarios.txt");
        this.tipoTextualPorCpf = new HashMap<>();
        carregarUsuarios();
    }

    public static UsuarioService getInstance() {
        return INSTANCE;
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }
    
    public boolean existeUsuarioPorCpf(String cpf) {
        String cpfNormalizado = normalizarDocumento(cpf);
        return usuarios.stream()
                .anyMatch(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfNormalizado));
    }

    /**
     * Cadastra um novo usuário e persiste imediatamente no arquivo.
     *
     * @param nome      nome do usuário
     * @param cpf       cpf já formatado (mantido como exibido para compatibilidade)
     * @param telefone  telefone do usuário
     * @param endereco  endereço do usuário
     * @param tipo      tipo do usuário (Cliente ou Funcionario)
     * @throws IllegalArgumentException se já existir alguém com o mesmo CPF
     */
    public void cadastrarUsuario(String nome, String cpf, String telefone, String endereco, String tipo) {
        String cpfNormalizado = normalizarDocumento(cpf);
        boolean existeCpf = usuarios.stream()
                .anyMatch(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfNormalizado));

        if (existeCpf) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este CPF.");
        }

        String tipoNormalizado = traduzirTipo(tipo);
        tipoTextualPorCpf.put(cpf.trim(), tipoNormalizado);

        char tipoChar = tipoNormalizado.isEmpty() ? ' ' : Character.toUpperCase(tipoNormalizado.charAt(0));

        Cliente novo = new Cliente(cpf.trim(), telefone.trim(), endereco.trim(), tipoChar, nome.trim());
        usuarios.add(novo);
        salvarUsuarios();
    }

    private void salvarUsuarios() {
        try (PrintWriter arquivo = new PrintWriter(new FileWriter(arquivoUsuarios))) {
            for (Usuario u : usuarios) {
                String tipoTexto = tipoTextualPorCpf.getOrDefault(u.getCpf(), traduzirTipo(String.valueOf(u.getTipoUsuario())));

                arquivo.println(u.getCpf() + ";" + u.getNome() + ";" + u.getTelefone() + ";" + u.getEndereco() + ";"
                        + tipoTexto);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar usuários: " + e.getMessage(), e);
        }
    }

    private void carregarUsuarios() {
        if (!arquivoUsuarios.exists()) {
            return;
        }

        try (Scanner leitor = new Scanner(arquivoUsuarios)) {
            while (leitor.hasNextLine()) {
                String linha = leitor.nextLine();
                String[] partes = linha.split(";");
                if (partes.length >= 5) {
                    String tipo = traduzirTipo(partes[4]);
                    tipoTextualPorCpf.put(partes[0], tipo);

                    char tipoChar = tipo.isEmpty() ? ' ' : Character.toUpperCase(tipo.charAt(0));

                    Cliente c = new Cliente(partes[0], partes[2], partes[3], tipoChar, partes[1]);
                    usuarios.add(c);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao carregar usuários: " + e.getMessage(), e);
        }
    }

    private String traduzirTipo(String valorBruto) {
        if (valorBruto == null || valorBruto.isEmpty()) {
            return "";
        }
        String normalizado = valorBruto.trim();
        if (normalizado.length() == 1) {
            char inicial = Character.toUpperCase(normalizado.charAt(0));
            if (inicial == 'F') {
                return "Funcionario";
            }
            if (inicial == 'C') {
                return "Cliente";
            }
        }
        return normalizado;
    }

    private String normalizarDocumento(String valor) {
        return valor.replaceAll("[^0-9]", "");
    }
}