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

    private FilmeService() {
        this.filmes = new ArrayList<>();
        this.arquivoFilmes = new File("filmes.txt");
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

    public void alugarFilme(int codigo, String cpfCliente) {
        Filme filme = buscarFilmePorCodigo(codigo);

        if (filme == null) {
            throw new IllegalArgumentException("Filme não encontrado.");
        }

        if (!"disponível".equalsIgnoreCase(filme.getSituacao())) {
            throw new IllegalArgumentException("O filme selecionado já está alugado.");
        }

        filme.setSituacao("indisponível");
        filme.setCpfClienteAlugou(cpfCliente);
        salvarFilmes();
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