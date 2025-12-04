package org.grupo1.gestordereceitas.repository;

import org.grupo1.gestordereceitas.model.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void deveSalvarCategoriaCorretamente() {
        // Cenário
        Categoria categoriaASerSalva = new Categoria(null, "Sobremesas");

        // Ação
        Categoria categoriaSalva = categoriaRepository.save(categoriaASerSalva);

        // Validação
        assertNotNull(categoriaSalva.getId());
        assertEquals(categoriaASerSalva.getNome(), categoriaSalva.getNome());
    }

    @Test
    void deveRetornarCategoriaQuandoBuscarPorIdExistente() {
        // Cenário
        Categoria categoriaASerSalva = new Categoria(null, "Sobremesas");
        Categoria categoria = categoriaRepository.save(categoriaASerSalva);

        // Ação
        Optional<Categoria> categoriaRetornada = categoriaRepository.findById(categoria.getId());

        // Validação
        assertTrue(categoriaRetornada.isPresent());
        assertEquals(categoria.getNome(), categoriaRetornada.get().getNome());
    }

    @Test
    void deveRetornarOptionalVazioQuandoBuscarCategoriaPorIdInexistente() {
        // Ação
        Optional<Categoria> categoriaRetornada = categoriaRepository.findById(11L);

        // Validação
        assertTrue(categoriaRetornada.isEmpty());
    }

    @Test
    void deveRetornarTodasAsCategoriasCorretamente() {
        // Cenário
        Categoria categoria1 = new Categoria(null, "Sobremesas");
        Categoria categoria2 = new Categoria(null, "Massas");
        categoriaRepository.save(categoria1);
        categoriaRepository.save(categoria2);

        // Ação
        List<Categoria> categoriasRetornadas = categoriaRepository.findAll();

        // Validação
        assertFalse(categoriasRetornadas.isEmpty());
        assertEquals(2, categoriasRetornadas.size());
    }

    @Test
    void deveRetornarListaVaziaSeNenhumaCategoriaExistir() {
        // Ação
        List<Categoria> categoriasRetornadas = categoriaRepository.findAll();

        // Validação
        assertTrue(categoriasRetornadas.isEmpty());
    }

    @Test
    void deveDeletarCategoriaComSucesso() {
        // Cenário
        Categoria categoria = new Categoria(null, "Sobremesas");
        categoriaRepository.save(categoria);

        // Ação
        categoriaRepository.deleteById(categoria.getId());
        Optional<Categoria> categoriaRetornada = categoriaRepository.findById(categoria.getId());

        // Validação
        assertTrue(categoriaRetornada.isEmpty());
    }
}
