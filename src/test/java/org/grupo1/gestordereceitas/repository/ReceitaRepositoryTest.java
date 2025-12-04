package org.grupo1.gestordereceitas.repository;

import org.grupo1.gestordereceitas.model.Categoria;
import org.grupo1.gestordereceitas.model.Receita;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReceitaRepositoryTest {

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveSalvarReceitaCorretamente() {
        // Cenário
        Categoria categoria = criarCategoria("Massas");
        Receita receitaASerSalva = criarReceita("Macarrao", categoria);

        // Ação
        Receita receitaSalva = receitaRepository.save(receitaASerSalva);

        // Validação
        assertNotNull(receitaSalva.getId());
        assertEquals(receitaASerSalva.getNome(), receitaSalva.getNome());
    }

    @Test
    void deveRetornarReceitaQuandoBuscarPorIdExistente() {
        // Cenário
        Categoria categoria = criarCategoria("Bebidas");
        Receita receitaASerSalva = criarReceita("Laracreme", categoria);
        Receita receita = receitaRepository.save(receitaASerSalva);

        // Ação
        Optional<Receita> receitaRetornada = receitaRepository.findById(receita.getId());

        // Validação
        assertTrue(receitaRetornada.isPresent());
        assertEquals(receita.getNome(), receitaRetornada.get().getNome());
    }

    @Test
    void deveRetornarOptionalVazioQuandoBuscarReceitaPorIdInexistente() {
        // Ação
        Optional<Receita> receitaRetornada = receitaRepository.findById(11L);

        // Validação
        assertTrue(receitaRetornada.isEmpty());
    }

    @Test
    void deveRetornarTodosAsReceitasCorretamente() {
        // Cenário
        Categoria categoria = criarCategoria("Sobremesas");
        Receita receita1 = criarReceita("Bolo de Chocolate", categoria);
        Receita receita2 = criarReceita("Pudim", categoria);
        receitaRepository.save(receita1);
        receitaRepository.save(receita2);

        // Ação
        List<Receita> receitasRetornadas = receitaRepository.findAll();

        // Validação
        assertFalse(receitasRetornadas.isEmpty());
        assertEquals(2, receitasRetornadas.size());
    }

    @Test
    void deveRetornarListaVaziaSeNenhumaReceitaExistir() {
        // Ação
        List<Receita> receitasRetornadas = receitaRepository.findAll();

        // Validação
        assertTrue(receitasRetornadas.isEmpty());
    }

    @Test
    void deveDeletarReceitaComSucesso() {
        // Cenário
        Categoria categoria = criarCategoria("Sobremesas");
        Receita receita = criarReceita("Bolo de Chocolate", categoria);
        receitaRepository.save(receita);

        // Ação
        receitaRepository.deleteById(receita.getId());
        Optional<Receita> receitaRetornada = receitaRepository.findById(receita.getId());

        // Validação
        assertTrue(receitaRetornada.isEmpty());
    }

    private Receita criarReceita(String nome, Categoria categoria) {
        Receita receita = new Receita();
        receita.setNome(nome);
        receita.setCategoria(categoria);
        receita.setReceitaIngredientes(new ArrayList<>());

        return receita;
    }

    private Categoria criarCategoria(String nome) {
        Categoria categoria = new Categoria(null, nome);
        return categoriaRepository.save(categoria);
    }
}
