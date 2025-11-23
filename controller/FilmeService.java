package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import model.Aluguel;
import model.Cliente;

import model.Filme;

/**
 * Serviço responsável por carregar, cadastrar e persistir filmes no arquivo
 * "filmes.txt". Segue o mesmo padrão usado em {@link UsuarioService} para que
 * as telas compartilhem a mesma instância e estado em memória.
 */
public class FilmeService {
    private static final FilmeService INSTANCE = new FilmeService();

    private final List<Filme> filmes;
    private final File arquivoFilmes;
    private final File arquivoAlugueis;

    private FilmeService() {
        this.filmes = new ArrayList<>();
        this.arquivoFilmes = new File("filmes.txt");
        this.arquivoAlugueis = new File("alugueis.txt");
        carregarFilmes();
    }

    public static FilmeService getInstance() {
        return INSTANCE;
    }

    public List<Filme> getFilmes() {
        return Collections.unmodifiableList(filmes);
    }
    
    public Filme buscarFilmePorCodigo(int codigo) {
        return filmes.stream()
                .filter(filme -> filme.getCodFilme() == codigo)
                .findFirst()
                .orElse(null);
    }
    
    public List<String> listarHistoricoAlugueisPorFilme(int codigo, UsuarioService usuarioService) {
        if (!arquivoAlugueis.exists()) {
            return Collections.emptyList();
        }

        List<String> historico = new ArrayList<>();

        try (Scanner leitor = new Scanner(arquivoAlugueis)) {
            while (leitor.hasNextLine()) {
                String[] partes = leitor.nextLine().split(";");
                if (partes.length == 3) {
                    long clienteId = Long.parseLong(partes[0]);
                    int codigoFilmeRegistrado = Integer.parseInt(partes[1]);

                    if (codigoFilmeRegistrado != codigo) {
                        continue;
                    }

                    Cliente cliente = usuarioService.buscarClientePorId(clienteId);

                    String nome = cliente != null ? cliente.getNome() : "Cliente não encontrado";
                    String cpf = cliente != null ? cliente.getCpf() : "ID: " + clienteId;
                    String telefone = cliente != null ? cliente.getTelefone() : "Não informado";
                    String endereco = cliente != null ? cliente.getEndereco() : "Não informado";

                    historico.add(String.format(
                            "Cliente: %s (CPF: %s) - Telefone: %s - Endereço: %s",
                            nome,
                            cpf,
                            telefone,
                            endereco
                    ));
                    continue;
                }

                if (partes.length >= 4) {
                    int codigoFilmeRegistrado = Integer.parseInt(partes[2]);
                    if (codigoFilmeRegistrado != codigo) {
                        continue;
                    }

                    String cpf = partes[0];
                    Cliente cliente = usuarioService.buscarClientePorCpf(cpf);

                    String nome = cliente != null ? cliente.getNome() : partes[1];
                    String telefone = cliente != null ? cliente.getTelefone() : "Não informado";
                    String endereco = cliente != null ? cliente.getEndereco() : "Não informado";
                    
                historico.add(String.format(
                            "Cliente: %s (CPF: %s) - Telefone: %s - Endereço: %s",
                            nome,
                            cpf,
                            telefone,
                            endereco
                    ));
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Arquivo de aluguéis não encontrado: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Erro ao ler histórico de aluguéis: " + e.getMessage(), e);
        }

        return historico;
    }

    public Filme alugarFilme(int codigo, Cliente cliente) {
        Filme filme = buscarFilmePorCodigo(codigo);

        if (filme == null) {
            throw new IllegalArgumentException("Filme não encontrado.");
        }

        if (!"disponível".equalsIgnoreCase(filme.getSituacao())) {
            throw new IllegalArgumentException("O filme selecionado já está alugado.");
        }

        filme.setSituacao("indisponível");
        filme.setCpfClienteAlugou(cliente.getCpf());
        salvarFilmes();
        registrarAluguel(cliente, filme);

        return filme;
    }

    public void devolverFilme(int codigo) {
        Filme filme = buscarFilmePorCodigo(codigo);

        if (filme == null) {
            throw new IllegalArgumentException("Filme não encontrado.");
        }

        if ("disponível".equalsIgnoreCase(filme.getSituacao())) {
            throw new IllegalArgumentException("O filme selecionado já está disponível.");
        }

        filme.setSituacao("disponível");
        filme.setCpfClienteAlugou(null);
        salvarFilmes();
    }

    public void excluirFilme(int codigo) {
        boolean removido = filmes.removeIf(filme -> filme.getCodFilme() == codigo);

        if (!removido) {
            throw new IllegalArgumentException("Filme não encontrado.");
        }
        salvarFilmes();
    }

    /**
     * Cadastra um novo filme e persiste no arquivo.
     *
     * @param codigo         código numérico único do filme
     * @param titulo         título do filme
     * @param genero         gênero textual completo (ex.: "Ação")
     * @param classificacao  idade mínima
     */
    public void cadastrarFilme(int codigo, String titulo, String genero, int classificacao) {
        boolean codigoExistente = filmes.stream()
                .anyMatch(filme -> filme.getCodFilme() == codigo);

        if (codigoExistente) {
            throw new IllegalArgumentException("Já existe um filme cadastrado com esse código.");
        }

        // ainda pode normalizar, para aceitar "A", "a", "ação", etc
        String generoNormalizado = traduzirGenero(genero);

        Filme novo = new Filme(codigo, titulo.trim(), generoNormalizado, classificacao, "disponível");
        filmes.add(novo);
        salvarFilmes();
    }

    private void salvarFilmes() {
        try (PrintWriter arquivo = new PrintWriter(new FileWriter(arquivoFilmes))) {
            for (Filme f : filmes) {
                arquivo.println(
                    f.getCodFilme() + ";" +
                    f.getTitulo() + ";" +
                    f.getGenero() + ";" +
                    f.getClassificacao() + ";" +
                    f.getSituacao()
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao salvar filmes: " + e.getMessage(), e);
        }
    }
    
    private void registrarAluguel(Cliente cliente, Filme filme) {
        Aluguel aluguel = new Aluguel(cliente, filme);

        try (PrintWriter arquivo = new PrintWriter(new FileWriter(arquivoAlugueis, true))) {
            arquivo.println(aluguel.toArquivo());
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao registrar aluguel: " + e.getMessage(), e);
        }
    }


    private void carregarFilmes() {
        if (!arquivoFilmes.exists()) {
            return;
        }

        try (Scanner leitor = new Scanner(arquivoFilmes)) {
            while (leitor.hasNextLine()) {
                String linha = leitor.nextLine();
                String[] partes = linha.split(";");
                if (partes.length >= 5) {
                    String genero = traduzirGenero(partes[2]);
                    int codigo = Integer.parseInt(partes[0]);

                    Filme filme = new Filme(
                            codigo,
                            partes[1],
                            genero,
                            Integer.parseInt(partes[3]),
                            partes[4]
                    );
                    filmes.add(filme);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao carregar filmes: " + e.getMessage(), e);
        }
    }

    private String traduzirGenero(String valorBruto) {
        if (valorBruto == null || valorBruto.isEmpty()) {
            return "";
        }
        String normalizado = valorBruto.trim();
        if (normalizado.length() == 1) {
            char inicial = Character.toUpperCase(normalizado.charAt(0));
            switch (inicial) {
                case 'A':
                    return "Ação";
                case 'R':
                    return "Romance";
                case 'D':
                    return "Drama";
                case 'T':
                    return "Terror";
                case 'F':
                    return "Ficção Científica";
                default:
                    return normalizado;
            }
        }
        return normalizado;
    }
}