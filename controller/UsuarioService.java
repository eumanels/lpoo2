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
    private long proximoId;
    
    private UsuarioService() {
        this.usuarios = new ArrayList<>();
        this.arquivoUsuarios = new File("usuarios.txt");
        this.tipoTextualPorCpf = new HashMap<>();
        this.proximoId = 1L;
        carregarUsuarios();
    }

    public static UsuarioService getInstance() {
        return INSTANCE;
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }
    
    public String obterTipoTextual(Usuario usuario) {
        if (usuario == null) {
            return "";
        }

        String tipo = tipoTextualPorCpf.getOrDefault(usuario.getCpf(), traduzirTipo(String.valueOf(usuario.getTipoUsuario())));
        if (tipo.isEmpty()) {
            return String.valueOf(usuario.getTipoUsuario());
        }

        return tipo;
    }
    
    public boolean existeUsuarioPorCpf(String cpf) {
        String cpfNormalizado = normalizarDocumento(cpf);
        return usuarios.stream()
                .anyMatch(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfNormalizado));
    }
    
    public Cliente buscarClientePorCpf(String cpf) {
        String cpfNormalizado = normalizarDocumento(cpf);

        return usuarios.stream()
                .filter(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfNormalizado))
                .filter(Cliente.class::isInstance)
                .map(Cliente.class::cast)
                .findFirst()
                .orElse(null);
    }
    
    public Cliente buscarClientePorId(long id) {
        return usuarios.stream()
                .filter(usuario -> usuario.getId() == id)
                .filter(Cliente.class::isInstance)
                .map(Cliente.class::cast)
                .findFirst()
                .orElse(null);
    }
    
    public Usuario buscarUsuarioPorCpf(String cpf) {
        String cpfNormalizado = normalizarDocumento(cpf);

        return usuarios.stream()
                .filter(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfNormalizado))
                .findFirst()
                .orElse(null);
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

        Cliente novo = new Cliente(gerarProximoId(), cpf.trim(), telefone.trim(), endereco.trim(), tipoChar, nome.trim());
        usuarios.add(novo);
        salvarUsuarios();
    }
    
    public void atualizarUsuario(String cpfOriginal, String nome, String cpf, String telefone, String endereco, String tipo) {
        String cpfOriginalNormalizado = normalizarDocumento(cpfOriginal);

        Usuario existente = usuarios.stream()
                .filter(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfOriginalNormalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String cpfNovoNormalizado = normalizarDocumento(cpf);
        boolean cpfEmUso = usuarios.stream()
                .filter(usuario -> usuario != existente)
                .anyMatch(usuario -> normalizarDocumento(usuario.getCpf()).equals(cpfNovoNormalizado));

        if (cpfEmUso) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este CPF.");
        }

        String tipoNormalizado = traduzirTipo(tipo);
        char tipoChar = tipoNormalizado.isEmpty() ? ' ' : Character.toUpperCase(tipoNormalizado.charAt(0));

        tipoTextualPorCpf.remove(existente.getCpf());

        existente.setCpf(cpf.trim());
        existente.setTelefone(telefone.trim());
        existente.setEndereco(endereco.trim());
        existente.setTipoUsuario(tipoChar);

        if (existente instanceof Cliente clienteExistente) {
            clienteExistente.setNome(nome.trim());
        }

        tipoTextualPorCpf.put(existente.getCpf(), tipoNormalizado);
        salvarUsuarios();
    }

    private void salvarUsuarios() {
        try (PrintWriter arquivo = new PrintWriter(new FileWriter(arquivoUsuarios))) {
            for (Usuario u : usuarios) {
                String tipoTexto = tipoTextualPorCpf.getOrDefault(u.getCpf(), traduzirTipo(String.valueOf(u.getTipoUsuario())));

                arquivo.println(u.getId() + ";" + u.getCpf() + ";" + u.getNome() + ";" + u.getTelefone() + ";" + u.getEndereco() + ";"
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
                    UsuarioDados usuarioDados = lerDadosUsuario(partes);
                    String tipo = usuarioDados.tipoTraduzido();

                    tipoTextualPorCpf.put(usuarioDados.cpf(), tipo);

                    char tipoChar = tipo.isEmpty() ? ' ' : Character.toUpperCase(tipo.charAt(0));

                    Cliente c = new Cliente(usuarioDados.id(), usuarioDados.cpf(), usuarioDados.telefone(), usuarioDados.endereco(), tipoChar, usuarioDados.nome());
                    usuarios.add(c);
                    atualizarProximoId(usuarioDados.id());
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
    
    private long gerarProximoId() {
        return proximoId++;
    }

    private void atualizarProximoId(long idLido) {
        if (idLido >= proximoId) {
            proximoId = idLido + 1;
        }
    }

    private UsuarioDados lerDadosUsuario(String[] partes) {
        if (partes.length >= 6) {
            return lerDadosNovos(partes);
        }

        return lerDadosAntigos(partes);
    }

    private UsuarioDados lerDadosNovos(String[] partes) {
        try {
            long id = Long.parseLong(partes[0]);
            return new UsuarioDados(id, partes[1], partes[2], partes[3], partes[4], traduzirTipo(partes[5]));
        } catch (NumberFormatException e) {
            return lerDadosAntigos(partes);
        }
    }

    private UsuarioDados lerDadosAntigos(String[] partes) {
        long idGerado = gerarProximoId();
        return new UsuarioDados(idGerado, partes[0], partes[1], partes[2], partes[3], traduzirTipo(partes[4]));
    }

    private record UsuarioDados(long id, String cpf, String nome, String telefone, String endereco, String tipoTraduzido) {
    }
}