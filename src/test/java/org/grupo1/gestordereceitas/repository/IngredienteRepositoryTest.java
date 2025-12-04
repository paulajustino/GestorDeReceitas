package org.grupo1.gestordereceitas.repository;

import org.grupo1.gestordereceitas.model.Ingrediente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class IngredienteRepositoryTest {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Test
    void deveSalvarIngredienteCorretamente() {
        // Cenário
        Ingrediente ingredienteASerSalvo = new Ingrediente(null, "Açúcar");

        // Ação
        Ingrediente ingredienteSalvo = ingredienteRepository.save(ingredienteASerSalvo);

        // Validação
        assertNotNull(ingredienteSalvo.getId());
        assertEquals(ingredienteASerSalvo.getNome(), ingredienteSalvo.getNome());
    }

    @Test
    void deveRetornarIngredienteQuandoBuscarPorIdExistente() {
        // Cenário
        Ingrediente ingrediente = new Ingrediente(null, "Farinha");
        ingredienteRepository.save(ingrediente);

        // Ação
        Optional<Ingrediente> ingredienteRetornado = ingredienteRepository.findById(ingrediente.getId());

        // Validação
        assertTrue(ingredienteRetornado.isPresent());
        assertEquals("Farinha", ingredienteRetornado.get().getNome());
    }

    @Test
    void deveRetornarOptionalVazioQuandoBuscarIngredientePorIdInexistente() {
        // Ação
        Optional<Ingrediente> ingredienteRetornado = ingredienteRepository.findById(11L);

        // Validação
        assertTrue(ingredienteRetornado.isEmpty());
    }

    @Test
    void deveRetornarTodosOsIngredientesCorretamente() {
        // Cenário
        Ingrediente ingrediente1 = new Ingrediente(null, "Ovo");
        Ingrediente ingrediente2 = new Ingrediente(null, "Manteiga");
        ingredienteRepository.save(ingrediente1);
        ingredienteRepository.save(ingrediente2);

        // Ação
        List<Ingrediente> ingredientesRetornados = ingredienteRepository.findAll();

        // Validação
        assertFalse(ingredientesRetornados.isEmpty());
        assertEquals(2, ingredientesRetornados.size());
    }

    @Test
    void deveRetornarListaVaziaSeNenhumIngredienteExistir() {
        // Ação
        List<Ingrediente> ingredientesRetornados = ingredienteRepository.findAll();

        // Validação
        assertTrue(ingredientesRetornados.isEmpty());
    }

    @Test
    void deveDeletarIngredienteComSucesso() {
        // Cenário
        Ingrediente ingrediente = new Ingrediente(null, "Ovo");
        ingredienteRepository.save(ingrediente);

        // Ação
        ingredienteRepository.deleteById(ingrediente.getId());
        Optional<Ingrediente> ingredienteRetornado = ingredienteRepository.findById(ingrediente.getId());

        // Validação
        assertTrue(ingredienteRetornado.isEmpty());
    }
}
